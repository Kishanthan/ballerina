function foo(int[] arr) returns int {
    return bar(arr);
}

function bar(int[] array) returns int {
    int i = 0;
    int sum = 0;
    while (i < lengthof array) {
        sum += array[i];
        i++;
    }
    return sum;
}
