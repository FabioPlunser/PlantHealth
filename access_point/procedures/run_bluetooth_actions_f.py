import logging
import asyncio

from datetime import datetime, timedelta
from bleak import BleakClient

from util import Config
from sensors import SensorStation, BLEConnectionError, ReadError, WriteError, scan_for_new_stations
from database import Database, DatabaseError, DB_FILENAME
from server import Server, TokenDeclinedError

log = logging.getLogger()


def run_bluetooth_actions(config: Config):
    """
    Scans for new stations if desired
    Collects data from known stations
    Disabled (locks) stations if desired
    """
    # scan for new stations
    if config.scan_active:
        find_stations(config)

    # collect data from assigned stations
    collect_data()

    # set disabled stations to locked
    disable_stations()

    
def find_stations(config: Config):
    """
    Scans for advertising BLEDevices and and filters out potential sensor_stations.
    Sensor stations are identified by their name, set via the constant SENSOR_STATION_NAME.
    Potential sensor stations are the polled and their DIP switch position is requested.
    All sensor stations for which that succeeds are then reported to the backend.
    """
    # If a found BLEDevice has this name, it is treated as a potential new sensor station
    SENSOR_STATION_NAME = 'SensorStation'

    backend = Server(config.backend_address, config.token)
    database = Database(DB_FILENAME)

    # start scan, ignore known stations
    log.info('Starting to scan for sensor stations')
    try:
        known_addresses = database.get_all_known_sensor_station_addresses()
    except DatabaseError as e:
        log.error(f'Unable to load addresses of known sensor stations from database: {e}')
        return
    try:
        new_station_addresses = scan_for_new_stations(known_addresses, SENSOR_STATION_NAME, timedelta(seconds=10))
    except ConnectionError as e:
        log.error(f'Unable to scan for sensor stations: {e}')
        return
    log.info(f'Found {len(new_station_addresses)} potential new sensor stations')

    # get required data
    report_data = []
    for address in new_station_addresses:
        try:
            dip_id = asyncio.run(get_dip_id(address))
            report_data.append({
                'address': address,
                'dip-switch': dip_id
            })
        except BLEConnectionError + (ReadError,) as e:
            log.warning(f'Unable to read DIP id from sensor station {address}: {e}')

    # remove stations that have been enabled while scanning
    try:
        known_addresses = database.get_all_known_sensor_station_addresses()
    except DatabaseError as e:
        log.error(f'Unable to load addresses of known sensor stations from database: {e}')
        return   
    report_data = [entry for entry in report_data if entry.get('address') not in known_addresses]

    # send data to backend
    if report_data:
        try:
            backend.report_found_sensor_station(report_data)
            log.info(f'Reported {len(report_data)} found sensor stations to backend')
        except TokenDeclinedError:
            log.warning('The sensor station has been locked by backend')
            config.reset_token()
            return
        except ConnectionError as e:
            log.error(e)
            return
    else:
        log.info('Did not find any new sensor stations to report to backend')

    config.update(scan_active=False)
    log.info(f'Disabled scanning mode')

def collect_data():
    database = Database(DB_FILENAME)
    try:
        addresses = database.get_all_known_sensor_station_addresses()
    except DatabaseError as e:
        log.error(f'Unable to load addresses of known sensor stations from database: {e}')
        return

    for address in addresses:
        # check if sensor station is still enabled (or it has been disabled in the meantime)
        # (establishing connections to sensor stations takes time - this is not unprobable)
        if address in database.get_all_known_sensor_station_addresses():
            asyncio.run(collect_data_from_single_station(address))

    if len(addresses) == 0:
        log.info('Did not find any assigned sensor stations')

def disable_stations():
    database = Database(DB_FILENAME)
    addresses = database.get_all_disabled_sensor_station_addresses()
    for address in addresses:
        database.delete_sensor_station(address)
        asyncio.run(lock_sensor_station(address))

