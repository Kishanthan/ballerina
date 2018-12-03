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
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "methodVisit",
        args = {
                @Argument(name = "visitType", type = STRING),
                @Argument(name = "args", type = ARRAY, elementType = INT),
        }
)
public class JVMMethodVisitor extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        MethodVisitor mv = JVMCodeGenUtil.getInstance().getMethodVisitor();

        String type = context.getStringArgument(0);
        BIntArray args = (BIntArray) context.getRefArgument(0);

        switch (MethodVisitType.valueOf(type.toUpperCase())) {
            case CODE:
                mv.visitCode();
                break;
            case MAX:
                int maxStack = (int) args.get(0);
                int maxLocal = (int) args.get(1);
                mv.visitMaxs(maxStack, maxLocal);
                break;
            case END:
                mv.visitEnd();
                break;
            case VAR_INS:
                int opCode = (int) args.get(0);
                int varIndex = (int) args.get(1);
                mv.visitVarInsn(opCode, varIndex);
                break;
            case METHOD_INS:
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", "(J)Ljava/lang/Long;",
                        false);
                break;
            case INS:
                opCode = (int) args.get(0);
                mv.visitInsn(opCode);
                break;
            case LDC_INS:
                Long value = args.get(0);
                mv.visitLdcInsn(value);
                break;
            case INT_INS:
                opCode = (int) args.get(0);
                int operand = (int) args.get(1);
                mv.visitIntInsn(opCode, operand);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    enum MethodVisitType {
        CODE, MAX, END, VAR_INS, METHOD_INS, INS, LDC_INS, JUMP_INS, TYPE_INS, INT_INS;
    }
}
