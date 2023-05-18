import yaml
import pytest
import os

from datetime import datetime, timedelta

from util import Config


TEST_CONF_FILENAME = lambda use: f'./util/tests/test_conf_{use}.yaml'


def test_load_config_from_file():
    """It is possible to load the configuration from a file"""
    config = Config(TEST_CONF_FILENAME('in'))
    with open(TEST_CONF_FILENAME('in'), 'r') as f:
        original = yaml.load(f, Loader=yaml.loader.SafeLoader)
        for k, v in original.items():
            config_value = vars(config)[f'_{k}']
            if isinstance(config_value, timedelta):
                config_value = config_value.total_seconds()
            assert config_value == v

def test_safe_config_to_file():
    """When updating the configuration it gets automatically saved to the file"""
    # create minimum test config file
    with open(TEST_CONF_FILENAME('out'), 'w+') as f:
        f.write(f'backend_address: http://old.com')
    config = Config(TEST_CONF_FILENAME('out'))
    new_value = 'http://new.com'
    config.update(backend_address=new_value)
    assert config.backend_address == new_value
    with open(TEST_CONF_FILENAME('out'), 'r') as f:
        data = yaml.load(f, Loader=yaml.loader.SafeLoader)
        assert data['backend_address'] == new_value
    try:
        os.remove(TEST_CONF_FILENAME('out'))
    except:
        pass

def test_config_from_non_existent_file():
    """When trying to open a config file that does not exist a ValueError is raised"""
    with pytest.raises(ValueError):
        config = Config('does-not-exist')

def test_config_from_malformed_file():
    """When trying to load a malformed config file a ValueError is raised"""
    # create malformed file
    with open(TEST_CONF_FILENAME('malformed'), 'w+') as f:
        f.write('not a valid yaml file')
    with pytest.raises(ValueError):
        config = Config(TEST_CONF_FILENAME('malformed'))
    try:
        os.remove(TEST_CONF_FILENAME('malformed'))
    except:
        pass    

def test_config_from_empty_file():
    """When trying to load an empty config file a ValueError is raised"""
    # create empty_file
    with open(TEST_CONF_FILENAME('empty'), 'w+') as f:
        pass
    with pytest.raises(ValueError):
        config = Config(TEST_CONF_FILENAME('empty'))
    try:
        os.remove(TEST_CONF_FILENAME('empty'))
    except:
        pass 

def test_set_transfer_interval_lower_than_collect_interval():
    """
    When trying to set the transfer interval lower than the collect interval
    a ValueError is raised
    """
    config = Config(TEST_CONF_FILENAME('in'))
    with pytest.raises(ValueError):
        collect_data_interval_seconds = int(config.collect_data_interval.total_seconds())
        config.update(transfer_data_interval=collect_data_interval_seconds - 1)