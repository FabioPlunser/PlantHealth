import logging
import asyncio

from bleak import BleakClient, exc

from server import Server, TokenDeclinedError
from database import Database, DatabaseError
from sensors import SensorStation, BLEConnectionError, WriteError
from util import Config, DB_FILENAME

log = logging.getLogger()


################################################
# PROCEDURE: update configuration from backend #
################################################

def get_config(conf: Config):
    backend = Server(conf.backend_address, conf.token)
    database = Database(DB_FILENAME)    

    # register at backend if not done yet
    if not backend.token:
        log.info('Not registered at backend yet')
        try:
            log.info(f'Trying to register at {backend.address}')
            conf.update(token=backend.register(str(conf.uuid), conf.room_name))
            log.info(f'Received token')
        except ConnectionError as e:
            log.error(e)
            return
    
    # get new configuration
    else:
        try:
            log.info('Checking backend for new configuration')
            new_config_data, sensor_stations = backend.get_config()
            log.info('Received configuration')

            # update configuration
            scan_active_before = conf.scan_active
            conf.update(**new_config_data)
            if not scan_active_before and conf.scan_active:
                log.info('Enabling scan for new sensor stations')

            # enable/disable sensor stations
            try:
                known_sensor_station_addresses = database.get_all_known_sensor_station_addresses()
            except DatabaseError as e:
                log.error(f'Unable to load addresses of known sensor stations from database: {e}')
                return
            sensor_stations_to_enable = [station.get('address')
                                         for station in sensor_stations
                                         if station.get('address') not in known_sensor_station_addresses]
            sensor_stations_to_disable = [adr
                                          for adr in known_sensor_station_addresses
                                          if adr not in [station.get('address') for station in sensor_stations]]
            if sensor_stations_to_enable: log.info(f'Enabling sensor stations: {sensor_stations_to_enable}')
            for adr in sensor_stations_to_enable:
                try:
                    database.enable_sensor_station(adr)
                except DatabaseError as e:
                    log.error(f'Unable to enable sensor station {adr} in database: {e}')
                    continue
            if sensor_stations_to_disable: log.info(f'Disabling sensor stations: {sensor_stations_to_disable}')
            for adr in sensor_stations_to_disable:
                asyncio.run(lock_sensor_station(adr))
                try:
                    database.disable_sensor_station(adr)
                except DatabaseError as e:
                    log.error(f'Unable to disable sensor station {adr} in database: {e}')
                    continue

            # update limits for sensors if applicable
            for sensor_station in sensor_stations:
                address = sensor_station.get('address')
                sensors = sensor_station.get('sensors')
                if sensors:
                    log.info(f'Updating limits for sensor station {address}')
                    for sensor in sensors:
                        # update limits for each sensor
                        log.info(f'Adjusting limits for sensor {sensor.get("sensor_name")}')
                        try:
                            database.update_sensor_setting(sensor_station_address=address,
                                                           **sensor)
                        except DatabaseError as e:
                            log.error(f'Unable to update setting for sensor {sensor} on sensor station {adr} in database: {e}')
            
        except TokenDeclinedError:
            log.warning('Token not valid anymore')
            conf.update(token=None)
        except ConnectionError as e:
            log.error(e)

async def lock_sensor_station(address: str):
    try:
        log.info(f'Attempting to lock sensor station {address}')
        async with BleakClient(address) as client:
            sensor_station = SensorStation(address, client)
            await sensor_station.set_unlocked(False)
            log.info(f'Locked sensor station {address}')
    except BLEConnectionError + (WriteError,):
        log.warning(f'Unable to set sensor station {address} to locked (deleting from assigned sensor stations anyway)')