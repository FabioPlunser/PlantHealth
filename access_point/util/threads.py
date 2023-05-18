import threading
import typing
import logging

from datetime import datetime, timedelta

log = logging.getLogger()


class ThreadScheduler:
    """
    Class to automatically restart a thread at given interval, but never more than once.
    Creates log output.
    """
    def __init__(self,
                 target: typing.Callable,
                 name: str,
                 interval: timedelta = timedelta(0),
                 start_immediately: bool = False,
                 suppress_interval_warning: bool = False,
                 **kwargs) -> None:
        """
        Creates a thread scheduler to automatically restart the given thread in defined intervals.
        :param target: The method to call when running the thread
        :param name: The name of the thread
        :param interval: The interval in which the thread shall be restarted (if the thread runs longer, it will be restarted immediately afterwards)
        :param start_immediately: 'True' -> Start the thread immediately | 'False' -> Wait for interval, then start
        :param suppress_interval_warning: 'True' -> Do not display warning, when thread runs longer than interval | 'False' -> Display warning
        """
        self.target = target
        self.name = name
        self.interval = interval
        self.kwargs = kwargs

        self.start_timestamp = None
        self.start_immediately = start_immediately
        self.thread = None
        self.interval_warning_logged = False
        self.suppress_interval_warning = suppress_interval_warning or interval == timedelta(0)

    def run(self) -> None:
        """
        Try to start the thread. If the thread is already running, do nothing.
        If the interval has not passed yet, do nothing.
        """
        # set initial start timestamp on first call of the run method - ensures that the timer only starts running,
        # once the thread has been started initially
        if self.start_timestamp is None:
            self.start_timestamp = datetime.now() - (self.interval if self.start_immediately else timedelta(0))
        # check if thread shall be started
        if (datetime.now() - self.start_timestamp) >= self.interval:
            if self.thread is None or not self.thread.is_alive():
                self.thread = threading.Thread(target=self.target, name=self.name, kwargs=self.kwargs, daemon=True)
                log.debug(f'Starting thread {self.name}')
                self.thread.start()
                self.start_timestamp = datetime.now()
                self.interval_warning_logged = False
            elif self.thread.is_alive() and not self.interval_warning_logged and not self.suppress_interval_warning:
                log.warning(f'{self.name} has not finished within given interval ({self.interval.total_seconds()}s)')
                self.interval_warning_logged = True

    def update_interval(self, interval: timedelta) -> None:
        """
        Updates the interval in which the thread shall be restarted.
        :param interval: The new interval
        """
        if interval != self.interval:
            self.interval = interval
            self.interval_warning_logged = False
            log.info(f'Updated interval for calling thread {self.name} to {self.interval.total_seconds()}s')
