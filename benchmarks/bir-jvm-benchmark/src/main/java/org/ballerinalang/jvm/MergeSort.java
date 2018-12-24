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
 * Java based impl for merge sort functionality.
 */
public class MergeSort {

    public long[] exec(long[] array) {
        int i = 0;
        long[] result = new long[]{};
        while (i < 100) {
            result = mergeSort(array);
            i = i + 1;
        }
        return result;
    }

    long[] mergeSort(long[] array) {
        int length = array.length;

        if (length < 2) {
            return array;
        }
        int mid = length / 2;

        long[] lhs = slice(array, 0, mid);
        long[] rhs = slice(array, mid, length);

        lhs = mergeSort(lhs);
        rhs = mergeSort(rhs);

        return merge(lhs, rhs);
    }

    long[] merge(long[] lhs, long[] rhs) {
        int i = 0;
        int j = 0;
        int k = 0;

        int lhsSize = lhs.length;
        int rhsSize = rhs.length;

        int arrayLength = lhsSize + rhsSize;
        long[] array = new long[arrayLength];

        while (i < lhsSize && j < rhsSize) {
            if (lhs[i] < rhs[j]) {
                array[k] = lhs[i];
                i = i + 1;
            } else {
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

    long[] slice(long[] array, int sliceStart, int sliceEnd) {
        int arrayLength = sliceEnd - sliceStart;
        long[] sliced = new long[arrayLength];
        int i = 0;
        int j = sliceStart;
        while (j < sliceEnd) {
            sliced[i] = array[j];
            i = i + 1;
            j = j + 1;
        }

        return sliced;
    }

}
