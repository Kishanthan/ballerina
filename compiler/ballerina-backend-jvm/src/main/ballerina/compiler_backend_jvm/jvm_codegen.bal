import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

BalToJVMIndexMap indexMap;
int returnVarRefIndex;
string currentFuncName;
string currentBBName;

bir:Function currentFunc = {};

public function main(string... args) {
    //do nothing
}

function genJVMClassFile(byte[] birBinary) returns byte[] {
    io:ByteChannel byteChannel = io:createMemoryChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    bir:ConstPoolParser cpParser = new(reader);
    bir:BirChannelReader birReader = new(reader, cpParser.parse());
    bir:TypeParser typeParser = new (birReader);
    bir:PackageParser pkgParser = new(birReader, typeParser);
    bir:Package pkg = pkgParser.parsePackage();
    return generateJVMClass(pkg);
}

function generateJVMClass(bir:Package pkg) returns byte[] {
    defineClass(pkg.org, pkg.name, pkg.versionValue);
    generateMethods(pkg.functions);

    jvm:classVisit("end", []);
    return jvm:getClassFileContent();
}

function defineClass(bir:Name orgName, bir:Name pkgName, bir:Name ver) {
    jvm:classVisit("init", ["DEFAULT_CLASS"]);
}

function generateMethods(bir:Function[] funcs) {
    foreach func in funcs {
        indexMap = new();
        currentFunc = untaint func;
        generateMethodDesc(func);
        generateMethodBody(func);
    }
}

function generateMethodDesc(bir:Function func) {
    currentFuncName = untaint func.name.value;

    string desc = "(";
    int i;
    while (i < func.argsCount) {
        match (func.typeValue.paramTypes[i]) {
            bir:BTypeInt => {
                desc = desc + "J";
            }
            bir:BArrayType => {
                desc = desc + "[J";
            }
            any => {
                error err = { message: "JVM generation is not supported for type " +
                                io:sprintf("%s", func.typeValue.paramTypes[i])};
                throw err;
            }
        }
        i++;
    }

    string returnType = generateReturnType(func.typeValue.retType);

    desc = desc + returnType;

    jvm:classVisit("method", [currentFuncName, desc]);
}

function generateMethodBody(bir:Function func) {
    jvm:methodVisit("code", []);

    // body visit
    int i = 0;
    int k = 1;
    boolean isVoidFunc = false;
    if (reflect:equals(bir:BTypeNil, func.typeValue.retType)) {
        isVoidFunc = true;
        k = 0;
    }

    bir:VariableDcl[] localVars = func.localVars;
    while (k < lengthof localVars) {
        bir:VariableDcl localVar = localVars[k];
        _ = getJVMIndexOfVarRef(localVar) but {() => 0};
        k++;
    }

    if (!isVoidFunc) {
        returnVarRefIndex = getJVMIndexOfVarRef(localVars[0]) but {() => 0};
    }

    bir:BasicBlock[] basicBlocks = func.basicBlocks;
    while (i < lengthof basicBlocks) {
        bir:BasicBlock bb = basicBlocks[i];
        //io:println("Basic Block Is : ", bb.id.value);
        currentBBName = io:sprintf("%s", bb.id.value);

        // create jvm label
        jvm:labelVisit("visit", [currentFuncName + bb.id.value]);

        // visit instructions
        int j = 0;
        while (j < lengthof bb.instructions) {
            bir:Instruction inst = bb.instructions[j];
            match inst {
                bir:ConstantLoad constIns => visitConstantLoadIns(constIns);
                bir:Move moveIns => visitMoveIns(moveIns);
                bir:BinaryOp binaryIns => visitBinaryOpIns(binaryIns);
                bir:ArrayAccess arrayAccessIns => visitArrayAccessIns(arrayAccessIns);
                bir:NewArray newArrayIns => visitNewArrayIns(newArrayIns);
                bir:ArrayStore arrayStoreIns => visitArrayStoreIns(arrayStoreIns);
                bir:Length lengthIns => visitLengthIns(lengthIns);
            }
            j++;
        }

        // visit terminator
        visitTerminator(bb);
        i++;
    }

    jvm:methodVisit("max", [100, 400]);
    jvm:methodVisit("end", []);
}

