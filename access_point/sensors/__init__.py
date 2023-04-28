"""
Handles the individual sensor stations.

Classes:
    - SensorStation

Functions:
    - scan_for_new_stations

Miscellaneous variables:
    - BLE_CONNECTION_ATTEMPTS

Exceptions:
    - BLEConnectionErrorSlow
    - BLEConnectionErrorFast
    - ReadError
    - WriteError
    - NoConnectionError
"""

from .sensor_station import (SensorStation,
                             BLE_CONNECTION_ATTEMPTS,
                             BLEConnectionErrorSlow,
                             BLEConnectionErrorFast,
                             ReadError,
                             WriteError,
                             NoConnectionError)
from .scanner import scan_for_new_stations