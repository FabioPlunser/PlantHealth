import pytest
import asyncio

from uuid import uuid4

from .mock_ble import MockBleakClient, MockGATTService, MockGATTCharacteristic
from sensors import SensorStation
from ..sensor_station import get_short_uuid


ADR = 'adr'
BYTEORDER = 'little'

SERVICES =[MockGATTService('dea07cc4-d084-11ed-a760-325096b39f47',  # sensor station info service
                           [MockGATTCharacteristic('2bed', int(0b00000010).to_bytes(1, BYTEORDER) +
                                                           int(0x0000).to_bytes(2, BYTEORDER) +
                                                           int(70).to_bytes(1, BYTEORDER)),
                            MockGATTCharacteristic('2a9a', int(101).to_bytes(1, BYTEORDER)),
                            MockGATTCharacteristic('2ae2', True.to_bytes(1, BYTEORDER)),
                            MockGATTCharacteristic('2abf', int(uuid4()).to_bytes(16, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f48',  # sensor data read
                           [MockGATTCharacteristic('2ae2', False.to_bytes(1, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f49',  # earth humidity
                           [MockGATTCharacteristic('2a6f', int(100).to_bytes(2, BYTEORDER)),
                            MockGATTCharacteristic('2a9a', int(1).to_bytes(1, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f4a',  # air humidity
                           [MockGATTCharacteristic('2a6f', int(200).to_bytes(2, BYTEORDER)),
                            MockGATTCharacteristic('2a9a', int(1).to_bytes(1, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f4b',  # air pressure
                           [MockGATTCharacteristic('2a6d', int(30).to_bytes(4, BYTEORDER)),
                            MockGATTCharacteristic('2a9a', int(1).to_bytes(1, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f4c',  # temperature
                           [MockGATTCharacteristic('2b0d', int(8).to_bytes(1, BYTEORDER, signed=True)),
                            MockGATTCharacteristic('2a9a', int(1).to_bytes(1, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f4d',  # air quality
                           [MockGATTCharacteristic('2b04', int(10).to_bytes(1, BYTEORDER)),
                            MockGATTCharacteristic('2a9a', int(1).to_bytes(1, BYTEORDER))]),
           MockGATTService('dea07cc4-d084-11ed-a760-325096b39f4e',  # light intensity
                           [MockGATTCharacteristic('2aff', int(6).to_bytes(2, BYTEORDER)),
                            MockGATTCharacteristic('2a9a', int(1).to_bytes(1, BYTEORDER))])]

def test_get_short_uuid():
    """A long uuid is correctly shortened"""
    short_uuid = get_short_uuid('00002a9a-0000-1000-8000-00805f9b34fb')
    assert short_uuid == '2a9a'

def test_get_short_uuid_already_short():
    """An alread short uuid is correctly parsed"""
    short_uuid = get_short_uuid('2a9a')
    assert short_uuid == '2a9a'

@pytest.mark.asyncio
async def test_read_dip_id():
    """It is possible to read out the dip switch id from the sensor station"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        dip_id = await sensor_station.dip_id
    assert dip_id == 101

@pytest.mark.asyncio
async def test_read_unlocked():
    """It is possible to read out of the sensor station is unlocked"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        unlocked = await sensor_station.unlocked
    assert unlocked == True

@pytest.mark.asyncio
async def test_set_unlocked():
    """It is possible to set if the sensor station is unlocked"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        await sensor_station.set_unlocked(False)
        unlocked = await sensor_station.unlocked
    assert unlocked == False

@pytest.mark.asyncio
async def test_read_sensor_data_read():
    """It is possible to read out if the current sensor data has already been read"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        sensor_data_read = await sensor_station.sensor_data_read
    assert sensor_data_read == False

@pytest.mark.asyncio
async def test_set_sensor_data_read():
    """It is possible to set if the current sensor data has already been read"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        await sensor_station.set_sensor_data_read(True)
        sensor_data_read = await sensor_station.sensor_data_read
    assert sensor_data_read == True

@pytest.mark.asyncio
async def test_read_sensor_data():
    """It is possible to read out current sensor data"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        sensor_data = await sensor_station.sensor_data
    
    assert sensor_data['Earth Humidity'] == 1
    assert sensor_data['Air Humidity'] == 2
    assert sensor_data['Air Pressure'] == 3
    assert sensor_data['Temperature'] == 4
    assert sensor_data['Air Quality'] == 5
    assert sensor_data['Light Intensity'] == 6

@pytest.mark.asyncio
async def test_read_battery_level():
    """It is possible to read out the current battery level"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        battery_level = await sensor_station.battery_level
    assert battery_level == 70

@pytest.mark.asyncio
async def test_set_alarm():
    """It is possible to set alarms for sensors"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        await sensor_station.set_alarms({'Earth Humidity': 'h',
                                         'Air Humidity': 'h',
                                         'Air Pressure': 'h',
                                         'Temperature': 'h',
                                         'Air Quality': 'h',
                                         'Light Intensity': 'h'})       
    for service in client._services:
        # skip info service
        if service.uuid == 'dea07cc4-d084-11ed-a760-325096b39f47':
            continue
        for characteristic in service.characteristics:
            # alarm characteristics
            if characteristic.uuid.endswith('2a9a'):
                assert characteristic._value == 2

@pytest.mark.asyncio
async def test_get_sensor_unit():
    """It is possible to get the unit for a specific sensor"""
    async with MockBleakClient(ADR, SERVICES) as client:
        sensor_station = SensorStation(ADR, client)
        await sensor_station.sensor_data
        unit = sensor_station.get_sensor_unit('Temperature')
    assert unit == '°C'