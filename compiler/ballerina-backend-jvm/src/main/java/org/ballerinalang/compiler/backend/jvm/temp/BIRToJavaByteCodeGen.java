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
package org.ballerinalang.compiler.backend.jvm.temp;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * BIRToJavaByteCodeGen class.
 */
public class BIRToJavaByteCodeGen extends BIRVisitor {

    private ClassWriter cw;
    private MethodVisitor mv;
    private BIRToJVMIndexMap indexMap;

    public void generateJavaClass(BIRNode.BIRPackage birPackage) {

        birPackage.accept(this);

        //todo: temporarily writing the class file here it self, but it should be moved to binary file writer
        try {
            Files.write(Paths.get("/Users/kishanthan/WSO2/msc/class-files/bir/Test.class"), cw.toByteArray());
        } catch (IOException e) {
            //ignore
        }
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {

        String jvmClassName = birPackageToJVMClass(birPackage);

        cw = new ClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, jvmClassName, null, Type.getInternalName(Object.class), null);
        generateDefaultConstructor(cw);

        birPackage.functions.forEach(birFunction -> birFunction.accept(this));
    }

    private String birPackageToJVMClass(BIRNode.BIRPackage birPackage) {

        return "Test";
    }

    private void generateDefaultConstructor(ClassWriter cw) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {

        indexMap = new BIRToJVMIndexMap();
        String methodDesc = generateJVMMethodDesc(birFunction);
        String methodName = birFunction.name.value;

        mv = this.cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, methodDesc, null, null);
        mv.visitCode();

        // todo visit body

        mv.visitMaxs(100, 400);
        mv.visitEnd();
    }

    @Override
    public void visit(BIRNonTerminator.NewArray newArray) {

    }

    @Override
    public void visit(BIRNonTerminator.ArrayAccess arrayAccess) {

    }

    @Override
    public void visit(BIRNonTerminator.ArrayStore arrayStore) {

    }

    private String generateJVMMethodDesc(BIRNode.BIRFunction birFunction) {

        List<BIRNode.BIRVariableDcl> params = birFunction.localVars;
        //todo fix for default, rest params
        StringBuilder result = new StringBuilder("(");
        for (int i = 1; i < params.size(); i++) {
            BIRNode.BIRVariableDcl param = params.get(i);
            switch (param.type.tag) {
                case TypeTags.INT:
                    result.append("J");
                    indexMap.add(param);
                    break;
                default:
                    //todo other types
                    break;
            }

        }
        if (TypeTags.NIL == params.get(0).type.tag) {
            result.append(")V");
        } else {
            result.append(")Ljava/lang/Object;");
        }
        return result.toString();
    }

    class BIRToJVMIndexMap {

        private int localVarIndex;
        private Map<BIRNode.BIRVariableDcl, Integer> jvmLocalVarIndexMap = new HashMap<>();

        void add(BIRNode.BIRVariableDcl variableDcl) {

            jvmLocalVarIndexMap.put(variableDcl, localVarIndex);
            localVarIndex = localVarIndex + 2;
        }

        int getIndex(BIRNode.BIRVariableDcl variableDcl) {

            if (!(jvmLocalVarIndexMap.containsKey(variableDcl))) {
                return -1;
            }

            return jvmLocalVarIndexMap.get(variableDcl);
        }
    }

}
