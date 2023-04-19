import time

from datetime import timedelta

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
    for _ in range(planned_executions * 4):
        thread.run()
        time.sleep(new_start_interval.total_seconds() / 4)
    assert counter.executions == planned_executions