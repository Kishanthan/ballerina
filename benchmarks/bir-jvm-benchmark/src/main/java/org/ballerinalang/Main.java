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

package org.ballerinalang;

/**
 * Main executable class for bench mark suite.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String benchmarkName = args[0];
        String programName = args[1];
        String functionName = args[2];
        Benchmark benchmark = Benchmark.valueOf(benchmarkName.toUpperCase());
        switch (benchmark) {
            case MERGESORT:
                MergeSortBenchmark mergeSortBenchmark = new MergeSortBenchmark();
                mergeSortBenchmark.execBenchmark(programName, functionName);
                break;
            case FIBONACCI:
                FibonacciBenchmark fibonacciBenchmark = new FibonacciBenchmark();
                fibonacciBenchmark.execBenchmark(programName, functionName);
                break;
            case QUICKSORT:
                QuickSortBenchmark quickSortBenchmark = new QuickSortBenchmark();
                quickSortBenchmark.execBenchmark(programName, functionName);
                break;
            case MATRIX_MULTIPLY:
                MatrixMultiplyBenchmark matrixMultiplyBenchmark = new MatrixMultiplyBenchmark();
                matrixMultiplyBenchmark.execBenchmark(programName, functionName);
                break;
            case BINARY_SEARCH:
                BinarySearchBenchmark binarySearchBenchmark = new BinarySearchBenchmark();
                binarySearchBenchmark.execBenchmark(programName, functionName);
                break;
            case STRING_MATCHES:
                StringOperationBenchmark stringOperation = new StringOperationBenchmark();
                stringOperation.execStringMatchesBenchmark(programName, functionName);
                break;
            case STRING_CONTAINS:
                stringOperation = new StringOperationBenchmark();
                stringOperation.execStringContainsBenchmark(programName, functionName);
                break;
            default:
                throw new UnsupportedOperationException("Benchmark " + benchmark + " not supported");
        }
    }

    enum Benchmark {

        MERGESORT("mergesort"),
        QUICKSORT("quicksort"),
        FIBONACCI("fibonacci"),
        BINARY_SEARCH("binary_search"),
        MATRIX_MULTIPLY("matrix_multiply"),
        STRING_MATCHES("string_matches"),
        STRING_CONTAINS("string_contains");

        final String benchmarkName;

        Benchmark(String benchmarkName) {
            this.benchmarkName = benchmarkName;
        }


        @Override
        public String toString() {
            return benchmarkName;
        }
    }
}
