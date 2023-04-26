import pytest
import asyncio

from procedures.bluetoothactions import collect_data
from database import Database, DB_FILENAME
from sensors import SensorStation
from procedures.bluetoothactions.collect_data_f import collect_data_from_single_station
from sensors.tests.mock_ble import MockBleakClient


def test_iterates_through_all_sensor_stations(mocker):
    """The collect_data() procedure iterates through all sensor stations"""
    stations_checked = {'adr1': False, 'adr2': False}
    async def mock_collect_data_from_single_station(address: str):
        stations_checked[address] = True
    mocker.patch('procedures.bluetoothactions.collect_data_f.collect_data_from_single_station',
                 new=mock_collect_data_from_single_station)
    mocker.patch('database.Database.get_all_known_sensor_station_addresses',
                 return_value=stations_checked.keys())
    collect_data()
    for v in stations_checked.values():
        assert v == True

def test_does_not_connect_to_recently_disabled_sensor_station(mocker):
    """
    The collect_data() procedure does not connect to a sensor station that has been disabled,
    while other sensor stations have been polled
    """
    stations_checked = {'adr1': False, 'adr2': False}
    sensor_station_addresses = list(stations_checked.keys())
    async def mock_collect_data_from_single_station(address: str):
        stations_checked[address] = True
        sensor_station_addresses.pop(1)
    mocker.patch('procedures.bluetoothactions.collect_data_f.collect_data_from_single_station',
                 new=mock_collect_data_from_single_station)
    mocker.patch('database.Database.get_all_known_sensor_station_addresses',
                 return_value=sensor_station_addresses)
    collect_data()
    assert stations_checked['adr1'] == True
    assert stations_checked['adr2'] == False

def test_sets_unlocked(mocker):
    """The collect_data_from_single_station() procedure sets the sensor station to unlocked"""
    unlocked = [False]
    mocker.patch('procedures.bluetoothactions.collect_data_f.BleakClient', MockBleakClient)
    mocker.patch('procedures.bluetoothactions.collect_data_f.DB_FILENAME', ':memory:')
    def mock_set_unlocked(self, value: bool):
        unlocked[0] = value
    mocker.patch.object(SensorStation, 'set_unlocked', mock_set_unlocked)
    asyncio.run(collect_data_from_single_station('adr'))
    assert unlocked[0] == True


def test_reads_sensor_data_if_available(mocker):
    """The collect_data_from_single_station() procedure reads sensor data if available"""
    sensor_data_read = [False]
    mocker.patch('procedures.bluetoothactions.collect_data_f.BleakClient', MockBleakClient)
    mocker.patch('procedures.bluetoothactions.collect_data_f.DB_FILENAME', ':memory:')
    async def mock_set_unlocked(*args, **kwargs):
        await asyncio.sleep(0)
    mocker.patch.object(SensorStation, 'set_unlocked', mock_set_unlocked)
    async def mock_dip_id(*args, **kwargs):
        return 0
    mocker.patch.object(SensorStation, 'dip_id', new_callable=mock_dip_id)
    mocker.patch.object(Database, 'set_dip_id', return_value=True)
    async def mock_battery_level(*args, **kwargs):
        return 0
    mocker.patch.object(SensorStation, 'battery_level', new_callable=mock_battery_level)
    async def mock_sensor_data_read(*args, **kwargs):
        return False
    mocker.patch.object(SensorStation, 'sensor_data_read', new_callable=mock_sensor_data_read)
    async def mock_read_sensor_data(self, *args, **kwargs):
        return
    mocker.patch('procedures.bluetoothactions.collect_data_f.read_sensor_data', mock_read_sensor_data)
    def mock_set_sensor_data_read(self, value, **kwargs):
        sensor_data_read[0] = value
    mocker.patch.object(SensorStation, 'set_sensor_data_read', mock_set_sensor_data_read)
    asyncio.run(collect_data_from_single_station('adr'))
    assert sensor_data_read[0] == True