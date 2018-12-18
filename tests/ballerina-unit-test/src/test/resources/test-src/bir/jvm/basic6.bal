function foo(int a) returns int {
    if (a == 1) {
        return 1;
    }

    if (a == 2) {
        return 1;
    }
 
    return foo(a - 1) + foo(a - 2);
}
