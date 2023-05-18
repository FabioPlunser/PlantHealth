import time

from datetime import timedelta, datetime

from util import ThreadScheduler

class ExecutionCounter:
    def __init__(self):
        self.executions = 0
    
    def increase(self):
        self.executions += 1

def dummy_thread(counter: ExecutionCounter):
    counter.increase()


def test_thread_restarts():
    """The thread is restarted in the given intervals"""
    planned_executions = 5
    start_interval = timedelta(milliseconds=200)
    counter = ExecutionCounter()
    thread = ThreadScheduler(dummy_thread, 'dummy', start_interval, start_immediately=True, counter=counter)
    for _ in range(planned_executions * 4):
        thread.run()
        time.sleep(start_interval.total_seconds() / 4)
    assert counter.executions == planned_executions

def test_update_interval():
    """The restart interval of the thread can be adjusted"""
    planned_executions = 5
    start_interval = timedelta(milliseconds=200)
    counter = ExecutionCounter()
    thread = ThreadScheduler(dummy_thread, 'dummy', start_interval, start_immediately=True, counter=counter)
    new_start_interval = timedelta(milliseconds=150)
    thread.update_interval(new_start_interval)
    for _ in range(planned_executions * 4 - 1):
        thread.run()
        time.sleep(new_start_interval.total_seconds() / 4)
    assert counter.executions == planned_executions

def test_start_thread_with_delay():
    """
    If the ThreadScheduler is created with a start delay, the start delay starts running only after calling run()
    for the first time
    """
    delay = timedelta(milliseconds=200)
    start_interval=timedelta(milliseconds=100)
    counter = ExecutionCounter()
    thread = ThreadScheduler(dummy_thread, 'dummy', start_interval, start_immediately=False, counter=counter)
    start_time = datetime.now()
    check_interval = min(delay, start_interval) / 4
    planned_executions = int((delay + start_interval) / check_interval + 1)
    for _ in range(planned_executions):
        if datetime.now() - start_time > delay:
            thread.run()
        time.sleep(check_interval.total_seconds())
    assert counter.executions == 1
