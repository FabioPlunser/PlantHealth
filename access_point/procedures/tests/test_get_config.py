from procedures import get_config
from server import Server
from database import Database
from .mock_config import MockConfig

def test_tries_to_register(mocker):
    """
    The procedure tries to register at the backend if no token is set yet and
    stores a received token in the global config
    """
    registration_attempted = [False]
    updated_fields = []
    mocker.patch('procedures.get_config_f.DB_FILENAME', ':memory:')
    def mock_token(*args, **kwargs):
        return None
    mocker.patch.object(MockConfig, 'token', new_callable=mock_token)
    def mock_register(self, *args, **kwargs):
        registration_attempted[0] = True
        return 'token'
    mocker.patch.object(Server, 'register', mock_register)
    def mock_update(self, *args, **kwargs):
        for k in kwargs.keys():
            updated_fields.append(k)
    mocker.patch.object(MockConfig, 'update', mock_update)
    get_config(MockConfig())
    assert registration_attempted[0] == True
    assert 'token' in updated_fields
    assert len(updated_fields) == 1
    
def test_updates_config_and_sensor_stations(mocker):
    """
    The procedure updates the global config with the received config and
    the list of enabled sensor stations
    """
    updated_fields = []
    new_enabled_stations = []
    disabled_stations = []
    mocker.patch('procedures.get_config_f.DB_FILENAME', ':memory:')
    def mock_get_config(self, *args, **kwargs):
        return {'arg1': 1, 'arg2': 2}, [{'address': 'known_address'}, {'address': 'new_address'}]
    mocker.patch.object(Server, 'get_config', mock_get_config)
    def mock_update(self, *args, **kwargs):
        for k in kwargs.keys():
            updated_fields.append(k)
    mocker.patch.object(MockConfig, 'update', mock_update)
    mocker.patch.object(Database, 'get_all_known_sensor_station_addresses', return_value=['known_address', 'address_to_disable'])
    def mock_enable_sensor_station(self, address: str):
        new_enabled_stations.append(address)
    mocker.patch.object(Database, 'enable_sensor_station', mock_enable_sensor_station)
    def mock_disable_sensor_station(self, address: str):
        disabled_stations.append(address)
    mocker.patch.object(Database, 'disable_sensor_station', mock_disable_sensor_station)
    get_config(MockConfig())
    assert len(updated_fields) == 2
    assert 'arg1' in updated_fields
    assert 'arg2' in updated_fields
    assert len(new_enabled_stations) == 1
    assert 'new_address' in new_enabled_stations
    assert len(disabled_stations) == 1
    assert 'address_to_disable' in disabled_stations