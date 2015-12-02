import logging
import sys
import socket
import json
import threading

from remotely import control

DEFAULT_SERVER_IFACE = "0.0.0.0"
DEFAULT_SERVER_PORT = 5051
BUFFER_SIZE = 2048

log = logging.getLogger("")


class NotAuthorizedException(Exception):
    pass


class InvalidMessage(Exception):
    pass


class RemotelyServer(object):
    ANNOUNCER_INTERVAL_SECONDS = 30
    ANNOUNCE_OBJ = {
        "name": "announce",
        "v": "1.0",
    }

    def __init__(self, ip_addr, port, control):
        self.ip_addr = ip_addr
        self.port = port
        self.control = control
        self.announce_timer = None
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def announce_loop(self):
        if self.announce_timer is not None:
            self.announce_timer.cancel()

        self.announce_timer = None

        self.socket.sendto(json.dumps(self.ANNOUNCE_OBJ), ('<broadcast>', self.port))
        self.announce_timer = threading.Timer(self.ANNOUNCER_INTERVAL_SECONDS, self.announce_loop)
        self.announce_timer.start()

    def listen(self):
        self.socket.bind((self.ip_addr, self.port))
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

        log.info("Server listening on %s:%d" % (self.ip_addr, self.port))

        self.announce_loop()

        while True:
            data, addr = self.socket.recvfrom(BUFFER_SIZE)
            log.debug("Received message (from=%s): %s" % (addr, data))

            try:
                cmd = self.parse_cmd(data)
            except InvalidMessage:
                log.warn("Received invalid command: %s" % cmd)
                continue

            if not self.has_command(cmd):
                log.warn("Command not found.")
                continue

            self.execute_command(cmd)

    def stop(self):
        if self.announce_timer is not None:
            self.announce_timer.cancel()

    def parse_cmd(self, data):
        try:
            cmd = json.loads(data)
        except ValueError:
            raise InvalidMessage()

        if not isinstance(cmd, dict):
            raise InvalidMessage()

        if not self.is_authorized(cmd):
            raise NotAuthorizedException()

        return cmd

    def is_authorized(self, command):
        return True

    def has_command(self, command):
        return True

    def execute_command(self, command):
        if command["name"] == "announce":
            log.debug("Received announce message")
        elif command["name"] == "vol_up":
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
    try:
        server.listen()
    except KeyboardInterrupt:
        log.info("Caught ^C, exit.")
        server.stop()

    return False


def start_default():
    ip = DEFAULT_SERVER_IFACE
    port = DEFAULT_SERVER_PORT
    main([ip, port])


if __name__ == "__main__":
    sys.exit(main(sys.argv))
