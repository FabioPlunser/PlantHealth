class StreamToLogger(object):
   """
   Replace sys.stdout, sys.stderr, etc. with an instance of this class to redirect their output
   to the logfiles.
   
   Taken from: https://stackoverflow.com/questions/19425736/how-to-redirect-stdout-and-stderr-to-logger-in-python
   """
   def __init__(self, logger, level):
      self.logger = logger
      self.level = level
      self.linebuf = ''

   def write(self, buf):
      for line in buf.rstrip().splitlines():
         if line != '^' and line != '~':
            self.logger.log(self.level, line.rstrip())
