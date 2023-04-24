from tempfile import NamedTemporaryFile

from procedures import find_stations
from database import Database
from server import Server, TokenDeclinedError
from .mock_config import MockConfig

def test_reports_new_stations(mocker):
    """Newly found sensor stations are reported to the backend"""
    known_station_addresses = ['k_adr1', 'k_adr2']
    new_station_addresses = ['n_adr1', 'n_adr2']
    reported_station_addresses = []
    mocker.patch('procedures.find_stations_f.DB_FILENAME', ':memory:')
    mocker.patch.object(Database, 'get_all_known_sensor_station_addresses', return_value=known_station_addresses)
    mocker.patch('procedures.find_stations_f.scan_for_new_stations', return_value=new_station_addresses)
    mocker.patch('procedures.find_stations_f.get_dip_id', return_value=0)
    def mock_report_found_sensor_station(self, sensor_stations : list[str]):
        for s in sensor_stations:
            reported_station_addresses.append(s['address'])
    mocker.patch.object(Server, 'report_found_sensor_station', mock_report_found_sensor_station)
    find_stations(MockConfig())
    assert len(reported_station_addresses) == len(new_station_addresses)
    for k_adr in known_station_addresses:
        assert k_adr not in reported_station_addresses
    for n_adr in new_station_addresses:
        assert n_adr in reported_station_addresses

def test_lock_access_point(mocker):
    """
    When sending new sensor stations to the backend results in a TokenDeclinedError,
    the token is deleted from the global configuration
    """
    token_reset = [False]
    mocker.patch('procedures.find_stations_f.DB_FILENAME', ':memory:')
    mocker.patch.object(Database, 'get_all_known_sensor_station_addresses', return_value=[])
    mocker.patch('procedures.find_stations_f.scan_for_new_stations', return_value=['adr'])
    mocker.patch('procedures.find_stations_f.get_dip_id', return_value=0)
    def mock_report_found_sensor_station(self, *args, **kwargs):
        raise TokenDeclinedError
    mocker.patch.object(Server, 'report_found_sensor_station', mock_report_found_sensor_station)
    def mock_reset_token(self, *args, **kwargs):
        token_reset[0] = True
    mocker.patch.object(MockConfig, 'reset_token', mock_reset_token)
    find_stations(MockConfig())
    assert token_reset[0] == True