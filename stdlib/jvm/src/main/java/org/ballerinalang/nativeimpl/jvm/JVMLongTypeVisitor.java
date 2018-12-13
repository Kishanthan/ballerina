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
import org.objectweb.asm.Type;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "longTypeVisit",
        args = {
                @Argument(name = "visitType", type = STRING),
                @Argument(name = "args", type = ARRAY, elementType = STRING),
        }
)
public class JVMLongTypeVisitor extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        MethodVisitor mv = JVMCodeGenUtil.getInstance().getMethodVisitor();

        String type = context.getStringArgument(0);
        BStringArray args = (BStringArray) context.getRefArgument(0);

        switch (MethodInvokeVisitType.valueOf(type.toUpperCase())) {
            case INVOKE_STATIC:
                String className = args.get(0);
                String methodName = args.get(1);
                String methodDesc = args.get(2);
                mv.visitMethodInsn(INVOKESTATIC, className, methodName, methodDesc, false);
                break;
            case INVOKE_VIRTUAL_LONG:
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Long.class), "longValue", "()J", false);
                break;
            case TYPE_CAST_LONG:
                className = Type.getInternalName(Long.class);
                mv.visitTypeInsn(CHECKCAST, className);
                break;
            case TYPE_CAST_LONG_ARRAY:
                className = Type.getInternalName(long[].class);
                mv.visitTypeInsn(CHECKCAST, className);
                break;
            case TYPE_CAST_LONG_ARRAY_OF_ARRAY:
                className = Type.getInternalName(long[][].class);
                mv.visitTypeInsn(CHECKCAST, className);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    enum MethodInvokeVisitType {
        INVOKE_STATIC, INVOKE_VIRTUAL_LONG, TYPE_CAST_LONG, TYPE_CAST_LONG_ARRAY, TYPE_CAST_LONG_ARRAY_OF_ARRAY;
    }
}
