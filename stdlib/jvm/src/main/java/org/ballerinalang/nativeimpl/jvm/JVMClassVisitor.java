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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * Native class for jvm java class byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "classVisit",
        args = {
                @Argument(name = "visitType", type = STRING),
                @Argument(name = "args", type = ARRAY, elementType = STRING),
        }
)
public class JVMClassVisitor extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        ClassWriter cw = JVMCodeGenUtil.getInstance().getClassWriter();
        String type = context.getStringArgument(0);
        BStringArray args = (BStringArray) context.getRefArgument(0);

        switch (CWVisitType.valueOf(type.toUpperCase())) {
            case INIT:
                String jvmClassName = args.get(0);
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, jvmClassName, null, Type.getInternalName(Object.class), null);
                generateDefaultConstructor(cw);
                break;
            case METHOD:
                String methodName = args.get(0);
                String methodDesc = args.get(1);
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, methodDesc, null, null);
                JVMCodeGenUtil.getInstance().setMethodVisitor(mv);
                break;
            case END:
                cw.visitEnd();
                break;
            default:
                throw new UnsupportedOperationException();
        }
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

    enum CWVisitType {
        INIT, METHOD, END;
    }
}

