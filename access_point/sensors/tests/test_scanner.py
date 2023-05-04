from datetime import timedelta

from sensors import scan_for_new_stations
from .mock_ble import MockBLEDevice


def test_scan_for_new_station_and_filter_names(mocker):
    """It is possible to scan for new stations and filter out BLE devices with other names"""
    mocker.patch('bleak.BleakScanner.discover',
                 return_value=[MockBLEDevice('station', 'adr 1'),
                               MockBLEDevice('station', 'adr 2'),
                               MockBLEDevice('no station', 'adr 3')])
    
    found_stations = scan_for_new_stations([], 'station', timedelta(seconds=1))
    assert 'adr 1' in found_stations
    assert 'adr 2' in found_stations
    assert 'adr 3' not in found_stations


def test_scan_for_new_station_and_filter_known(mocker):
    """It is possible to scan for new stations and filter out already known stations"""
    mocker.patch('bleak.BleakScanner.discover',
                 return_value=[MockBLEDevice('station', 'adr 1'),
                               MockBLEDevice('station', 'adr 2'),
                               MockBLEDevice('station', 'adr 3')])
    
    found_stations = scan_for_new_stations(['adr 3'], 'station', timedelta(seconds=1))
    assert 'adr 1' in found_stations
    assert 'adr 2' in found_stations
    assert 'adr 3' not in found_stations
