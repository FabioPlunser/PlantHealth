import asyncio
import logging

from datetime import timedelta
from bleak import BleakScanner
from .sensor_station import BLEConnectionErrorSlow

log = logging.getLogger()

# suppress log output from bleak and asyncio library
logging.getLogger('bleak').setLevel(logging.WARNING)
logging.getLogger('asyncio').setLevel(logging.WARNING)


def scan_for_new_stations(known_station_addresses: list[str], identifier: str, duration: timedelta) -> list[str]:
    """
    Scans for new sensor stations.
    :param known_station_addresses: A list with addresses of already known stations
    :param identifier: Only devices with names like the given identifier will be recognized as potential sensor stations
    :param duration: Duration of scan
    :return: A list with the addresses of all found and not yet managed sensor stations
    :raises ConnectionError: If a hardware problem occurred while trying to scan
    """
    found_devices = asyncio.run(_scan(duration))
    return [address
            for (name, address) in found_devices
            if name == identifier and address not in known_station_addresses]
    
async def _scan(duration: timedelta) -> list[tuple[str, str]]:
    """
    Internal method - scans for bluetooth devices
    :param duration: Duration of scan
    :return: A list of tuples, containing device names and address
    :raises ConnectionError: If a hardware problem occured while trying to scan
    """
    try:
        devices = await BleakScanner.discover(duration.total_seconds())
    except BLEConnectionErrorSlow as e:
        raise ConnectionError(e)
    return [(d.name, d.address) for d in devices]
