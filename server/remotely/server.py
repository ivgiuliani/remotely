import logging
import sys
import socket
import json

from zeroconf import ServiceInfo, Zeroconf

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
    def __init__(self, ip_addr, port, control):
        self.ip_addr = ip_addr
        self.port = port
        self.control = control
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def listen(self):
        self.socket.bind((self.ip_addr, self.port))
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

        log.info("Server listening on %s:%d" % (self.ip_addr, self.port))

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

            self.execute_command(addr, cmd)

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

    def execute_command(self, from_addr, command):
        if command["name"] == "ping":
            log.debug("Received ping message")
            self.pong(from_addr)
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

    def pong(self, from_addr):
        self.socket.sendto("pong", from_addr)


def zeroconf_register(zc, ip, port):
    properties = {}
    zc_info = ServiceInfo(
        "_http._tcp.local.", "Remotely._http._tcp.local.",
        socket.inet_aton(ip), port, 0, 0, properties
    )
    zc.register_service(zc_info)
    return zc_info


def zeroconf_unregister(zc, zc_info):
    zc.unregister_service(zc_info)
    zc.close()


def main(args):
    logging.basicConfig(level=logging.INFO)

    try:
        ip = args[1]
    except IndexError:
        ip = DEFAULT_SERVER_IFACE

    try:
        port = int(args[2])
    except IndexError:
        port = DEFAULT_SERVER_PORT

    zeroconf = Zeroconf()
    server = RemotelyServer(ip, port, control=control.LinuxControl())
    zc_info = zeroconf_register(zeroconf, ip, port)

    try:
        server.listen()
    except KeyboardInterrupt:
        zeroconf_unregister(zeroconf, zc_info)
        log.info("Caught ^C, exiting.")

    return False


def start_default():
    ip = DEFAULT_SERVER_IFACE
    port = DEFAULT_SERVER_PORT
    main([ip, port])


if __name__ == "__main__":
    sys.exit(main(sys.argv))
