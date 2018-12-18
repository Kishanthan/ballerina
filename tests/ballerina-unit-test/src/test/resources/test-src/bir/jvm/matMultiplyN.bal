function matrixMultiply(int[][] a, int[][] b) returns int[][] {
    int rowsA = lengthof a;
    int colsA = lengthof a[0];
    int colsB = lengthof b[0];

    int[][] result = [];
    int i = 0;
    while (i < rowsA) {
        result[i] = [];
        int j = 0;
        while (j < colsB) {
            int k = 0;
            result[i][j] = 0;
            while (k < colsA) {
                result[i][j] = result[i][j] + (a[i][k] * b[k][j]);
                k = k + 1;
            }
            j = j + 1;
        }
        i = i + 1;
    }
    return result;
}
