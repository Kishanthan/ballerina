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

import org.ballerinalang.jvm.Fibonacci;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;

import java.io.File;
import java.io.PrintStream;

/**
 * Fibonacci benchmark.
 */
public class FibonacciBenchmark extends BaseBenchmark {

    private static final PrintStream console = System.out;

    public void fibonacciTest2() throws Exception {
        String programName = "fibonacci2.bal";
        String functionName = "fib";

        long fibFactor = 35;

        genJVMExecutable(programName);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{fibFactor};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(fibFactor)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    public void fibonacciTest3() throws Exception {
        String programName = "fibonacci3.bal";
        String functionName = "foo";

        execFibonacci(programName, functionName);
    }

    private void execFibonacci(String programName, String functionName) throws Exception {
        String jvmTargetTime = "";
        String bvmTargetTime = "";
        String pureJVMTime = "";

        genJVMExecutable(programName);
        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        Class<?>[] jvmParamSignature = new Class[]{long.class};

        Fibonacci jvmFib = new Fibonacci();

        for (int fibFactor = 1; fibFactor <= 35; fibFactor++) {
            Object[] jvmArgs = new Object[]{fibFactor};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTargetTime = jvmTargetTime.concat(String.valueOf((end - start) / 200)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BInteger(fibFactor)};
            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTargetTime = bvmTargetTime.concat(String.valueOf((end - start) / 200)).concat(",");

            start = System.currentTimeMillis();
            long pureJvmResult = jvmFib.invoke(fibFactor);
            end = System.currentTimeMillis();

            pureJVMTime = pureJVMTime.concat(String.valueOf((end - start) / 200)).concat(",");

            console.println("Fib factor : " + fibFactor + ", JVM Target Time : " + jvmTargetTime);
            console.println("Fib factor : " + fibFactor + ", BVM Target Time : " + bvmTargetTime);
            console.println("Fib factor : " + fibFactor + ", Pure JVM Time : " + pureJVMTime);

            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), pureJvmResult);
        }

        console.println(jvmTargetTime);
        console.println(bvmTargetTime);
        console.println(pureJVMTime);
    }

    void execBenchmark(String programName, String functionName) throws Exception {
        execFibonacci(programName, functionName);
    }
}
