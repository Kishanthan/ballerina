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
package org.ballerinalang.nativeimpl.jvm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

class JVMCodeGenUtil {

    private ClassWriter classWriter;
    private MethodVisitor methodVisitor;
    private Map<String, Label> labels = new HashMap<>();

    private JVMCodeGenUtil() {
        this.classWriter = new ClassWriter(COMPUTE_FRAMES);
    }


    private static JVMCodeGenUtil jvmCodeGenUtil = new JVMCodeGenUtil();

    static JVMCodeGenUtil getInstance() {
        return jvmCodeGenUtil;
    }

    ClassWriter getClassWriter() {
        return classWriter;
    }

    void setClassWriter(ClassWriter classWriter) {
        this.classWriter = classWriter;
    }

    MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    void setMethodVisitor(MethodVisitor methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    void addLabel(String labelId, Label label) {
        labels.put(labelId, label);
    }

    Label getLabel(String labelId) {
        if (!(labels.containsKey(labelId))) {
            labels.put(labelId, new Label());
        }
        return labels.get(labelId);
    }

    void clean() {
        classWriter = new ClassWriter(COMPUTE_FRAMES);
        labels = new HashMap<>();
    }
}
