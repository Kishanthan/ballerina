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
package org.ballerinalang.test.bir.jvm;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Random;

public class BinarySearchTest extends BaseTest {

    @Test
    public void binarySearchTest1() throws Exception {
        String programName = "binarySearch.bal";
        String functionName = "foo";

        String jvmTime = "";
        String bvmTime = "";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir.toAbsolutePath().toString());
        Class<?>[] jvmParamSignature = new Class[]{long[].class, long.class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        int[] loops = new int[]{1000, 5000, 10000, 50000, 100000, 200000, 300000, 400000, 500000, 1000000};

        for (int size : loops) {
            long[] array = new long[size];

            Random generator = new Random();

            long noToFind = 0;

            for (int i = 0; i < size; i++) {
                array[i] = generator.nextLong();

                if (i == (size - 1)) {
                    noToFind = array[i];
                }
            }

            Object[] jvmArgs = new Object[]{array, noToFind};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf(end - start)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BIntArray(array), new BInteger(noToFind)};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf(end - start)).concat(",");

            System.out.println("Array size : " + size + ", JVM Time : " + jvmTime);
            System.out.println("Array size : " + size + ", BVM Time : " + bvmTime);

            System.out.println("Index : BVM - " + ((BInteger)bvmResult[0]).intValue() + " JVM - " + jvmResult);
        }

        System.out.println(jvmTime);
        System.out.println(bvmTime);
    }

    @Test
    public void quickSortTest2() throws Exception {
        String programName = "quickSortAvg.bal";
        String functionName = "foo";

        String jvmTime = "";
        String bvmTime = "";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir.toAbsolutePath().toString());
        Class<?>[] jvmParamSignature = new Class[]{long[].class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        int[] loops = new int[]{1000, 2000, 3000, 4000, 5000, 10000, 20000, 30000, 40000, 50000, 100000, 200000,
                300000, 400000, 500000};

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

            System.out.println("Array size : " + size + ", JVM Time : " + jvmTime);
            System.out.println("Array size : " + size + ", BVM Time : " + bvmTime);
        }

        System.out.println(jvmTime);
        System.out.println(bvmTime);
    }
}
