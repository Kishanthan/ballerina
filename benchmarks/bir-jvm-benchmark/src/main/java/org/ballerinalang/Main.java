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
        Main main = new Main();
        String benchmarkName = args[0];
        String programName = args[1];
        String functionName = args[2];
        main.runBenchmark(Benchmark.valueOf(benchmarkName.toUpperCase()), programName, functionName);
    }

    private void runBenchmark(Benchmark benchmarkName, String programName, String functionName) throws Exception {

        switch (benchmarkName) {
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
            case MATRIX_MUTIPLY:
                MatrixMultiplyBenchmark matrixMultiplyBenchmark = new MatrixMultiplyBenchmark();
                matrixMultiplyBenchmark.execBenchmark(programName, functionName);
                break;
            case STRING_MATCHES:
            case STRING_CONTAINS:
                StringOperationBenchmark stringOperation = new StringOperationBenchmark();
                stringOperation.execBenchmark(programName, functionName);
                break;
            default:
                throw new UnsupportedOperationException("Benchmark " + benchmarkName + " not supported");
        }
    }

    enum Benchmark {

        MERGESORT("mergesort"),
        QUICKSORT("quicksort"),
        FIBONACCI("fibonacci"),
        MATRIX_MUTIPLY("matrix_mutliply"),
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
