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

import org.ballerinalang.jvm.MatrixMultiply;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;

import java.io.File;
import java.io.PrintStream;
import java.util.Random;

/**
 * Matric multiply benchmark.
 */
public class MatrixMultiplyBenchmark extends BaseBenchmark {

    private static final PrintStream console = System.out;

    public void matrixMultiplyTest2() throws Exception {

        String programName = "matMultiply2.bal";

        String jvmTime = "";
        String bvmTime = "";

        long[][] a = new long[][]{{1, 2, 3, 4}, {3, 4, 5, 6}, {6, 7, 8, 9}, {1, 2, 3, 4}};
        long[][] b = new long[][]{{1, 2, 3, 4}, {3, 4, 5, 6}, {6, 7, 8, 9}, {1, 2, 3, 4}};

        genJVMExecutable(programName);
        Class<?>[] jvmParamSignature = new Class[]{long[][].class, long[][].class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);
        Object[] jvmArgs = new Object[]{a, b};
        long start = System.currentTimeMillis();
        Object jvmResult = invokeJVM(programName, "matrixMultiply", jvmParamSignature, jvmArgs);
        long end = System.currentTimeMillis();

        jvmTime = jvmTime.concat(String.valueOf(end - start)).concat(",");

        BValue[] bvmArgs = new BValue[]{};

        start = System.currentTimeMillis();
        BValue[] bvmResult = invokeBVM(result, "foo", bvmArgs);
        end = System.currentTimeMillis();

        bvmTime = bvmTime.concat(String.valueOf(end - start)).concat(",");

        console.println(jvmTime);
        console.println(bvmTime);
    }

    public void matrixMultiplyTest3() throws Exception {

        String programName = "matMultiply3.bal";

        String jvmTime = "";
        String bvmTime = "";

        long[][] a = new long[][]{{1, 2, 3, 4, 1, 2, 3, 4}, {3, 4, 5, 6, 3, 4, 5, 6}, {6, 7, 8, 9, 6, 7, 8, 9},
                {1, 2, 3, 4, 1, 2, 3, 4}, {1, 2, 3, 4, 1, 2, 3, 4}, {3, 4, 5, 6, 3, 4, 5, 6}, {6, 7, 8, 9, 6, 7, 8, 9},
                {1, 2, 3, 4, 1, 2, 3, 4}};

        long[][] b = new long[][]{{1, 2, 3, 4, 1, 2, 3, 4}, {3, 4, 5, 6, 3, 4, 5, 6}, {6, 7, 8, 9, 6, 7, 8, 9},
                {1, 2, 3, 4, 1, 2, 3, 4}, {1, 2, 3, 4, 1, 2, 3, 4}, {3, 4, 5, 6, 3, 4, 5, 6}, {6, 7, 8, 9, 6, 7, 8, 9},
                {1, 2, 3, 4, 1, 2, 3, 4}};

        genJVMExecutable(programName);
        Class<?>[] jvmParamSignature = new Class[]{long[][].class, long[][].class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);
        Object[] jvmArgs = new Object[]{a, b};
        long start = System.currentTimeMillis();
        Object jvmResult = invokeJVM(programName, "matrixMultiply", jvmParamSignature, jvmArgs);
        long end = System.currentTimeMillis();

        jvmTime = jvmTime.concat(String.valueOf(end - start)).concat(",");

        BValue[] bvmArgs = new BValue[]{};

        start = System.currentTimeMillis();
        BValue[] bvmResult = invokeBVM(result, "foo", bvmArgs);
        end = System.currentTimeMillis();

        bvmTime = bvmTime.concat(String.valueOf(end - start)).concat(",");

        console.println(jvmTime);
        console.println(bvmTime);
    }

    public void matrixMultiplyTestN() throws Exception {

        String programNameJVM = "matMultiplyNJVM.bal";
        String programNameBVM = "matMultiplyNBVM.bal";
        String functionName = "foo";

        MatrixMultiply matrixMultiply = new MatrixMultiply();

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programNameBVM);

        genJVMExecutable(programNameJVM);
        Class<?>[] jvmParamSignature = new Class[]{long[][].class, long[][].class};

        String jvmTime = "";
        String bvmTime = "";
        String pureJVMTime = "";

        Random generator = new Random();

        for (int size = 2; size < 1000; size++) {
            long[][] a = new long[size][size];
            long[][] b = new long[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    a[i][j] = generator.nextLong();
                    b[i][j] = generator.nextLong();
                }
            }

            Object[] jvmArgs = new Object[]{a, b};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programNameJVM, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf((end - start) / 50)).concat(",");

            BRefValueArray intArrayOfArrayA = new BRefValueArray(new BArrayType(new BArrayType(BTypes.typeInt)));
            BRefValueArray intArrayOfArrayB = new BRefValueArray(new BArrayType(new BArrayType(BTypes.typeInt)));
            for (int i = 0; i < size; i++) {
                intArrayOfArrayA.add(i, new BIntArray(a[i]));
                intArrayOfArrayB.add(i, new BIntArray(b[i]));
            }

            BValue[] bvmArgs = new BValue[]{intArrayOfArrayA, intArrayOfArrayB};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();
            bvmTime = bvmTime.concat(String.valueOf((end - start) / 50)).concat(",");

            start = System.currentTimeMillis();
            long[][] pureJVMResult = matrixMultiply.exec(a, b);
            end = System.currentTimeMillis();
            pureJVMTime = pureJVMTime.concat(String.valueOf((end - start) / 50)).concat(",");

            console.println("JVM : matrix size " + size + " time : " + jvmTime);
            console.println("BVM : matrix size " + size + " time : " + bvmTime);
            console.println("Pure JVM : matrix size " + size + " time : " + pureJVMTime);
        }

        console.println(jvmTime);
        console.println(bvmTime);
        console.println(pureJVMTime);
    }

    void execBenchmark(String programName, String functionName) throws Exception {
        matrixMultiplyTestN();
    }
}