function visitConstantLoadIns(bir:ConstantLoad loadIns) {
    //load
    jvm:methodVisit("ldc_ins", [loadIns.value]);

    //store
    int index = getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl) but {() => 0};
    //io:println("Const Store Index is :::::::::::", index);
    jvm:methodVisit("var_ins", [LSTORE, index]);
}

function visitMoveIns(bir:Move moveIns) {
    int rhsIndex = getJVMIndexOfVarRef(moveIns.rhsOp.variableDcl) but {() => 0};
    //io:println("RHS Index is :::::::::::", rhsIndex);
    int lhsLndex = getJVMIndexOfVarRef(moveIns.lhsOp.variableDcl) but {() => 0};
    //io:println("LHS Index is :::::::::::", lhsLndex);
    match (moveIns.rhsOp.typeValue ) {
        bir:BTypeInt => {
            jvm:methodVisit("var_ins", [LLOAD, rhsIndex]);
            jvm:methodVisit("var_ins", [LSTORE, lhsLndex]);
        }
        bir:BArrayType => {
            jvm:methodVisit("var_ins", [ALOAD, rhsIndex]);
            jvm:methodVisit("var_ins", [ASTORE, lhsLndex]);
        }
        any => {
            error err = { message: "JVM generation is not supported for type" };
            throw err;
        }
    }
}

function visitNewArrayIns(bir:NewArray newArrayIns) {
    //io:println("NewArray Ins : ", io:sprintf("%s", newArrayIns));
    // LDC 10000000
    // L2I
    // NEWARRAY T_LONG
    // ASTORE

    // todo array length is hardcoded - fix me
    jvm:methodVisit("ldc_ins", [1000000]);
    jvm:methodVisit("ins", [L2I]);

    int T_LONG = 11;

    jvm:methodVisit("int_ins", [NEWARRAY, T_LONG]);

    int lhsIndex = getJVMIndexOfVarRef(newArrayIns.lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("var_ins", [ASTORE, lhsIndex]);
}

function visitArrayStoreIns(bir:ArrayStore arrayStoreIns) {
    //io:println("ArrayStore Ins : ", io:sprintf("%s", arrayStoreIns));
    // ALOAD
    // LLOAD
    // L2I
    // LLOAD
    // LASTORE

    int lhsIndex = getJVMIndexOfVarRef(arrayStoreIns.lhsOp.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ALOAD, lhsIndex]);

    int accessIndex = getJVMIndexOfVarRef(arrayStoreIns.storeIndex.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, accessIndex]);

    jvm:methodVisit("ins", [L2I]);

    int rhsIndex = getJVMIndexOfVarRef(arrayStoreIns.rhsOp.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, rhsIndex]);

    jvm:methodVisit("ins", [LASTORE]);
}

function visitArrayAccessIns(bir:ArrayAccess arrayAccessIns) {
    //io:println("ArrayAccess Ins : ", io:sprintf("%s", arrayAccessIns));

    // ALOAD
    // LLOAD
    // L2I
    // LALOAD


    int rhsIndex = getJVMIndexOfVarRef(arrayAccessIns.rhsOp.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ALOAD, rhsIndex]);

    int accessIndex = getJVMIndexOfVarRef(arrayAccessIns.accessIndex.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, accessIndex]);

    jvm:methodVisit("ins", [L2I]);

    int lhsIndex = getJVMIndexOfVarRef(arrayAccessIns.lhsOp.variableDcl) but {() => 0};
    jvm:methodVisit("ins", [LALOAD]);

    jvm:methodVisit("var_ins", [LSTORE, lhsIndex]);
}

