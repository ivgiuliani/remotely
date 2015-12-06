from flask_restful import Resource, abort, reqparse

from remotely.util import accept_json_only
from remotely.control import LinuxControl as SystemControl


class BaseCommandResource(Resource):
    def __init__(self):
        self.control = SystemControl()


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

        if command not in command_map:
            abort(404)
            return

        return command_map[command]()

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
    def post(self, key):
        self.control.keypress(key)


class Mouse(BaseCommandResource):
    @accept_json_only
    def post(self, command):
        command_map = {
            "move": self.command_mouse_move,
            "click_left": self.command_click_left,
        }

        if command not in command_map:
            abort(404)
            return

        return command_map[command]()

    def command_mouse_move(self):
        argparser = reqparse.RequestParser()
        argparser.add_argument("delta_x", type=int, required=True)
        argparser.add_argument("delta_y", type=int, required=True)

        args = argparser.parse_args()

        self.control.mouse_move(args.delta_x, args.delta_y)

    def command_click_left(self):
        self.control.mouse_click()

