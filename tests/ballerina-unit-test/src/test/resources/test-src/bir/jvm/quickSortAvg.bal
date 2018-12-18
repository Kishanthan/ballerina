function partition(int[] arr, int low, int high) returns int {
    int pivot = arr[high];
    int i = low - 1;
    int j = low;

    while (j < high) {
        if (arr[j] <= pivot) {
            i = i + 1;

            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        j = j + 1;
    }

    int temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;

    return i + 1;
}

function sort(int[] arr, int low, int high) returns int[]{

    if (low < high) {
        int pi = partition(arr, low, high);

        int[] lSorted = sort(arr, low, pi - 1);
        int[] rSorted = sort(arr, pi + 1, high);
    }

    return arr;
}

function quickSort(int[] array) returns int[] {
    int length = lengthof array;
    int[] sorted = sort(array, 0, length - 1);
    return sorted;
}

function foo(int[] array) returns int[] {
    int i = 0;
    int[] result = [];
    while (i < 100) {
       	result = quickSort(array);
	    i = i + 1;
    }
    return result;
}
