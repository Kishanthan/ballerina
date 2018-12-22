function foo(int[] array, int x) returns int {
    int i = 0;
    int result = 0;
    int n = lengthof array;
    while (i < 100) {
        result = binarySearch(array, 0, n - 1, x);
        i = i + 1;
    }

    return result;
}

function binarySearch(int[] arr, int l, int r, int x) returns int {
    if (r < l) {
        return -1;
    }
    int mid = l + (r - l) / 2;

    if (arr[mid] == x) {
        return mid;
    }

    if (x < arr[mid]) {
        return binarySearch(arr, l, mid - 1, x);
    }

    return binarySearch(arr, mid + 1, r, x);
}

