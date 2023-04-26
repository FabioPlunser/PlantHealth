import logging
import asyncio

from datetime import timedelta
from bleak import BleakClient
from typing import Union

from util import Config
from server import Server, TokenDeclinedError
from database import Database, DB_FILENAME, DatabaseError
from sensors import SensorStation, scan_for_new_stations, BLEConnectionErrorSlow, BLEConnectionErrorFast, ReadError


log = logging.getLogger()

# If a found BLEDevice has this name, it is treated as a potential new sensor station
SENSOR_STATION_NAME = 'SensorStation'

def find_stations(config: Config):
    """
    Scans for advertising BLEDevices and and filters out potential sensor_stations.
    Sensor stations are identified by their name, set via the constant SENSOR_STATION_NAME.
    Potential sensor stations are the polled and their DIP switch position is requested.
    All sensor stations for which that succeeds are then reported to the backend.
    """
    log.info('Starting to scan for sensor stations')

    # start scan, ignore known stations
    new_station_addresses = run_scan()

    # get required data
    report_data = get_data_from_new_stations(new_station_addresses)

    # remove stations that have been enabled while scanning
    report_data = clean_data(report_data)

    # send data to backend
    send_data_to_backend(config, report_data)

    # disable scanning mode
    config.update(scan_active=False)
    log.info(f'Finished scan for new sensor stations')

def run_scan() -> list[str]:
    """
    Runs the actual scan for sensor stations
    :return: A list with the addresses of potential new sensor stations
    """
    database = Database(DB_FILENAME)
    try:
        known_addresses = database.get_all_known_sensor_station_addresses()
    except DatabaseError as e:
        log.error(f'Unable to load addresses of known sensor stations from database: {e}')
        return []
    try:
        new_station_addresses = scan_for_new_stations(known_addresses, SENSOR_STATION_NAME, timedelta(seconds=10))
    except ConnectionError as e:
        log.error(f'Unable to scan for sensor stations: {e}')
        return []
    log.info(f'Found {len(new_station_addresses)} potential new sensor stations')
    return new_station_addresses

def get_data_from_new_stations(addresses: list[str]) -> list[dict[str, Union[str, int]]]:
    """
    Connects to the potential new sensor stations and tries to read the DIP switch id
    :param addresses: The addresses of the sensor stations
    :return: A list of dictionaries with addresses and dip-switch ids
    """
    report_data = []
    for i in range(3):
        for address in addresses:
            try:
                dip_id = asyncio.run(get_dip_id(address))
                report_data.append({
                    'address': address,
                    'dip-switch': dip_id
                })
                break
            except BLEConnectionErrorSlow + (ReadError,) as e:
                log.warning(f'Unable to read DIP id from sensor station {address}: {e}')
    return report_data

def clean_data(report_data: list[dict[str, Union[str, int]]]):
    """
    Removes data of any sensor station from the report that has been enabled in the meantime
    :param report_data: Output of get_data_from_new_stations()
    :return: report_data with all these entries removed that belong to sensor stations that are enabled
    """
    database = Database(DB_FILENAME)
    try:
        known_addresses = database.get_all_known_sensor_station_addresses()
    except DatabaseError as e:
        log.error(f'Unable to load addresses of known sensor stations from database: {e}')
        return []
    return [entry for entry in report_data if entry.get('address') not in known_addresses]

def send_data_to_backend(config:Config, report_data: list[dict[str, Union[str, int]]]) -> None:
    """
    Sends the info on found sensor stations to the backend
    :param config: The configuration containing backend address and token
    :param report_data: Output from clean_data (or get_data_from_new_stations)
    """
    backend = Server(config.backend_address, config.token)
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

async def get_dip_id(address: str) -> int:
    """
    Handles a single connection to a BLE device for reading the
    DIP switch position.
    :param address: The address of the sensor station
    :return: The integer encoded DIP switch position
    :raises BLEConnectionError: If the connection to the device fails
    :raises ReadError: If the DIP switch position could not be read
    """
    connection_attempts = 3
    for i in range(connection_attempts):
        try:
            async with BleakClient(address) as client:
                sensor_station = SensorStation(address, client)
                return await sensor_station.dip_id
        except BLEConnectionErrorFast as e:
            if i >= connection_attempts - 1:
                raise TimeoutError(e)