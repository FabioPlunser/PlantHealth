import logging
import asyncio

from bleak import BleakClient

from server import Server, TokenDeclinedError
from database import Database, DatabaseError, DB_FILENAME
from sensors import SensorStation, BLEConnectionError, WriteError
from util import Config

log = logging.getLogger()


def get_config(config: Config) -> None:
    """
    Polls the server backend for the access point configuration.
    Updates the configuration accordingly and tries to store the 
    configuration persistently.
    If new sensor stations are enabled from the backend, they are 
    enabled in the local filestorage.
    If sensor stations are disabled from the backend, they are
    disabled in the local filestorage and a try is made to reset
    the 'unlocked' flag on the sensor station.
    Limits for sensors of known sensor stations are updated if 
    necessary.
    """
    backend = Server(config.backend_address, config.token)
    database = Database(DB_FILENAME)    

    # register at backend if not done yet
    if not backend.token:
        log.info('Not registered at backend yet')
        try:
            log.info(f'Trying to register at {backend.address}')
            token=backend.register(str(config.uuid), config.room_name)
            if token:
                config.update(token=token)
                log.info(f'Received token')
            else:
                log.info(f'No token received')
        except ConnectionError as e:
            log.error(e)
            return
    
    # get new configuration
    else:
        log.info('Checking backend for new configuration')
        try:
            new_config_data, sensor_stations = backend.get_config()
        except TokenDeclinedError:
            log.warning('The sensor station has been locked by backend')
            config.reset_token()
            return
        except ConnectionError as e:
            log.error(e)
            return
        log.info('Received configuration')

        # update configuration
        scan_active_before = config.scan_active
        config.update(**new_config_data)
        if not scan_active_before and config.scan_active:
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
            try:
                database.disable_sensor_station(adr)
            except DatabaseError as e:
                log.error(f'Unable to disable sensor station {adr} in database: {e}')
                continue
            asyncio.run(lock_sensor_station(adr))

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
                        database.update_limits(sensor_station_address=address,
                                                       **sensor)
                    except DatabaseError as e:
                        log.error(f'Unable to update setting for sensor {sensor} on sensor station {adr} in database: {e}')    

async def lock_sensor_station(address: str) -> None:
    """
    Handles a single connection to a sensor station to reset the 'unlocked' flag.
    """
    try:
        log.info(f'Attempting to lock sensor station {address}')
        async with BleakClient(address) as client:
            sensor_station = SensorStation(address, client)
            await sensor_station.set_unlocked(False)
            log.info(f'Locked sensor station {address}')
    except BLEConnectionError + (WriteError,):
        log.warning(f'Unable to set sensor station {address} to locked (deleted from database anyway)')