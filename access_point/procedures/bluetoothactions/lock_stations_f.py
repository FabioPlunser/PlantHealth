import logging
import asyncio

from bleak import BleakClient

from database import Database, DB_FILENAME, DatabaseError
from sensors import SensorStation, BLEConnectionErrorFast, BLEConnectionErrorSlow, WriteError

log = logging.getLogger()


def lock_stations():
    database = Database(DB_FILENAME)
    addresses = database.get_all_disabled_sensor_station_addresses()
    for address in addresses:
        try:
            database.delete_sensor_station(address)
            log.info(f'Deleted sensor station {address} from database')
            asyncio.run(lock_sensor_station(address))
        except DatabaseError as e:
            log.error(f'Unable to delete sensor station {address} from database: {e}')
    
async def lock_sensor_station(address: str) -> None:
    """
    Handles a single connection to a sensor station to reset the 'unlocked' flag.
    """
    connection_attempts = 3
    for i in range(connection_attempts):
        try:
            log.info(f'Attempting to lock sensor station {address}')
            async with BleakClient(address) as client:
                sensor_station = SensorStation(address, client)
                await sensor_station.set_unlocked(False)
                log.info(f'Locked sensor station {address}')
                break
        except BLEConnectionErrorFast as e:
            if i >= connection_attempts - 1:
                log.warning(f'Unable to set sensor station {address} to locked: {e}')
        except BLEConnectionErrorSlow + (WriteError,) as e:
            log.warning(f'Unable to set sensor station {address} to locked: {e}')
            break