function foo(string input, int loopTimes) returns string {
    int i = 0;
    string s = "Default";
    while (i < loopTimes) {
        s = s + input;
        i = i + 1;
    }

    return s;
}
