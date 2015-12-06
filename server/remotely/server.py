import logging
import os
import sys
import socket

from flask import Flask
from flask.ext.restful import Api
from zeroconf import ServiceInfo, Zeroconf

from remotely.commands import Keyboard
from remotely.commands import Media
from remotely.commands import Mouse

DEFAULT_SERVER_IFACE = "0.0.0.0"
DEFAULT_SERVER_PORT = 5051
BUFFER_SIZE = 2048

log = logging.getLogger("")
app = Flask(__name__)
api = Api(app)

api.add_resource(Media, "/media/<command>")
api.add_resource(Keyboard, "/keyboard/press")
api.add_resource(Mouse, "/mouse/<command>")


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

    if os.environ.get("WERKZEUG_RUN_MAIN") == "true":
        zeroconf = Zeroconf()
        zc_info = zeroconf_register(zeroconf, ip, port)

    try:
        app.run(host=DEFAULT_SERVER_IFACE, port=DEFAULT_SERVER_PORT, debug=True)
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
