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

import org.ballerinalang.jvm.StringOperation;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * String operations (matches, contains) benchmark.
 */
public class StringOperationBenchmark extends BaseBenchmark {

    private static final PrintStream console = System.out;

    public void stringContainsTest() throws Exception {

        stringContains("string3.bal", "foo");
    }

    private void stringContains(String programName, String functionName) throws Exception {
        String jvmTime = "";
        String bvmTime = "";
        String pureJVMTime = "";

        genJVMExecutable(programName);

        StringOperation stringOperation = new StringOperation();
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

            start = System.currentTimeMillis();
            long pureJVMResult = stringOperation.execStringContains(array, textToFind);
            end = System.currentTimeMillis();

            pureJVMTime = pureJVMTime.concat(String.valueOf((end - start) / 100)).concat(",");

            console.println("Array size : " + size + ", JVM Target Time : " + jvmTime);
            console.println("Array size : " + size + ", BVM Target Time : " + bvmTime);
            console.println("Array size : " + size + ", Pure JVM Time : " + pureJVMTime);

            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
            Assert.assertEquals(pureJVMResult, (long) jvmResult);
        }

        console.println(jvmTime);
        console.println(bvmTime);
        console.println(pureJVMTime);
    }

    public void stringMatchesTest1() throws Exception {

        matchRegex("string4.bal", "foo", "([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)");
    }

    public void stringMatchesTest2() throws Exception {

        matchRegex("string4.bal", "foo", "([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)|([^ @]+)@([^ @]+)");
    }

    private void matchRegex(String programName, String functionName, String regex) throws Exception {

        String jvmTime = "";
        String bvmTime = "";
        String pureJVMTime = "";

        genJVMExecutable(programName);

        StringOperation stringOperation = new StringOperation();

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
            long pureJVMResult = stringOperation.execStringMatch(largerArray, regex);
            long end = System.currentTimeMillis();
            pureJVMTime = pureJVMTime.concat(String.valueOf((end - start) / 10)).concat(",");
            console.println("Array size : " + i * initialLength + ", Pure JVM Time : " + pureJVMTime);

            start = System.currentTimeMillis();
            Object jvmResult = invokeJVM(programName, functionName, jvmParamSignature, jvmArgs);
            end = System.currentTimeMillis();

            jvmTime = jvmTime.concat(String.valueOf((end - start) / 10)).concat(",");
            console.println("Array size : " + i * initialLength + ", JVM Time : " + jvmTime);

            BValue[] bvmArgs = new BValue[]{new BStringArray(largerArray), new BString(regex)};

            start = System.currentTimeMillis();
            BValue[] bvmResult = invokeBVM(result, functionName, bvmArgs);
            end = System.currentTimeMillis();

            bvmTime = bvmTime.concat(String.valueOf((end - start) / 10)).concat(",");
            console.println("Array size : " + i * initialLength + ", BVM Time : " + bvmTime);

            Assert.assertEquals(((BInteger) bvmResult[0]).intValue(), (long) jvmResult);
            Assert.assertEquals(pureJVMResult, (long) jvmResult);

            array = largerArray;
        }

        console.println(jvmTime);
        console.println(bvmTime);
        console.println(pureJVMTime);
    }


    void execStringMatchesBenchmark(String programName, String functionName) throws Exception {
        String regex = "([a-zA-Z][a-zA-Z0-9]*)://([^ /]+)(/?[^ ]*)";
        matchRegex(programName, functionName, regex);
    }


    void execStringContainsBenchmark(String programName, String functionName) throws Exception {
        stringContains(programName, functionName);
    }
}
