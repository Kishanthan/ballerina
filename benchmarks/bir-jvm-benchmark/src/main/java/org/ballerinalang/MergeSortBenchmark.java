/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang;

import org.ballerinalang.jvm.MergeSort;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BValue;

import java.io.File;
import java.io.PrintStream;
import java.util.Random;

/**
 * Merge sort benchmark.
 */
public class MergeSortBenchmark extends BaseBenchmark {

    private static final PrintStream console = System.out;

    public void mergeSortTest1() throws Exception {
        String programName = "mergeSort.bal";
        String functionName = "mergeSort";

        String jvmTime = "";
        String bvmTime = "";

        genJVMExecutable(programName);
        Class<?>[] jvmParamSignature = new Class[]{long[].class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        int[] loops = new int[]{1000, 10000, 100000, 1000000};

        for (int size : loops) {
            long[] array = new long[size];

            Random generator = new Random();

            for (int i = 0; i < size; i++) {
                array[i] = generator.nextLong();
            }

            Object[] jvmArgs = new Object[]{array};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf(end - start)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BIntArray(array)};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf(end - start)).concat(",");

            console.println("Array size : " + size + ", JVM Time : " + jvmTime);
            console.println("Array size : " + size + ", BVM Time : " + bvmTime);
        }

        console.println(jvmTime);
        console.println(bvmTime);
    }

    public void mergeSortTest2() throws Exception {
        String programName = "mergeSortAvg.bal";
        String functionName = "foo";

        execMergeSort(programName, functionName);
    }

    public void execMergeSort(String programName, String functionName) throws Exception {
        String jvmTime = "";
        String bvmTime = "";
        String pureJVMTime = "";

        genJVMExecutable(programName);
        Class<?>[] jvmParamSignature = new Class[]{long[].class};

        MergeSort mergeSort = new MergeSort();

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        int[] loops = new int[]{1000, 5000, 10000, 50000, 100000, 200000, 300000, 400000, 500000, 1000000};

        for (int size : loops) {
            long[] array = new long[size];

            Random generator = new Random();

            for (int i = 0; i < size; i++) {
                array[i] = generator.nextLong();
            }

            Object[] jvmArgs = new Object[]{array};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf((end - start) / 100)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BIntArray(array)};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf((end - start) / 100)).concat(",");

            start = System.currentTimeMillis();
            long[] pureJVMResult = mergeSort.exec(array);
            end = System.currentTimeMillis();

            pureJVMTime = pureJVMTime.concat(String.valueOf((end - start) / 100)).concat(",");

            console.println("Array size : " + size + ", JVM Target Time : " + jvmTime);
            console.println("Array size : " + size + ", BVM Target Time : " + bvmTime);
            console.println("Array size : " + size + ", Pure JVM Time : " + pureJVMTime);
        }

        console.println(jvmTime);
        console.println(bvmTime);
        console.println(pureJVMTime);
    }

    void execBenchmark(String programName, String functionName) throws Exception {
        execMergeSort(programName, functionName);
    }
}
