def add(a, b):
    return a + b

def fib(n: int):
    a, b = 0, 1
    out = []
    for _ in range(n):
        out.append(a)
        a, b = b, a + b
    return out
