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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "labelVisit",
        args = {
                @Argument(name = "visitType", type = STRING),
                @Argument(name = "args", type = ARRAY, elementType = STRING),
        }
)
public class JVMLabelVisitor extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        MethodVisitor mv = JVMCodeGenUtil.getInstance().getMethodVisitor();

        String type = context.getStringArgument(0);
        BStringArray args = (BStringArray) context.getRefArgument(0);

        switch (LabelVisitType.valueOf(type.toUpperCase())) {
            case GOTO:
                String labelId = args.get(0);
                Label label = JVMCodeGenUtil.getInstance().getLabel(labelId);
                mv.visitJumpInsn(GOTO, label);
                break;
            case VISIT:
                labelId = args.get(0);
                label = JVMCodeGenUtil.getInstance().getLabel(labelId);
                mv.visitLabel(label);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    enum LabelVisitType {
        GOTO, VISIT;
    }
}
