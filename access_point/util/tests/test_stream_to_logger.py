import logging
import sys

from util import StreamToLogger

def test_stdout_to_logger(caplog):
    """stdout can be redirected to the logger"""
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    sys.stdout = StreamToLogger(logger, logging.INFO)
    print('From stdout')
    assert 'From stdout' in caplog.text

def test_stderr_to_logger(caplog):
    """stderr can be redirected to the logger"""
    logger = logging.getLogger()
    logger.setLevel(logging.ERROR)
    sys.stderr = StreamToLogger(logger, logging.ERROR)
    print('From stderr', file=sys.stderr)
    assert 'From stderr' in caplog.text

def test_placeholder_lines_deleted(caplog):
    """Placeholder lines just containing '~' or '^' are not written to the logger"""
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    sys.stdout = StreamToLogger(logger, logging.INFO)
    print('~')
    print('^')
    assert len(caplog.text) == 0