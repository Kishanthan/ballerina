function foo(int n) returns int {
    int i = 0;
    int result = 0;
    int limitVal = 10;

    while (i < limitVal) {
        result = fib(n);
        i = i + 1;
    }

    return result;
}


function fib(int n) returns int {
  if (n == 1 || n == 2) {
    return 1;
  } else {
    return fib(n - 1) + fib (n - 2);
  }
}

