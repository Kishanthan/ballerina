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
package org.wso2.ballerinalang.compiler.codegen;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.OperatorKind;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.Instruction;
import org.wso2.ballerinalang.programfile.InstructionCodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.model.tree.OperatorKind.*;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

public class JavaByteCodeGenerator extends BLangNodeVisitor {

    private static final CompilerContext.Key<JavaByteCodeGenerator> JAVA_BYTE_CODE_GENERATOR_KEY =
            new CompilerContext.Key<>();

    private SymbolEnv env;
    private SymbolTable symTable;

    private ClassWriter cw;
    private MethodVisitor mv;

    private BalToJVMIndexMap indexMap;
    private boolean varAssignment;

    public JavaByteCodeGenerator(CompilerContext context) {
        context.put(JAVA_BYTE_CODE_GENERATOR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.indexMap = new BalToJVMIndexMap();
    }

    public BLangPackage generateClass(BLangPackage pkgNode) {

        genNode(pkgNode, this.symTable.pkgEnvMap.get(pkgNode.symbol));

        //todo: temporarily writing the class file here it self, but it should be moved to binary file writer
        try {
            Files.write(Paths.get("/Users/kishanthan/WSO2/msc/class-files/Test.class") , cw.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.JAVA_BYTE_CODE_GEN)) {
            return;
        }

        String jvmClassName = ballerinaPackageToJVMClass(pkgNode.packageID);

        cw = new ClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, jvmClassName, null, Type.getInternalName(Object.class), null);
        generateDefaultConstructor(cw);

        pkgNode.functions.forEach(this::generateFunctionInfo);

        cw.visitEnd();

        pkgNode.completedPhases.add(CompilerPhase.JAVA_BYTE_CODE_GEN);
    }

    private String ballerinaPackageToJVMClass(PackageID packageID) {
        //todo
        return "Test";
    }

    private void generateFunctionInfo(BLangFunction funcNode) {
        String methodDesc = generateJVMMethodDesc(funcNode);
        String methodName = funcNode.getName().value;

        mv = this.cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, methodDesc, null, null);
        mv.visitCode();

        genNode(funcNode, this.env);

        mv.visitMaxs(100, 400);
        mv.visitEnd();
    }

    private String generateJVMMethodDesc(BLangFunction function) {
        List<BLangVariable> requiredParams = function.requiredParams;
        //todo fix for default, rest params
        StringBuilder result = new StringBuilder("(");
        for (int i = 0; i < requiredParams.size(); i++) {
            BLangVariable param = requiredParams.get(i);
            switch (param.type.tag) {
                case TypeTags.INT:
                    result.append("J");
                    indexMap.add(param.symbol);
                default:
                    //todo other types
                    break;
            }

        }
        if (TypeTags.NIL == function.returnTypeNode.type.tag) {
            result.append(")V");
        } else {
            result.append(")Ljava/lang/Object;");
        }
        return result.toString();
    }

    class BalToJVMIndexMap {
        private int localVarIndex;
        private Map<BSymbol, Integer> jvmLocalVarIndexMap = new HashMap<>();

        void add(BSymbol symbol) {
            jvmLocalVarIndexMap.put(symbol, localVarIndex);
            localVarIndex = localVarIndex + 2;
        }

        int getIndex(BSymbol symbol) {
            if (!(jvmLocalVarIndexMap.containsKey(symbol))) {
                return -1;
            }

            return jvmLocalVarIndexMap.get(symbol);
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

    private <T extends BLangNode, U extends SymbolEnv> T genNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
        return t;
    }

    @Override
    public void visit(BLangFunction funcNode) {
        genNode(funcNode.body, this.env);
    }

    public void visit(BLangVariableDef varDefNode) {
        genNode(varDefNode.var, this.env);
    }

    public void visit(BLangVariable varNode) {
        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr != null) {
            genNode(rhsExpr, this.env);
        }
        int index = getJVMIndexOfVar(varNode.symbol);
        mv.visitVarInsn(LSTORE, index);
    }

    public void visit(BLangLocalVarRef localVarRef) {
        int opcode = LLOAD;
        if (varAssignment) {
            opcode = LSTORE;
        }

        int varIndex = getJVMIndexOfVar(localVarRef.varSymbol);
        switch (localVarRef.type.tag) {
            case TypeTags.INT:
                mv.visitVarInsn(opcode, varIndex);
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        int typeTag = literalExpr.type.tag;

        switch (typeTag) {
            case TypeTags.INT:
                long longVal = (Long) literalExpr.value;
                loadIntegerConstantToJVMStack(longVal);
                break;
            default:
                break;
        }
    }

    private int getJVMIndexOfVar(BVarSymbol varSymbol) {
        if (indexMap.getIndex(varSymbol) == -1) {
            indexMap.add(varSymbol);
        }

        return indexMap.getIndex(varSymbol);
    }

    private void loadIntegerConstantToJVMStack(long value) {
        if (value == 0) {
            mv.visitInsn(LCONST_0);
        } else if (value == 1) {
            mv.visitInsn(LCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public void visit(BLangBlockStmt blockNode) {
        for (BLangStatement stmt : blockNode.stmts) {
            genNode(stmt, this.env);
        }
    }

    public void visit(BLangReturn returnNode) {
        if (returnNode.expr.type != symTable.nilType) {
            BLangExpression expr = returnNode.expr;
            genNode(expr, this.env);
            if (TypeTags.INT == returnNode.expr.type.tag) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", "(J)Ljava/lang/Long;",
                        false);
            }
            mv.visitInsn(ARETURN);
        } else {
            mv.visitInsn(RETURN);
        }
    }

    public void visit(BLangBinaryExpr binaryExpr) {

        switch (binaryExpr.opKind) {
            case ADD:
                genNode(binaryExpr.lhsExpr, this.env);
                genNode(binaryExpr.rhsExpr, this.env);
                mv.visitInsn(LADD);
                break;
            case EQUAL:
                genNode(binaryExpr.lhsExpr, this.env);
                genNode(binaryExpr.rhsExpr, this.env);
                mv.visitInsn(LCMP);
                break;
            default:
                break;
        }
    }

    public void visit(BLangAssignment assignNode) {
        BLangExpression lhrExpr = assignNode.varRef;
        BLangExpression rhsExpr = assignNode.expr;

        genNode(rhsExpr, this.env);

        varAssignment = true;
        genNode(lhrExpr, this.env);
        varAssignment = false;
    }

    public void visit(BLangIf ifNode) {
        genNode(ifNode.expr, this.env);
        Label label1 = new Label();
        mv.visitJumpInsn(IFNE, label1);
        genNode(ifNode.body, this.env);
        Label label2 = new Label();
        mv.visitJumpInsn(GOTO, label2);
        mv.visitLabel(label1);

        if (ifNode.elseStmt != null) {
            genNode(ifNode.elseStmt, this.env);
            mv.visitLabel(label2);
        }
    }
}
