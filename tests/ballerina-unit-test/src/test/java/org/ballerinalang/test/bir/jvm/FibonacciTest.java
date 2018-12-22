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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.PrintStream;

public class FibonacciTest extends BaseTest {

    private static final PrintStream console = System.out;

    @Test
    public void fibonacciTest2() throws Exception {
        String programName = "fibonacci2.bal";
        String functionName = "fib";

        long fibFactor = 35;

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{fibFactor};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(fibFactor)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void fibonacciTest3() throws Exception {
        String programName = "fibonacci3.bal";
        String functionName = "foo";

        String jvmTime = "";
        String bvmTime = "";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        Class<?>[] jvmParamSignature = new Class[]{long.class};

        for (int fibFactor = 30; fibFactor <= 35; fibFactor++) {
            Object[] jvmArgs = new Object[]{fibFactor};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf((end - start) / 100)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BInteger(fibFactor)};
            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf((end - start) / 100)).concat(",");

            console.println("Fib factor : " + fibFactor + ", Time : " + jvmTime);
            console.println("Fib factor : " + fibFactor + ", Time : " + bvmTime);

            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
        }

        console.println(jvmTime);
        console.println(bvmTime);
    }
}
