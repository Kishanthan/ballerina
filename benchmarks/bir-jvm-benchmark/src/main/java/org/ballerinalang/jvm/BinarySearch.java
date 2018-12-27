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
 * Java based impl for binary search.
 */
public class BinarySearch {
    public long exec(long[] array, long x) {
        int n = array.length;
        return v2(array, 0, n - 1, x);
    }

    long binarySearch(long[] arr, int l, int r, long x) {
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

    long v2(long[] arr, int first, int last, long key) {
        if (last < first) {
            return -1;
        }
        int mid = first + (last - first) / 2;
        if (arr[mid] == key) {
            return mid;
        }
        if (arr[mid] > key) {
            return v2(arr, first, mid - 1, key);
        } else {
            return v2(arr, mid + 1, last, key);
        }
    }
}
