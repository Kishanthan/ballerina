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

import java.io.PrintStream;

/**
 * Main executable class for bench mark suite.
 */
public class Main {
    private static final PrintStream console = System.out;
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
                Basic basicBenchmark = new Basic();
                basicBenchmark.warmup();

                MatrixMultiplyBenchmark matrixMultiplyBenchmark = new MatrixMultiplyBenchmark();
                matrixMultiplyBenchmark.execBenchmark(programName, functionName);
                break;
            case BINARY_SEARCH:
                BinarySearchBenchmark binarySearchBenchmark = new BinarySearchBenchmark();
                binarySearchBenchmark.execBenchmark(programName, functionName);
                break;
            case STRING_MATCHES:
                String regex = args[3];
                StringOperationBenchmark stringOperation = new StringOperationBenchmark();
                stringOperation.execStringMatchesBenchmark(programName, functionName, regex);
                break;
            case STRING_CONTAINS:
                stringOperation = new StringOperationBenchmark();
                stringOperation.execStringContainsBenchmark(programName, functionName);
                break;
            case ALL:
                console.println("############################ START ############################");

                basicBenchmark = new Basic();
                basicBenchmark.warmup();

                functionName = "foo";
                mergeSortBenchmark = new MergeSortBenchmark();
                try {
                    mergeSortBenchmark.execBenchmark("mergeSortAvg.bal", functionName);
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }
                fibonacciBenchmark = new FibonacciBenchmark();
                try {
                    fibonacciBenchmark.execBenchmark("fibonacci3.bal", functionName);
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }
                matrixMultiplyBenchmark = new MatrixMultiplyBenchmark();
                try {
                    matrixMultiplyBenchmark.execBenchmark("mat.bal", functionName);
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }
                stringOperation = new StringOperationBenchmark();
                try {
                    stringOperation.execStringMatchesBenchmark("string4.bal", functionName,
                            "([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)");
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }
                try {
                    stringOperation.execStringMatchesBenchmark("string4.bal", functionName,
                            "([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)|([^ @]+)@([^ @]+)");
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }
                try {
                    stringOperation.execStringContainsBenchmark("string3.bal", functionName);
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }

                try {
                    quickSortBenchmark = new QuickSortBenchmark();
                    quickSortBenchmark.execBenchmark("quickSortAvg.bal", functionName);
                } catch (Throwable e) {
                    console.println(e);
                    //continue
                }

                console.println("############################ END ############################");
                break;
            default:
                throw new UnsupportedOperationException("Benchmark " + benchmark + " not supported");
        }

        System.exit(0);
    }

    enum Benchmark {

        MERGESORT("mergesort"),
        QUICKSORT("quicksort"),
        FIBONACCI("fibonacci"),
        BINARY_SEARCH("binary_search"),
        MATRIX_MULTIPLY("matrix_multiply"),
        STRING_MATCHES("string_matches"),
        STRING_CONTAINS("string_contains"),
        ALL("all");

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
