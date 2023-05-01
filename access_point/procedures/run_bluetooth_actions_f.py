import logging

from datetime import datetime

from util import Config
from procedures.bluetoothactions import find_stations, collect_data, lock_stations

log = logging.getLogger()


def run_bluetooth_actions(config: Config):
    """
    Scans for new stations if desired
    Collects data from known stations (in set interval)
    Disabled (locks) stations if desired
    """
    # scan for new stations
    if config.scan_active:
        find_stations(config)

    # collect data from assigned stations
    if datetime.now() - config.last_data_collect >= config.collect_data_interval:
        collect_data()
        config.data_collected()

    # set disabled stations to locked
    lock_stations()
