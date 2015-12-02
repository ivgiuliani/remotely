import logging
import sys
import socket
import json

from remotely import control


BUFFER_SIZE = 2048

log = logging.getLogger("")


class NotAuthorizedException(Exception):
    pass


class InvalidMessage(Exception):
    pass


class RemotelyServer(object):
    def __init__(self, ip_addr, port, control):
        self.ip_addr = ip_addr
        self.port = port
        self.control = control
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def listen(self):
        self.socket.bind((self.ip_addr, self.port))
        log.info("Server listening on %s:%d" % (self.ip_addr, self.port))

        while True:
            data, addr = self.socket.recvfrom(BUFFER_SIZE)
            log.debug("Received message (from=%s): %s" % (addr, data))

            cmd = self.parse_cmd(data)
            if not self.has_command(cmd):
                log.warn("Command not found.")
                continue

            self.execute_command(cmd)

    def parse_cmd(self, data):
        try:
            cmd = json.loads(data)
        except ValueError:
            raise InvalidMessage()

        if not self.is_authorized(cmd):
            raise NotAuthorizedException()

        return cmd

    def is_authorized(self, command):
        return True

    def has_command(self, command):
        return True

    def execute_command(self, command):
        if command["name"] == "vol_up":
            self.control.keypress("XF86AudioRaiseVolume")
        elif command["name"] == "vol_down":
            self.control.keypress("XF86AudioLowerVolume")
        elif command["name"] == "vol_mute":
            self.control.keypress("XF86AudioMute")
        elif command["name"] == "mm_play":
            self.control.keypress("XF86AudioPlay")
        elif command["name"] == "mm_pause":
            self.control.keypress("XF86AudioPause")


def main(args):
    logging.basicConfig(level=logging.INFO)

    ip = args[0]
    port = int(args[1])

    server = RemotelyServer(ip, port, control=control.LinuxControl())
    server.listen()

    return False


def start_default():
    ip = "0.0.0.0"
    port = 5051
    main([ip, port])


if __name__ == "__main__":
    sys.exit(main(sys.argv))
