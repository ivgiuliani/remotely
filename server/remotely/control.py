import logging
from subprocess import Popen, PIPE


log = logging.getLogger("")


class LinuxControl(object):
    def __init__(self):
        pass

    def keypress(self, keys):
        if not isinstance(keys, (list, tuple)):
            keys = [keys]
        log.info("Press %s" % keys)

        sequence = "\n".join(["key %s" % key for key in keys]) + "\n"

        p = Popen(['xte'], stdin=PIPE)
        p.communicate(input=sequence)
