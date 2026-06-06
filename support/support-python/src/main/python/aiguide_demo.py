"""AiGuide 内嵌 Python 示例脚本（support-python）。

由 Kotlin 侧经 Chaquopy 调用：
    PythonEngine.call("aiguide_demo", "greet", "world")
    PythonEngine.call("aiguide_demo", "add", 1, 2)
"""


def greet(name):
    return f"Hello, {name}! —— from embedded Python"


def add(a, b):
    return a + b


def sum_list(items):
    return sum(items)
