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

import org.ballerinalang.compiler.backend.jvm.JVMCodeGen;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Base class for bir jvm target related benchmark suite.
 */
public abstract class BaseBenchmark {
    private Path targetDir = FileSystems.getDefault().getPath(".").toAbsolutePath();
    Path projectDirPath = FileSystems.getDefault().getPath(".").toAbsolutePath();
    private URLClassLoader urlClassLoader;
    private JVMCodeGen codeGen = JVMCodeGen.getInstance();

    BaseBenchmark() {
        try {
            urlClassLoader = URLClassLoader.newInstance(new URL[]{
                    targetDir.toFile().toURI().toURL()
            });
        } catch (MalformedURLException e) {
            //ignore
        }
    }

    void genJVMExecutable(String functionName) {
        codeGen.genJVMExecutable(projectDirPath, functionName, targetDir);
    }

    Object invokeJVM(String programName, String functionName, Class<?>[] paramSignature, Object[] args)
            throws Exception {
        Class clazz = urlClassLoader.loadClass(programName.substring(0, programName.indexOf(".")));
        Method method = clazz.getDeclaredMethod(functionName, paramSignature);
        return method.invoke(null, args);
    }

    BValue[] invokeBVM(String programName, String functionName, BValue[] bvmArgs) {
        CompileResult result = BCompileUtil.compile(projectDirPath + File.separator + programName);
        return BRunUtil.invoke(result, functionName, bvmArgs);
    }

    BValue[] invokeBVM(CompileResult result, String functionName, BValue[] bvmArgs) {
        return BRunUtil.invoke(result, functionName, bvmArgs);
    }
}
