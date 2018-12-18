function mergeSort(int[] array) returns int[] {
    int length = lengthof array;

    if (length < 2) {
        return array;
    }
    int mid = length / 2;
    
    int[] lhs = slice(array, 0, mid);
    int[] rhs = slice(array, mid, length);

    lhs = mergeSort(lhs);
    rhs = mergeSort(rhs);
 
    return merge(lhs, rhs);
}

function merge(int[] lhs, int[] rhs) returns int[] {
    int i = 0;
    int j = 0;
    int k = 0;

    int lhsSize = lengthof lhs;
    int rhsSize = lengthof rhs;

    int arrayLength = lhsSize + rhsSize; 
    int[] array;

    while (i < lhsSize && j < rhsSize) {
        if (lhs[i] < rhs[j]) {
            array[k] = lhs[i];
            i = i + 1;
        }
        else {
            array[k] = rhs[j];
            j = j + 1;
        }
        k = k + 1;
    }

    while (i < lhsSize) {
        array[k] = lhs[i];
        k = k + 1;
        i = i + 1;
    }

    while (j < rhsSize) {
        array[k] = rhs[j];
        k = k + 1;
        j = j + 1;
    }

    return array;
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
