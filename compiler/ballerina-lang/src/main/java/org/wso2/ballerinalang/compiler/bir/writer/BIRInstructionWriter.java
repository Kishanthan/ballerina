/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.writer;

import io.netty.buffer.ByteBuf;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.BooleanCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ArrayAccess;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ArrayStore;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewArray;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;

/**
 * Responsible for serializing BIR instructions and operands.
 *
 * @since 0.980.0
 */
public class BIRInstructionWriter extends BIRVisitor {
    private ByteBuf buf;
    private ConstantPool cp;
    private TypeVisitor typeWriter;

    public BIRInstructionWriter(ByteBuf buf, ConstantPool cp, TypeVisitor typeWriter) {
        this.buf = buf;
        this.cp = cp;
        this.typeWriter = typeWriter;
    }

    public void writeBBs(List<BIRBasicBlock> bbList) {
        buf.writeInt(bbList.size());
        bbList.forEach(bb -> bb.accept(this));
    }

    public void visit(BIRBasicBlock birBasicBlock) {
        //Name of the basic block
        addCpAndWriteString(birBasicBlock.id.value);
        // Number of instructions
        // Adding the terminator instruction as well.
        buf.writeInt(birBasicBlock.instructions.size() + 1);
        birBasicBlock.instructions.forEach(instruction -> ((BIRNonTerminator) instruction).accept(this));
        if (birBasicBlock.terminator == null) {
            throw new BLangCompilerException("Basic block without a terminator : " + birBasicBlock.id);
        }
        birBasicBlock.terminator.accept(this);
    }


    // Terminating instructions

    public void visit(BIRTerminator.GOTO birGoto) {
        buf.writeByte(birGoto.kind.getValue());
        addCpAndWriteString(birGoto.targetBB.id.value);
    }

    public void visit(BIRTerminator.Return birReturn) {
        buf.writeByte(birReturn.kind.getValue());
    }

    public void visit(BIRTerminator.Branch birBranch) {
        buf.writeByte(birBranch.kind.getValue());
        birBranch.op.accept(this);
        // true:BB
        addCpAndWriteString(birBranch.trueBB.id.value);
        // false:BB
        addCpAndWriteString(birBranch.falseBB.id.value);
    }


    // Non-terminating instructions

    public void visit(Move birMove) {
        buf.writeByte(birMove.kind.getValue());
        birMove.rhsOp.accept(this);
        birMove.lhsOp.accept(this);
    }

    public void visit(BIRTerminator.Call birCall) {
        buf.writeByte(birCall.kind.getValue());
        PackageID calleePkg = birCall.calleePkg;
        int orgCPIndex = addStringCPEntry(calleePkg.orgName.value);
        int nameCPIndex = addStringCPEntry(calleePkg.name.value);
        int versionCPIndex = addStringCPEntry(calleePkg.version.value);
        int pkgIndex = cp.addCPEntry(new CPEntry.PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        buf.writeInt(pkgIndex);
        buf.writeInt(addStringCPEntry(birCall.name.getValue()));
        buf.writeInt(birCall.args.size());
        for (BIROperand arg : birCall.args) {
            arg.accept(this);
        }
        if (birCall.lhsOp != null) {
            buf.writeByte(1);
            birCall.lhsOp.accept(this);
        } else {
            buf.writeByte(0);
        }
        addCpAndWriteString(birCall.thenBB.id.value);
    }

    public void visit(BinaryOp birBinaryOp) {
        buf.writeByte(birBinaryOp.kind.getValue());
        birBinaryOp.rhsOp1.accept(this);
        birBinaryOp.rhsOp2.accept(this);
        birBinaryOp.lhsOp.accept(this);
    }

    public void visit(UnaryOP birUnaryOp) {
        buf.writeByte(birUnaryOp.kind.getValue());
        birUnaryOp.rhsOp.accept(this);
        birUnaryOp.lhsOp.accept(this);
    }

    public void visit(ConstantLoad birConstantLoad) {
        buf.writeByte(birConstantLoad.kind.getValue());
        birConstantLoad.type.accept(typeWriter);
        birConstantLoad.lhsOp.accept(this);

        BType type = birConstantLoad.type;
        switch (type.tag) {
            case TypeTags.INT:
                buf.writeInt(cp.addCPEntry(new IntegerCPEntry(((Number) birConstantLoad.value).longValue())));
                break;
            case TypeTags.BOOLEAN:
                buf.writeInt(cp.addCPEntry(new BooleanCPEntry((Boolean) birConstantLoad.value)));
                break;
            case TypeTags.STRING:
                buf.writeInt(cp.addStringCPEntry((String) birConstantLoad.value));
                break;
            case TypeTags.NIL:
                break;
            default:
                throw new IllegalStateException("unsupported constant type: " + type.getDesc());
        }
    }

    @Override
    public void visit(NewArray newArray) {
        buf.writeByte(newArray.kind.getValue());
        newArray.type.accept(typeWriter);
        newArray.lhsOp.accept(this);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        buf.writeByte(arrayAccess.kind.getValue());
        arrayAccess.rhsOp.accept(this);
        arrayAccess.index.accept(this);
        arrayAccess.lhsOp.accept(this);
    }

    @Override
    public void visit(ArrayStore arrayStore) {
        buf.writeByte(arrayStore.kind.getValue());
        arrayStore.rhsOp.accept(this);
        arrayStore.index.accept(this);
        arrayStore.lhsOp.accept(this);
    }

    // Operands
    public void visit(BIROperand.BIRVarRef birVarRef) {
        // TODO use the integer index of the variable.
        addCpAndWriteString(birVarRef.variableDcl.name.value);
    }


    // private methods

    private void addCpAndWriteString(String string) {
        buf.writeInt(addStringCPEntry(string));
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new CPEntry.StringCPEntry(value));
    }
}
