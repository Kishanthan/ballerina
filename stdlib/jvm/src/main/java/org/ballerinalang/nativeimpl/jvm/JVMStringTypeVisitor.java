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
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Native class for jvm string operations related byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "stringTypeVisit",
        args = {
                @Argument(name = "visitType", type = STRING),
                @Argument(name = "args", type = ARRAY, elementType = STRING),
        }
)
public class JVMStringTypeVisitor extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        MethodVisitor mv = JVMCodeGenUtil.getInstance().getMethodVisitor();

        String type = context.getStringArgument(0);
        BStringArray args = (BStringArray) context.getRefArgument(0);

        switch (MethodVisitType.valueOf(type.toUpperCase())) {
            case LDC_INS:
                String value = args.get(0);
                mv.visitLdcInsn(value);
                break;
            case NEW:
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                break;
            case APPEND:
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                break;
            case CONCAT:
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                        "(Ljava/lang/String;)Ljava/lang/String;", false);
                break;
            case TO_STRING:
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                break;
            case CONTAINS:
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
                break;
            case MATCHES:
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "matches", "(Ljava/lang/String;)Z", false);
                break;
            case CHECKCAST:
                mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    enum MethodVisitType {
        METHOD_INS, LDC_INS, NEW, APPEND, TO_STRING, CHECKCAST, CONCAT, CONTAINS, MATCHES;
    }
}
