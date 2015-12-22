from flask_restful import Resource, abort, reqparse

from remotely.util import accept_json_only
from remotely.control import LinuxControl as SystemControl


class BaseCommandResource(Resource):
    def __init__(self):
        self.control = SystemControl()

    def dispatch(self, command_map, command):
        if command not in command_map:
            abort(404)
            return

        return command_map[command]()


class Media(BaseCommandResource):
    @accept_json_only
    def post(self, command):
        command_map = {
            "volume_up": self.command_volume_up,
            "volume_down": self.command_volume_down,
            "volume_mute": self.command_volume_mute,
            "play": self.command_play,
            "pause": self.command_pause,
        }

        return self.dispatch(command_map, command)

    def command_play(self):
        self.control.keypress("XF86AudioPlay")

    def command_pause(self):
        self.control.keypress("XF86AudioPause")

    def command_volume_up(self):
        self.control.keypress("XF86AudioRaiseVolume")

    def command_volume_down(self):
        self.control.keypress("XF86AudioLowerVolume")

    def command_volume_mute(self):
        self.control.keypress("XF86AudioMute")


class Keyboard(BaseCommandResource):
    @accept_json_only
    def post(self):
        argparser = reqparse.RequestParser()
        argparser.add_argument("keycode", type=int, required=True)

        args = argparser.parse_args()
        char = chr(args.keycode)

        # There's probably no harm in adding all those supported.
        charmap = {
            '\x08': "BackSpace",
            '\t': "Tab",
            '\n': "Return"
        }

        self.control.keypress(charmap.get(char, char))


class Mouse(BaseCommandResource):
    @accept_json_only
    def post(self, command):
        command_map = {
            "move": self.command_mouse_move,
            "click_left": self.command_click_left,
            "double_click_left": self.command_double_click_left,
        }

        return self.dispatch(command_map, command)

    def command_mouse_move(self):
        argparser = reqparse.RequestParser()
        argparser.add_argument("delta_x", type=int, required=True)
        argparser.add_argument("delta_y", type=int, required=True)

        args = argparser.parse_args()

        self.control.mouse_move(args.delta_x, args.delta_y)

    def command_click_left(self):
        self.control.mouse_click()

    def command_double_click_left(self):
        self.control.mouse_double_click()


class Meta(BaseCommandResource):
    @accept_json_only
    def get(self, command):
        command_map = {
            "ping": self.ping,
        }

        return self.dispatch(command_map, command)

    def ping(self):
        return {
            "pong": True
        }
