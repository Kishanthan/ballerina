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

import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicTest extends BaseTest {

    @Test
    public void basicTest0() throws Exception {

        String programName = "basic0.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, new Object[]{});

        BValue[] bvmArgs = new BValue[]{};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest1() throws Exception {

        String programName = "basic1.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest2() throws Exception {

        String programName = "basic2.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest3() throws Exception {

        String programName = "basic3.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest4() throws Exception {

        String programName = "basic4.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest5() throws Exception {

        String programName = "basic5.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest6() throws Exception {

        String programName = "basic6.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest7() throws Exception {

        String programName = "basic7.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest8() throws Exception {

        String programName = "basic8.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long[].class};
        Object[] jvmArgs = new Object[]{new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9}};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BIntArray(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9})};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest9() throws Exception {

        String programName = "basic9.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long[].class};
        Object[] jvmArgs = new Object[]{new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9}};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BIntArray(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9})};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest10() throws Exception {

        String programName = "basic10.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long[].class};
        long[] in = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        Object[] jvmArgs = new Object[]{in};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
        long[] out = (long[]) jvmResult;
        Assert.assertEquals(in, out);
    }

    @Test
    public void basicTest11() throws Exception {

        String programName = "basic11.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class, long.class};
        Object[] jvmArgs = new Object[]{2, 3};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(2), new BInteger(3)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);
        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest12() throws Exception {

        String programName = "basic12.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long[].class};
        Object[] jvmArgs = new Object[]{new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9}};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
        Assert.assertEquals(new long[]{5, 6, 7, 8, 9}, (long[]) jvmResult);
    }

    @Test
    public void basicTest13() throws Exception {

        String programName = "basic13.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class, String.class};
        Object[] jvmArgs = new Object[]{34, "Hello"};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(34), new BString("Hello")};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(bvmResult[0].stringValue(), jvmResult);
    }

    @Test
    public void basicTest16() throws Exception {

        String programName = "basic16.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest17() throws Exception {

        String programName = "basic17.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest18() throws Exception {

        String programName = "basic18.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest19() throws Exception {

        String programName = "basic19.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }

    @Test
    public void basicTest22() throws Exception {

        String programName = "basic22.bal";
        String functionName = "foo";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{long.class};
        Object[] jvmArgs = new Object[]{12};
        Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);

        BValue[] bvmArgs = new BValue[]{new BInteger(12)};
        BValue[] bvmResult = invokeBVM(programName, functionName, bvmArgs);

        Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
    }
}

