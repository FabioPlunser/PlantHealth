"""
Handles the individual sensor stations.

Classes:
    - SensorStation

Functions:
    - scan_for_new_stations

Exceptions:
    - BLEConnectionErrorSlow
    - BLEConnectionErrorFast
    - ReadError
    - WriteError
    - NoConnectionError
"""

from .sensor_station import SensorStation, BLEConnectionErrorSlow, BLEConnectionErrorFast, ReadError, WriteError, NoConnectionError
from .scanner import scan_for_new_stations