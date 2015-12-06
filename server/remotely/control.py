import logging
from subprocess import Popen, PIPE


log = logging.getLogger("")


class LinuxControl(object):
    def __init__(self):
        pass

    def __xte(self, sequence):
        p = Popen(['xte'], stdin=PIPE)
        p.communicate(input=sequence)

    def keypress(self, keys):
        if not isinstance(keys, (list, tuple)):
            keys = [keys]

        log.info("Press %s" % keys)
        sequence = "\n".join(["key %s" % key for key in keys]) + "\n"
        self.__xte(sequence)

    def mouse_move(self, delta_x, delta_y):
        log.debug("Mouse moved: %d %d" % (delta_x, delta_y))
        sequence = "mousermove %d %d\n" % (delta_x, delta_y)
        self.__xte(sequence)

    def mouse_click(self):
        log.debug("Mouse click")
        sequence = "mouseclick 1\n"
        self.__xte(sequence)
