import validators
import yaml
import logging

from uuid import UUID, uuid4
from datetime import timedelta

log = logging.getLogger()


# File in which the configuration is stored
CONFIG_FILENAME = 'conf.yaml'

# helper methods
def describe_wrong_type(name, type):
    return f'Expected value of type {type.__name__} for {name}'

def describe_limits(name, lower, upper, unit=''):
    return f'Expected value of {name} between {lower}{unit} and {upper}{unit}'

class Config(object):
    """
    Handler class for the global configuration.
    """

    # Attributes of the configuration that are not stored within the config file
    ATTRIBUTES_NO_FILESAVE = [
                    'filename',
                    'scan_active'
                ]

    def __init__(self, filename: str) -> None:
        """
        Initializes the handler for the configuration.
        :param filename: Name of the file in which the configuration is saved.
        """
        self._filename = filename    # not stored in config file
        self._set_defaults()
        self._load()

    def _set_defaults(self):
        """
        Sets default values for the configuration.
        """
        self._uuid = uuid4()
        self._room_name = 'New AccessPoint'
        self._backend_address = None
        self._token = None

        self._get_config_interval = timedelta(seconds=5)
        self._collect_data_interval = timedelta(seconds=10)
        self._transfer_data_interval = timedelta(seconds=30)
        
        self._scan_active = False    # not stored in config file

        self._debug = False

    @property
    def uuid(self):
        """A self assigned UUID. Randomly generated."""
        return self._uuid

    @property
    def room_name(self):
        """Name of the room in which the access point is positioned."""
        return self._room_name

    @property
    def backend_address(self):
        """URL or IP at which the backend can be reached."""
        return self._backend_address
    
    @property
    def token(self):
        """The token for authenticating against the backend."""
        return self._token
    
    @property
    def get_config_interval(self):
        """Interval in which the backend is polled for configuration updates."""
        return self._get_config_interval
    
    @property
    def collect_data_interval(self):
        """Interval in which data is collected from sensor stations."""
        return self._collect_data_interval
    
    @property
    def transfer_data_interval(self):
        """Interval in which collected sensor data is transferred to the backend."""
        return self._transfer_data_interval
    
    @property
    def scan_active(self):
        """Flag to indicate the access point shall scan for new sensor stations."""
        return self._scan_active
    
    @property
    def debug(self):
        """Flag to indicate if debugging output shall be included in the logfiles."""
        return self._debug

    def update(
            self,
            uuid: str=None,
            room_name:str=None,
            backend_address:str=None,
            token:str=None,
            get_config_interval:int=None,
            collect_data_interval:int=None,
            transfer_data_interval:int=None,
            scan_active:bool=None,
            debug:bool=None,
            **kwargs
        ) -> None:
        """
        Updates the configuration with the given values.
        Validates before updating.
        :raises ValueError: If some given values do not pass the validation
        """
        if kwargs:
            log.debug(f'Received {len(kwargs)}')

        data = {k: v for k, v in locals().items() if k != 'self'}
        self._validate(**data)

        current_state = self._get_cleaned_vars()
        change_found = len([v for k, v in locals().items() if k in current_state and v is not None and v != current_state[k]]) > 0

        if change_found:
            self._set_new_values(uuid,
                                 room_name,
                                 backend_address,
                                 token,
                                 get_config_interval,
                                 collect_data_interval,
                                 transfer_data_interval,
                                 scan_active,
                                 debug)
            self._save()

    def _set_new_values(self,
                        uuid: str,
                        room_name:str,
                        backend_address:str,
                        token:str,
                        get_config_interval:int,
                        collect_data_interval:int,
                        transfer_data_interval:int,
                        scan_active:bool,
                        debug:bool) -> None:
        """Sets new values."""
        self._uuid = uuid if uuid else self._uuid
        self._room_name = room_name if room_name else self._room_name
        self._backend_address = backend_address if backend_address else self._backend_address
        self._token = token if token else self._token

        self._get_config_interval = timedelta(seconds=get_config_interval) if get_config_interval else self._get_config_interval
        self._collect_data_interval = timedelta(seconds=collect_data_interval) if collect_data_interval else self._collect_data_interval
        self._transfer_data_interval = timedelta(seconds=transfer_data_interval) if transfer_data_interval else self._transfer_data_interval

        self._scan_active = scan_active if scan_active else self._scan_active

        self._debug = debug if debug else self._debug

    def reset_token(self):
        """Resets the token."""
        self._token = None
        self._save()

    def _load(self):
        """
        Loads configuration from the file given during initialization.
        :raises ValueError: If validation of the values loaded from the file fails.
        """
        try:
            with open(self._filename, 'r') as f:
                try:
                    data = yaml.load(f, Loader=yaml.loader.SafeLoader)
                    if not data:
                        raise ValueError('Config file empty')
                    try:
                        self.update(**data)
                    except (TypeError, ValueError) as e:
                        raise ValueError(e)
                except yaml.YAMLError as e:
                    raise ValueError(e)
        except FileNotFoundError as e:
            raise ValueError(e)

    def _save(self):
        """
        Saves current configuration to the file given during initialization.
        """
        try:
            data = self._get_cleaned_vars()
            data = {k: v for k, v in data.items() if k not in Config.ATTRIBUTES_NO_FILESAVE and v is not None}
            with open(self._filename, 'w') as f:
                f.write(yaml.dump(data))
        except FileNotFoundError as e:
            raise ValueError(e)
    
    def _validate(
            self,
            uuid:str=None,
            room_name:str=None,
            backend_address:str=None,
            token:str=None,
            get_config_interval:int=None,
            collect_data_interval:int=None,
            transfer_data_interval:int=None,
            scan_active:bool=None,
            debug:bool=None,
            **kwargs
        ):
        """
        Validates the given values against set constraints.
        :raises ValueError: If validation fails
        """
        if kwargs:
            log.debug(f'Unknown parameters: {kwargs.get("kwargs")}')

        self._validate_backend_info(backend_address,
                                    token)
        
        self._validation_access_point_info(uuid,
                                           room_name,
                                           scan_active,
                                           debug)

        self._validate_intervals(get_config_interval,
                                 collect_data_interval,
                                 transfer_data_interval)

        
    def _validate_intervals(self,
                            get_config_interval:int=None,
                            collect_data_interval:int=None,
                            transfer_data_interval:int=None):
        """
        Sub-method for _validate()
        :raises ValueError: If validation fails
        """
        # datatypes
        if get_config_interval and not isinstance(get_config_interval, int):
            raise ValueError(describe_wrong_type('get_config_interval', int))
        if collect_data_interval and not isinstance(collect_data_interval, int):
            raise ValueError(describe_wrong_type('collect_data_interval', int))
        if transfer_data_interval and not isinstance(transfer_data_interval, int):
            raise ValueError(describe_wrong_type('transfer_data_interval', int))
        
        # min and max values
        if get_config_interval and not 1 <= get_config_interval <= 10:
            raise ValueError(describe_limits('get_config_interval', 1, 10, 'sec'))
        if collect_data_interval and not 1 <= collect_data_interval <= 3600:
            raise ValueError(describe_limits('collect_data_interval', 1, 3600, 'sec'))
        if transfer_data_interval:
            if collect_data_interval:
                transfer_data_interval_lower_limit = collect_data_interval
            else:
                transfer_data_interval_lower_limit = self._collect_data_interval.total_seconds()
            if not transfer_data_interval_lower_limit <= transfer_data_interval <= 3600:
                raise ValueError(describe_limits('transfer_data_interval', transfer_data_interval_lower_limit, 3600, 'sec'))
            
    def _validate_backend_info(self,
                               backend_address:str=None,
                               token:str=None):
        """
        Sub-method for _validate()
        :raises ValueError: If validation fails
        """
        # data types
        if backend_address and not isinstance(backend_address, str):
            raise ValueError(describe_wrong_type('backend_address', str))
        if token and not isinstance(token, str):
            raise ValueError(describe_wrong_type('token', str))
        
        # general
        if not backend_address and not self._backend_address:
            raise ValueError('Expected a value for backend_address')
        if backend_address and not validators.url(backend_address):
            raise ValueError('Expected valid URL for backend_address')
        
    def _validation_access_point_info(self,
                                      uuid:str=None,
                                      room_name:str=None,
                                      scan_active:bool=None,
                                      debug:bool=None,):
        """
        Sub-method for _validate()
        :raises ValueError: If validation fails
        """
        if uuid and not isinstance(uuid, str):
            raise ValueError(describe_wrong_type('uuid', str))
        if room_name and not isinstance(room_name, str):
            raise ValueError(describe_wrong_type('room_name', str))
        if scan_active is not None and not isinstance(scan_active, bool):
            raise ValueError(describe_wrong_type('scan_active', bool))
        if debug and not isinstance(debug, bool):
            raise ValueError(describe_wrong_type('debug', bool))
        
        if uuid and not validators.uuid(uuid):
            raise ValueError('Expected a valid UUID for uuid')

    def _get_cleaned_vars(self):
        """
        Like vars(self) but with a modified output.
        Required due to the use of the @property decorator.
        """
        # vars(self) has leading '_' for each key -> remove that leading '_'
        data = {k[1:]: v for k, v in vars(self).items()}
        # transform uuid to string
        if 'uuid' in data:
            data['uuid'] = str(data['uuid'])
        # transform all timedelta objects to an integer value representing their total seconds
        data = {k: int(v.total_seconds()) if isinstance(v, timedelta) else v for k, v in data.items()}
        return data