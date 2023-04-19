import pytest
import requests_mock
import json

from requests_mock import Mocker
from uuid import uuid4
from datetime import datetime

from server import Server, TokenDeclinedError


URL = 'https://backend.com/ap'
TOKEN = 'token'
REQUIRED_HEADERS = {'User-Agent': 'AccessPoint',
                    'Authorization': json.dumps({'token': TOKEN})}

def test_register(requests_mock: Mocker):
    """It is possible to register on the backend"""
    backend = Server(URL)
    token = str(uuid4())
    requests_mock.post(f'{URL}/register',
                       request_headers={'User-Agent': 'AccessPoint'},
                       json={'token': token})
    returned_token = backend.register('id', 'room')
    assert backend.token == token
    assert returned_token == token
    sent = requests_mock.last_request.json()
    assert sent['accessPointId'] == 'id'
    assert sent['roomName'] == 'room'

def test_register_declined(requests_mock: Mocker):
    """When the backend declines registration no error is raised"""
    backend = Server(URL)
    requests_mock.post(f'{URL}/register', status_code=401)
    returned_token = backend.register('id', 'room')
    assert backend.token is None
    assert returned_token is None

def test_get_config(requests_mock: Mocker):
    """It is possible to get a configuration from the backend"""
    backend = Server(URL, TOKEN)
    requests_mock.get(f'{URL}/get-config',
                      request_headers=REQUIRED_HEADERS,
                      json={'roomName': 'new',
                            'pairingMode': True,
                            'transferInterval': 45,
                            'sensorStations': [{'bdAddress': 'bd adr 1',
                                                'sensors': [{'sensorName': 'sensor 1-1',
                                                             'limits': {'lowerLimit': -12.34,
                                                                        'upperLimit': 56.78},
                                                             'thresholdTime': 99},
                                                            {'sensorName': 'sensor 1-2',
                                                             'limits': {'lowerLimit': -87.65,
                                                                        'upperLimit': 43.12},
                                                             'thresholdTime': 100}]},
                                               {'bdAddress': 'bd adr 2',
                                                'sensors': [{'sensorName': 'sensor 2-1',
                                                             'limits': {'lowerLimit': 12.34,
                                                                        'upperLimit': -56.78},
                                                             'thresholdTime': 101},
                                                            {'sensorName': 'sensor 2-2',
                                                             'limits': {'lowerLimit': 87.65,
                                                                        'upperLimit': -43.12},
                                                             'thresholdTime': 102}]}]})
    access_point_config, sensor_station_config = backend.get_config()
    assert access_point_config['room_name'] == 'new'
    assert access_point_config['scan_active'] == True
    assert access_point_config['transfer_data_interval'] == 45
    assert sensor_station_config[0]['address'] == 'bd adr 1'
    assert sensor_station_config[0]['sensors'][0]['sensor_name'] == 'sensor 1-1'
    assert sensor_station_config[0]['sensors'][0]['lower_limit'] == -12.34
    assert sensor_station_config[0]['sensors'][0]['upper_limit'] == 56.78
    assert sensor_station_config[0]['sensors'][0]['alarm_tripping_time'] == 99
    assert sensor_station_config[0]['sensors'][1]['sensor_name'] == 'sensor 1-2'
    assert sensor_station_config[0]['sensors'][1]['lower_limit'] == -87.65
    assert sensor_station_config[0]['sensors'][1]['upper_limit'] == 43.12
    assert sensor_station_config[0]['sensors'][1]['alarm_tripping_time'] == 100
    assert sensor_station_config[1]['address'] == 'bd adr 2'
    assert sensor_station_config[1]['sensors'][0]['sensor_name'] == 'sensor 2-1'
    assert sensor_station_config[1]['sensors'][0]['lower_limit'] == 12.34
    assert sensor_station_config[1]['sensors'][0]['upper_limit'] == -56.78
    assert sensor_station_config[1]['sensors'][0]['alarm_tripping_time'] == 101
    assert sensor_station_config[1]['sensors'][1]['sensor_name'] == 'sensor 2-2'
    assert sensor_station_config[1]['sensors'][1]['lower_limit'] == 87.65
    assert sensor_station_config[1]['sensors'][1]['upper_limit'] == -43.12
    assert sensor_station_config[1]['sensors'][1]['alarm_tripping_time'] == 102

def test_revoke_token_with_get_config(requests_mock: Mocker):
    """
    When the backend sends status code 401 to a get get-config request, a TokenDeclinedError is raised and
    the internal token is reset
    """
    backend = Server(URL, TOKEN)
    requests_mock.get(f'{URL}/get-config', status_code=401)
    with pytest.raises(TokenDeclinedError):
        backend.get_config()
    assert backend.token is None

def test_report_found_sensor_stations(requests_mock: Mocker):
    """It is possible to report found sensor stations to the backend"""
    backend = Server(URL, TOKEN)
    requests_mock.put(f'{URL}/found-sensor-stations', request_headers=REQUIRED_HEADERS)
    backend.report_found_sensor_station([{'address': 'adr 1',
                                          'dip-switch': 1},
                                         {'address': 'adr 2',
                                          'dip-switch': 2}])
    sent = requests_mock.last_request.json()
    assert sent['sensorStations'][0]['bdAddress'] == 'adr 1'
    assert sent['sensorStations'][0]['dipSwitchId'] == 1
    assert sent['sensorStations'][1]['bdAddress'] == 'adr 2'
    assert sent['sensorStations'][1]['dipSwitchId'] == 2
    
def test_transfer_data(requests_mock: Mocker):
    """it is possible to transfer data to the backend"""
    backend = Server(URL, TOKEN)
    requests_mock.post(f'{URL}/transfer-data',
                       request_headers=REQUIRED_HEADERS)
    timestamp = datetime.now()
    backend.transfer_data({'adr 1': {'connection_alive': True,
                                      'dip_id': 1},
                           'adr 2': {'connection_alive': False,
                                      'dip_id': None}},
                          [{'sensor_station_address': 'adr 1',
                            'sensor_name': 'sensor 1',
                            'unit': '%',
                            'timestamp': timestamp,
                            'value': -12.34,
                            'alarm': 'n'},
                           {'sensor_station_address': 'adr 1',
                            'sensor_name': 'sensor 2',
                            'unit': '°C',
                            'timestamp': timestamp,
                            'value': 56.78,
                            'alarm': 'h'}])
    sent = requests_mock.last_request.json()
    assert sent[0]['bdAddress'] == 'adr 1'
    assert sent[0]['connectionAlive'] == True
    assert sent[0]['dipSwitchId'] == 1
    assert sent[0]['sensorData'][0]['timeStamp'] == timestamp.isoformat()
    assert sent[0]['sensorData'][0]['value'] == -12.34
    assert sent[0]['sensorData'][0]['alarm'] == 'n'
    assert sent[0]['sensorData'][0]['sensor']['type'] == 'sensor 1'
    assert sent[0]['sensorData'][0]['sensor']['unit'] == '%'
    assert sent[0]['sensorData'][1]['timeStamp'] == timestamp.isoformat()
    assert sent[0]['sensorData'][1]['value'] == 56.78
    assert sent[0]['sensorData'][1]['alarm'] == 'h'
    assert sent[0]['sensorData'][1]['sensor']['type'] == 'sensor 2'
    assert sent[0]['sensorData'][1]['sensor']['unit'] == '°C'
    assert sent[1]['bdAddress'] == 'adr 2'
    assert sent[1]['connectionAlive'] == False
    assert sent[1]['dipSwitchId'] == None
