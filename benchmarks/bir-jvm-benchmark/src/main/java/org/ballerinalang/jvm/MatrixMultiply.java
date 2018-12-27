/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm;

/**
 * Java based impl for matrix multiplication.
 */
public class MatrixMultiply {
    public long[][] exec(long[][] a, long[][] b) {
        long i = 0;
        long[][] result = new long[][]{};
        while (i < 100) {
            result = matrixMultiply(a, b);
            i = i + 1;
        }

        return result;
    }

    long[][] matrixMultiply(long[][] a, long[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        long[][] result = new long[rowsA][colsA];
        int i = 0;
        while (i < rowsA) {
            result[i] = new long[rowsA];
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

}
