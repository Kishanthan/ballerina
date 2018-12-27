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
 * Java based impl for quick sort functionality.
 */
public class QuickSort {
    long partition(long[] arr, int low, int high) {
        long pivot = arr[high];
        int i = low - 1;
        int j = low;

        while (j < high) {
            if (arr[j] <= pivot) {
                i = i + 1;

                long temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
            j = j + 1;
        }

        long temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    long[] sort(long[] arr, long low, long high) {

        if (low < high) {
            long pi = partition(arr, (int) low, (int) high);

            sort(arr, low, pi - 1);
            sort(arr, pi + 1, high);
        }

        return arr;
    }

    long[] quickSort(long[] array) {
        long length = array.length;
        long[] sorted = sort(array, 0, length - 1);
        return sorted;
    }

    public long[] exec(long[] array) {
        int i = 0;
        long[] result = new long[]{};
        while (i < 100) {
            result = quickSort(array);
            i = i + 1;
        }
        return result;
    }

}
