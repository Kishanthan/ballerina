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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringTest extends BaseTest {

    private static final PrintStream console = System.out;

    @Test
    public void stringContainsTest() throws Exception {

        String programName = "string3.bal";
        String functionName = "foo";

        String jvmTime = "";
        String bvmTime = "";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{String[].class, String.class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        int[] loops = new int[]{1000, 10000, 100000, 200000, 300000, 400000, 500000, 1000000, 2000000, 3000000, 4000000,
                5000000};

        for (int size : loops) {
            String[] array = new String[size];
            String textToFind = "Hello";

            for (int i = 0; i < size; i++) {
                byte[] byteArray = new byte[10];
                new Random().nextBytes(byteArray);
                String generatedString = new String(byteArray, Charset.forName("UTF-8"));
                array[i] = generatedString;
                if (i == (size - 1)) {
                    array[i] = generatedString.concat(textToFind);
                }
            }
            Object[] jvmArgs = new Object[]{array, textToFind};
            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf((end - start) / 100)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BStringArray(array), new BString(textToFind)};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf((end - start) / 100)).concat(",");

            console.println("Array size : " + size + ", JVM Time : " + jvmTime);
            console.println("Array size : " + size + ", BVM Time : " + bvmTime);

            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
        }

        console.println(jvmTime);
        console.println(bvmTime);
    }

    @Test
    public void stringMatchesTest1() throws Exception {

        matchRegex("([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)");
    }

    @Test
    public void stringMatchesTest2() throws Exception {

        matchRegex("([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)|([^ @]+)@([^ @]+)");
    }

    private void matchRegex(String regex) throws Exception {

        String programName = "string4.bal";
        String functionName = "foo";

        String jvmTime = "";
        String bvmTime = "";

        codeGen.genJVMExecutable(projectDirPath, programName, targetDir);
        Class<?>[] jvmParamSignature = new Class[]{String[].class, String.class};

        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);

        BufferedReader abc = new BufferedReader(new FileReader(projectDirPath + File.separator + "sampleText1.txt"));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = abc.readLine()) != null) {
            lines.add(line);
        }
        abc.close();

        String[] array = lines.toArray(new String[]{});
        int initialLength = array.length;

        for (int i = 2; i < 10; i++) {

            String[] largerArray = new String[initialLength + array.length];
            System.arraycopy(array, 0, largerArray, 0, array.length);
            System.arraycopy(array, 0, largerArray, array.length, initialLength);

            Object[] jvmArgs = new Object[]{largerArray, regex};

            long start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            long end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf((end - start) / 10)).concat(",");

            BValue[] bvmArgs = new BValue[]{new BStringArray(largerArray), new BString(regex)};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf((end - start) / 10)).concat(",");

            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);

            console.println("Array size : " + i * initialLength + ", JVM Time : " + jvmTime);
            console.println("Array size : " + i * initialLength + ", BVM Time : " + bvmTime);

            array = largerArray;
        }

        console.println(jvmTime);
        console.println(bvmTime);
    }
}