function visitLengthIns(bir:Length lengthIns) {
    //io:println("Length Ins : ", io:sprintf("%s", lengthIns));

    //  ALOAD
    //  ARRAYLENGTH
    //  I2L
    //  LSTORE

    int rhsIndex = getJVMIndexOfVarRef(lengthIns.lengthVarOp.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ALOAD, rhsIndex]);

    jvm:methodVisit("ins", [ARRAYLENGTH]);
    jvm:methodVisit("ins", [I2L]);

    int lhsIndex = getJVMIndexOfVarRef(lengthIns.lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("var_ins", [LSTORE, lhsIndex]);
}

function visitBinaryOpIns(bir:BinaryOp binaryIns) {
    int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, rhsOps1Index]);

    int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, rhsOps2Index]);

    match binaryIns.kind {
        bir:LESS_THAN lessThanIns => visitLessThanIns(lessThanIns, binaryIns.lhsOp);
        bir:ADD addIns => visitAddIns(addIns, binaryIns.lhsOp);
        bir:EQUAL equalIns => visitEqualIns(equalIns, binaryIns.lhsOp);
        bir:SUB subIns => visitSubIns(subIns, binaryIns.lhsOp);
        bir:DIV => int a = 5; // todo not supported yet
        bir:GREATER_EQUAL => int a = 5; // todo not supported yet
        bir:GREATER_THAN => int a = 5; // todo not supported yet
        bir:LESS_EQUAL => int a = 5; // todo not supported yet
        bir:MUL => int a = 5; // todo not supported yet
        bir:NOT_EQUAL => int a = 5; // todo not supported yet
    }

}

function visitLessThanIns(bir:LESS_THAN lessThanIns, bir:VarRef lhsOp) {
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:labelVisit("create", [label1]);
    jvm:labelVisit("create", [label2]);

    jvm:methodVisit("ins", [LCMP]);
    jvm:labelVisit("less_than_0", [label1]);

    jvm:methodVisit("ins", [ICONST_0]);
    jvm:labelVisit("goto", [label2]);

    jvm:labelVisit("visit", [label1]);
    jvm:methodVisit("ins", [ICONST_1]);

    jvm:labelVisit("visit", [label2]);
    jvm:methodVisit("var_ins", [ISTORE, lhsOpIndex]);
}

function visitAddIns(bir:ADD addIns, bir:VarRef lhsOp) {
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("ins", [LADD]);
    jvm:methodVisit("var_ins", [LSTORE, lhsOpIndex]);
}

function visitEqualIns(bir:EQUAL addIns, bir:VarRef lhsOp) {
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:labelVisit("create", [label1]);
    jvm:labelVisit("create", [label2]);

    jvm:methodVisit("ins", [LCMP]);
    jvm:labelVisit("not_equal_0", [label1]);

    jvm:methodVisit("ins", [ICONST_1]);
    jvm:labelVisit("goto", [label2]);

    jvm:labelVisit("visit", [label1]);
    jvm:methodVisit("ins", [ICONST_0]);

    jvm:labelVisit("visit", [label2]);
    jvm:methodVisit("var_ins", [ISTORE, lhsOpIndex]);
}

function visitSubIns(bir:SUB subIns, bir:VarRef lhsOp) {
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("ins", [LSUB]);
    jvm:methodVisit("var_ins", [LSTORE, lhsOpIndex]);
}

function visitTerminator(bir:BasicBlock bb) {
    match bb.terminator {
        bir:GOTO gotoIns => genGoToTerm(gotoIns);
        bir:Return returnIns => genReturnTerm(returnIns);
        bir:Branch brIns => genBranchTerm(brIns);
        bir:Call callIns => genCallTerm(callIns);
    }
}

function genGoToTerm(bir:GOTO gotoIns) {
    jvm:labelVisit("goto", [currentFuncName + gotoIns.targetBB.id.value]);
}

function genReturnTerm(bir:Return returnIns) {
    if (reflect:equals(bir:BTypeNil, currentFunc.typeValue.retType)) {
        jvm:methodVisit("ins", [RETURN]);
    } else {
        match (currentFunc.typeValue.retType) {
            bir:BArrayType => {
                jvm:methodVisit("var_ins", [ALOAD, returnVarRefIndex]);
            }
            bir:BTypeInt => {
                jvm:methodVisit("var_ins", [LLOAD, returnVarRefIndex]);
                jvm:methodVisit("method_ins", []);
            }
            any => {
                error err = { message: "JVM generation is not supported for type " +
                                io:sprintf("%s", currentFunc.typeValue.retType)};
                throw err;
            }
        }
        jvm:methodVisit("ins", [ARETURN]);
    }
}

