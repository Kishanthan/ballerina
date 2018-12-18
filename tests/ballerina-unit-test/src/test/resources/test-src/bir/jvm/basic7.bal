function foo(int a) returns int {
    int i = 0;
    int b;
    while (i < 10) {
       b = fib(a);
       i++;
    }
    return b;
}

function fib(int a) returns int {
    if (a == 1) {
        return 1;
    }

    if (a == 2) {
        return 1;
    }
 
    return fib(a - 1) + fib(a - 2);
}
