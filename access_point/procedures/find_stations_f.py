import logging
import asyncio

from datetime import timedelta
from bleak import BleakClient, exc

from server import Server, TokenDeclinedError
from database import Database, DatabaseError, DB_FILENAME
from util import Config
from sensors import SensorStation, scan_for_new_stations, BLEConnectionError, ReadError

log = logging.getLogger()


# If a found BLEDevice has this name, it is treated as a potential new sensor station
SENSOR_STATION_NAME = 'SensorStation'

def find_stations(config: Config) -> None:
    """
    Scans for advertising BLEDevices and and filters out potential sensor_stations.
    Sensor stations are identified by their name, set via the constant SENSOR_STATION_NAME.
    Potential sensor stations are the polled and their DIP switch position is requested.
    All sensor stations for which that succeeds are then reported to the backend.
    """
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
    
