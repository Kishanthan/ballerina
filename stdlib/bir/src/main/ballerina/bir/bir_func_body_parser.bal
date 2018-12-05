import ballerina/internal;
import ballerina/io;

public type FuncBodyParser object {
    BirChannelReader reader;
    map<VariableDcl> localVarMap;
    TypeParser typeParser;
    public new(reader, typeParser, localVarMap) {
    }

    public function parseBB() returns BasicBlock {
        var id = reader.readStringCpRef();
        var numInstruction = reader.readInt32() - 1;
        Instruction[] instructions;
        int i;
        while (i < numInstruction) {
            instructions[i] = parseInstruction();
            i++;
        }
        return { id: { value: id },
            instructions: instructions,
            terminator: parseTerminator() };
    }

    public function parseInstruction() returns Instruction {
        var kindTag = reader.readInt8();
        InstructionKind kind = "CONST_LOAD";
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vers
        if (kindTag == 6){
            var bType = typeParser.parseType();
            kind = "CONST_LOAD";
            var constLoad = new ConstantLoad(kind,
                parseVarRef(),
                bType,
                reader.readIntCpRef());
            return constLoad;
        } else if (kindTag == 5){
            kind = "MOVE";
            var rhsOp = parseVarRef();
            var lhsOp = parseVarRef();
            return new Move(kind, lhsOp, rhsOp);
        } else if (kindTag == 18) {
            kind = "NEW_ARRAY";
            var bType = typeParser.parseType();
            var lhsOp = parseVarRef();
            return new NewArray(kind, lhsOp, bType);
        } else if (kindTag == 19) {
            kind = "ARRAY_STORE";
            var rhsOp = parseVarRef();
            var storeIndex = parseVarRef();
            var lhsOp = parseVarRef();
            return new ArrayStore(kind, rhsOp, storeIndex, lhsOp);
        } else if (kindTag == 20) {
            kind = "ARRAY_ACCESS";
            var rhsOp = parseVarRef();
            var accessIndex = parseVarRef();
            var lhsOp = parseVarRef();
            return new ArrayAccess(kind, rhsOp, accessIndex, lhsOp);
        } else if (kindTag == 21) {
            kind = "LENGTH";
            var rhsOp = parseVarRef();
            var lhsOp = parseVarRef();
            return new Length(kind, rhsOp, lhsOp);
        } else {
            return parseBinaryOpInstruction(kindTag);
        }
    }



    public function parseTerminator() returns Terminator {
        var kindTag = reader.readInt8();
        if (kindTag == 3){
            InstructionKind kind = "BRANCH";
            var op = parseVarRef();
            BasicBlock trueBB = parseBBRef();
            BasicBlock falseBB = parseBBRef();
            return new Branch(falseBB, kind, op, trueBB);
        } else if (kindTag == 1){
            InstructionKind kind = "GOTO";
            return new GOTO(kind, parseBBRef());
        } else if (kindTag == 4){
            InstructionKind kind = "RETURN";
            return new Return(kind);
        } else if (kindTag == 2){
            InstructionKind kind = "CALL";
            var pkgIdCp = reader.readInt32();
            var name = reader.readStringCpRef();
            var argsCount = reader.readInt32();
            Operand[] args = [];
            int i = 0;
            while (i < argsCount) {
                args[i] = parseVarRef();
                i++;
            }
            var hasLhs = reader.readBoolean();
            VarRef? lhsOp = ();
            if (hasLhs){
                lhsOp = parseVarRef();
            }
            BasicBlock thenBB = parseBBRef();
            return new Call(args, kind, lhsOp, { value: name }, thenBB);

        }
        error err = { message: "term instrucion kind " + kindTag + " not impl." };
        throw err;
    }


    public function parseVarRef() returns VarRef {
        var varName = reader.readStringCpRef();
        var decl = getDecl(localVarMap, varName);
        return new VarRef("VAR_REF", decl.typeValue, decl);
    }

    public function parseBBRef() returns BasicBlock {
        return { id: { value: reader.readStringCpRef() } };
    }

    public function parseBinaryOpInstruction(int kindTag) returns BinaryOp {
        BinaryOpInstructionKind kind = "ADD";
        if (kindTag == 7){
            kind = "ADD";
        } else if (kindTag == 8){
            kind = "SUB";
        } else if (kindTag == 9){
            kind = "MUL";
        } else if (kindTag == 10){
            kind = "DIV";
        } else if (kindTag == 12){
            kind = "EQUAL";
        } else if (kindTag == 13){
            kind = "NOT_EQUAL";
        } else if (kindTag == 14){
            kind = "GREATER_THAN";
        } else if (kindTag == 15){
            kind = "GREATER_EQUAL";
        } else if (kindTag == 16){
            kind = "LESS_THAN";
        } else if (kindTag == 17){
            kind = "LESS_EQUAL";
        } else if (kindTag == 18){
            kind = "LESS_EQUAL";
        } else if (kindTag == 22){
            kind = "AND";
        } else if (kindTag == 23){
            kind = "OR";
        } else {
            error err = { message: "instrucion kind " + kindTag + " not impl." };
            throw err;
        }

        var rhsOp1 = parseVarRef();
        var rhsOp2 = parseVarRef();
        var lhsOp = parseVarRef();
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
    }

};

function getDecl(map<VariableDcl> localVarMap, string varName) returns VariableDcl {
    var posibalDcl = localVarMap[varName];
    match posibalDcl {
        VariableDcl dcl => return dcl;
        () => {
            error err = { message: "local var missing " + varName };
            throw err;
        }
    }
}

