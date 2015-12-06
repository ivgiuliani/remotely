from functools import wraps
from flask import request
from werkzeug.exceptions import UnsupportedMediaType


def accept_only(*content_types):
    """ Decorator for Flask request handler methods that raises UnsupportedMediaType
        exception if the incoming content-type header is not in the list.
    """
    def decorated(fun):
        @wraps(fun)
        def wrapper(*args, **kwargs):
            if request.mimetype not in content_types:
                raise UnsupportedMediaType()
            return fun(*args, **kwargs)
        return wrapper
    return decorated


def accept_json_only(fun):
    return accept_only('application/json')(fun)