async def collect_data_from_single_station(address: str):
    """
    Handles the connection and actions for a single sensor station.
    """
    database = Database(DB_FILENAME)
    log.info(f'Connecting to sensor station {address}')
    try:
        async with BleakClient(address) as client:
            log.info(f'Established connection to sensor station {address}')
            sensor_station = SensorStation(address, client)
            timestamp = datetime.now()
            alarms = {}

            # set sensor station to unlocked
            await sensor_station.set_unlocked(True)

            # set DIP id (defined by DIP switches)
            try:
                database.set_dip_id(sensor_station_address=address,
                                    dip_id=await sensor_station.dip_id)
            except DatabaseError as e:
                log.error(f'Unable to update dip id for sensor station {address} in database: {e}')
                return
            
            # read sensor data if new available
            if not await sensor_station.sensor_data_read:
                sensor_values = await sensor_station.sensor_data
                log.info(f'Got data for {len(sensor_values)} sensors from sensor station {address}')
                # get all sensor data
                for sensor_name, value in sensor_values.items():
                    # get unit of value
                    unit = sensor_station.get_sensor_unit(sensor_name)

                    # get current alarm settings
                    try:
                        (lower_limit,
                         upper_limit,
                         alarm_tripping_time,
                         last_inside_limits) = database.get_limits(sensor_station_address=address,
                                                                   sensor_name=sensor_name)
                    except DatabaseError as e:
                        log.error(f'Unable to get limits for sensor {sensor_name} on sensor station {address} from database: {e}')
                        continue
                    
                    # flag alarm if applicable
                    if (lower_limit and
                        value < lower_limit and
                        alarm_tripping_time and
                        (timestamp - last_inside_limits) > alarm_tripping_time):
                        alarms[sensor_name] = 'l'
                    elif (upper_limit and
                            value > upper_limit and
                            alarm_tripping_time and
                            (timestamp - last_inside_limits) > alarm_tripping_time):
                        alarms[sensor_name] = 'h'
                    else:
                        alarms[sensor_name] = 'n'

                    # store measurement in database
                    try:
                        database.add_measurement(sensor_station_address=address,
                                                 sensor_name=sensor_name,
                                                 unit=unit,
                                                 timestamp=timestamp,
                                                 value=value,
                                                 alarm=alarms[sensor_name])
                    except DatabaseError as e:
                        log.error(f'Unable to add measurement for sensor {sensor_name} on sensor station {address} to database: {e}')
                        continue

                # get battery level
                battery_level = await sensor_station.battery_level
                if battery_level:
                    try:
                        database.add_measurement(sensor_station_address=address,
                                                sensor_name='Battery Level',
                                                unit='%',
                                                timestamp=timestamp,
                                                value=battery_level,
                                                alarm='n')
                    except DatabaseError as e:
                        log.error(f'Unable to update battery level for sensor station {address} in database: {e}')
                else:
                    log.info(f'Sensor station {address} did not provide battery level')

                # set data read flag on station
                await sensor_station.set_sensor_data_read(True)
            else:
                log.info(f'No new sensor data available on sensor station {address}')
            
            # set alarms on sensor station
            await sensor_station.set_alarms(alarms)
    except BLEConnectionError as e:
        log.error(f'Unable to connect to sensor station {address}: {e}')
        database.add_failed_connection_attempt(address)
    except ReadError as e:
        log.error(f'Unable to read value from sensor station {address}: {e}')
    except WriteError as e:
        log.error(f'Unable to write value to sensor station {address}: {e}')

async def get_dip_id(address: str) -> int:
    """
    Handles a single connection to a BLE device for reading the
    DIP switch position.
    :param address: The address of the sensor station
    :return: The integer encoded DIP switch position
    :raises BLEConnectionError: If the connection to the device fails
    :raises ReadError: If the DIP switch position could not be read
    """
    async with BleakClient(address) as client:
        sensor_station = SensorStation(address, client)
        return await sensor_station.dip_id
    
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
