function foo(int[] array) returns int[] {
    int length = lengthof array;

    if (length < 2) {
        return array;
    }
    int mid = length / 2;
    
    int[] rhs = slice(array, mid, length);
 
    return rhs;
}

function slice(int[] array, int sliceStart, int sliceEnd) returns int[] {
    int arrayLength = sliceEnd - sliceStart;
    int[] sliced;
    int i = 0;
    int j = sliceStart;
    while (j < sliceEnd) {
        sliced[i] = array[j];
        i = i + 1;
        j = j + 1;
    }

    return sliced;
}
