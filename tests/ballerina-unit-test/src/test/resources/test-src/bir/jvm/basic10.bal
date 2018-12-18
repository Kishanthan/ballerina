function foo(int[] array) returns int[] {
    int i = 0;
    int length = lengthof array;
    int[] myArr = [];
    while (i < length) {
        myArr[i] = array[i];
        i = i + 1;
    }
    return myArr;
}