function genBranchTerm(bir:Branch branchIns) {
    string trueBBId = branchIns.trueBB.id.value;
    string falseBBId = branchIns.falseBB.id.value;

    int opIndex = getJVMIndexOfVarRef(branchIns.op.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ILOAD, opIndex]);
    jvm:labelVisit("greater_than_0", [currentFuncName + trueBBId]);
    jvm:labelVisit("goto", [currentFuncName + falseBBId]);
}

function genCallTerm(bir:Call callIns) {
    string jvmClass = "DEFAULT_CLASS"; //todo get the correct class name
    string methodName = callIns.name.value;
    string methodDesc = "(";
    foreach arg in callIns.args {
        int argIndex = getJVMIndexOfVarRef(arg.variableDcl) but {() => 0};
        jvm:methodVisit("var_ins", [LLOAD, argIndex]);
        methodDesc = methodDesc + "J";
    }

    methodDesc = methodDesc + ")Ljava/lang/Object;";

    // call method
    jvm:methodInvokeVisit("invoke_static", [jvmClass, methodName, methodDesc]);

    jvm:methodInvokeVisit("type_cast", []);
    jvm:methodInvokeVisit("invoke_virtual", [jvmClass, methodName, methodDesc]);

    // store return
    bir:VariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl but {() => {}};

    int lhsLndex = getJVMIndexOfVarRef(lhsOpVarDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LSTORE, lhsLndex]);

    // goto thenBB
    jvm:labelVisit("goto", [currentFuncName + callIns.thenBB.id.value]);
}


function generateReturnType(bir:BType bType) returns string {
    match bType {
        bir:BTypeInt => return ")Ljava/lang/Object;";
        bir:BArrayType => return ")Ljava/lang/Object;";
        bir:BTypeNil => return ")V";
        any => {
            error err = { message: "JVM generation is not supported for type" };
            throw err;
        }
    }
}

function checkValidBirChannel(bir:ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(bir:ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = { message: "Invalid BIR binary content, unexptected header" };
        throw err;
    }
}

function checkVersion(bir:ChannelReader reader) {
    var birVersion = reader.readInt32();
    var supportedBirVersion = 1;
    if (birVersion != 1){
        error err = {message: "Unsupported BIR version " + birVersion + ", supports version " + supportedBirVersion};
        throw err;
    }
}

function openFileForReading(string filePath) returns io:ByteChannel {
    io:ByteChannel byteChannel = io:openFile(filePath, io:READ);
    return byteChannel;
}

function arrayEq(byte[] x, byte[] y) returns boolean {
    var xLen = lengthof x;

    if xLen != lengthof y{
        return false;
    }

    int i;
    while i < xLen {
        if (x[i] != y[i]){
            return false;
        }
        i++;
    }
    return true;
}


function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int? {
    if (indexMap.getIndex(varDcl) == -1) {
        indexMap.add(varDcl);
    }
    return indexMap.getIndex(varDcl);
}

type BalToJVMIndexMap object {
    private int localVarIndex;
    private map<int> jvmLocalVarIndexMap;

    function add(bir:VariableDcl varDcl) {
        string varRefName = getVarRefName(varDcl);
        jvmLocalVarIndexMap[varRefName] = localVarIndex;
        localVarIndex = localVarIndex + 2;
    }

    function getIndex(bir:VariableDcl varDcl) returns int? {
        string varRefName = getVarRefName(varDcl);
        if (!(jvmLocalVarIndexMap.hasKey(varRefName))) {
            return -1;
        }

        return jvmLocalVarIndexMap[varRefName];
    }

    private function getVarRefName(bir:VariableDcl varDcl) returns string {
        return io:sprintf("%s", varDcl);
    }
};


