import sys

def hello(name: str) -> str:
    return f"Hello, {name}! (Python {'.'.join(map(str, sys.version_info[:3]))})"
