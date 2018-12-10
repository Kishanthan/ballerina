import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

BalToJVMIndexMap indexMap;
int returnVarRefIndex;
string currentFuncName;
string currentBBName;
int arrayInitLengthIndex = -1;

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
        arrayInitLengthIndex = -1;
    }
}

function generateMethodDesc(bir:Function func) {
    currentFuncName = untaint func.name.value;

    string desc = "(";
    int i;
    while (i < func.argsCount) {
        desc = desc + getFunctionArgDesc(func.typeValue.paramTypes[i]);
        i++;
    }

    string returnType = generateReturnType(func.typeValue.retType);

    desc = desc + returnType;

    jvm:classVisit("method", [currentFuncName, desc]);
}

function getFunctionArgDesc(bir:BType bType) returns string {
    match (bType) {
        bir:BTypeInt => {
            return "J";
        }
        bir:BTypeString => {
            return "Ljava/lang/String;";
        }
        bir:BArrayType => {
            return "[J";
        }
        any => {
            error err = { message: "JVM generation is not supported for type " + io:sprintf("%s", bType)};
            throw err;
        }
    }
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
    bir:BType bType = loadIns.typeValue;
    match bType {
        bir:BTypeInt => {
            any val = loadIns.value;
            jvm:methodVisit("ldc_ins", [check <int>val]);

            //store
            int index = getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl) but {() => 0};
            //io:println("Const Store Index is :::::::::::", index);
            jvm:methodVisit("var_ins", [LSTORE, index]);
        }
        bir:BTypeString => {
            any val = loadIns.value;
            jvm:stringTypeVisit("ldc_ins", [<string>val]);

            //store
            int index = getJVMIndexOfVarRef(loadIns.lhsOp.variableDcl) but {() => 0};
            //io:println("Const Store Index is :::::::::::", index);
            jvm:methodVisit("var_ins", [ASTORE, index]);
        }
        any => {
            error err = { message: "JVM generation is not supported for type : " + io:sprintf("%s", bType)};
            throw err;
        }
    }
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
        bir:BTypeString => {
            jvm:methodVisit("var_ins", [ALOAD, rhsIndex]);
            jvm:methodVisit("var_ins", [ASTORE, lhsLndex]);
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
    // LDC
    // L2I
    // NEWARRAY T_LONG
    // ASTORE

    if (arrayInitLengthIndex == -1){
        // todo array length is hardcoded - fix me
        jvm:methodVisit("ldc_ins", [100]);
    } else {
        jvm:methodVisit("var_ins", [LLOAD, arrayInitLengthIndex]);
    }
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

    match lengthIns.lengthVarOp.typeValue {
        bir:BArrayType => {
            jvm:methodVisit("var_ins", [ALOAD, rhsIndex]);
            jvm:methodVisit("ins", [ARRAYLENGTH]);
            jvm:methodVisit("ins", [I2L]);
        }
        bir:BTypeInt => {
            jvm:methodVisit("var_ins", [LLOAD, rhsIndex]);
        }
        any => {
            error err = {message: "JVM generation is not supported for type " +
                            io:sprintf("%s", lengthIns.lengthVarOp.typeValue)};
            throw err;
        }
    }

    int lhsIndex = getJVMIndexOfVarRef(lengthIns.lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("var_ins", [LSTORE, lhsIndex]);

    arrayInitLengthIndex = lhsIndex;
}

function visitBinaryOpIns(bir:BinaryOp binaryIns) {
    match binaryIns.kind {
        bir:LESS_THAN => visitLessThanIns(binaryIns);
        bir:ADD => visitAddIns(binaryIns);
        bir:EQUAL => visitEqualIns(binaryIns);
        bir:SUB => visitSubIns(binaryIns);
        bir:DIV => visitDivIns(binaryIns);
        bir:AND => visitAndIns(binaryIns);
        bir:LESS_EQUAL => visitLessEqualIns(binaryIns);
        any => {
            error err = {message: "JVM generation is not supported for type : " + io:sprintf("%s", binaryIns.kind)};
            throw err;
        }
    }
}

function visitBinaryRhsAndLhsLoad(bir:BinaryOp binaryIns) {
    int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, rhsOps1Index]);

    int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [LLOAD, rhsOps2Index]);
}

function visitLessThanIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
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

function visitLessEqualIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:labelVisit("create", [label1]);
    jvm:labelVisit("create", [label2]);

    jvm:methodVisit("ins", [LCMP]);
    jvm:labelVisit("less_than_equal_0", [label1]);

    jvm:methodVisit("ins", [ICONST_0]);
    jvm:labelVisit("goto", [label2]);

    jvm:labelVisit("visit", [label1]);
    jvm:methodVisit("ins", [ICONST_1]);

    jvm:labelVisit("visit", [label2]);
    jvm:methodVisit("var_ins", [ISTORE, lhsOpIndex]);
}

function visitEqualIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
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

function visitAddIns(bir:BinaryOp binaryIns) {
    //io:println("ADD Ins " + io:sprintf("%s", binaryIns));
    match binaryIns.lhsOp.typeValue {
        bir:BTypeInt => {
            bir:VarRef lhsOp = binaryIns.lhsOp;
            visitBinaryRhsAndLhsLoad(binaryIns);
            int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

            jvm:methodVisit("ins", [LADD]);
            jvm:methodVisit("var_ins", [LSTORE, lhsOpIndex]);
        }
        bir:BTypeString => {
            //jvm:stringTypeVisit("new", []);

            int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl) but {() => 0};
            jvm:methodVisit("var_ins", [ALOAD, rhsOps1Index]);
            //jvm:stringTypeVisit("append", []);

            int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl) but {() => 0};
            jvm:methodVisit("var_ins", [ALOAD, rhsOps2Index]);
            //jvm:stringTypeVisit("append", []);
            jvm:stringTypeVisit("concat", []);

            //jvm:stringTypeVisit("to_string", []);

            bir:VarRef lhsVarRef = binaryIns.lhsOp;
            int lhsIndex = getJVMIndexOfVarRef(lhsVarRef.variableDcl) but {() => 0};
            jvm:methodVisit("var_ins", [ASTORE, lhsIndex]);
        }
        any => {
            error err = { message: "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue)};
            throw err;
        }
    }
}

function visitSubIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("ins", [LSUB]);
    jvm:methodVisit("var_ins", [LSTORE, lhsOpIndex]);
}

function visitDivIns(bir:BinaryOp binaryIns) {
    bir:VarRef lhsOp = binaryIns.lhsOp;
    visitBinaryRhsAndLhsLoad(binaryIns);
    //io:println("DIV ins : " + io:sprintf("%s", lhsOp));
    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};

    jvm:methodVisit("ins", [LDIV]);
    jvm:methodVisit("var_ins", [LSTORE, lhsOpIndex]);
}

function visitAndIns(bir:BinaryOp binaryIns) {
    // ILOAD
    // ICONST_1
    // IF_ICMPNE L0
    // ILOAD
    // ICONST_1
    // IF_ICMPNE L0
    // ICONST_1
    // ISTORE

    bir:VarRef lhsOp = binaryIns.lhsOp;

    //io:println("AND ins : " + io:sprintf("%s", binaryIns));

    string label1 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "01";
    string label2 = currentFuncName + currentBBName + io:sprintf("%s", lhsOp.variableDcl) + "02";

    jvm:labelVisit("create", [label1]);
    jvm:labelVisit("create", [label2]);

    int rhsOps1Index = getJVMIndexOfVarRef(binaryIns.rhsOp1.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ILOAD, rhsOps1Index]);

    jvm:methodVisit("ins", [ICONST_1]);
    jvm:labelVisit("if_icmpne", [label1]);

    int rhsOps2Index = getJVMIndexOfVarRef(binaryIns.rhsOp2.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ILOAD, rhsOps2Index]);

    jvm:methodVisit("ins", [ICONST_1]);
    jvm:labelVisit("if_icmpne", [label1]);

    jvm:methodVisit("ins", [ICONST_1]);
    jvm:labelVisit("goto", [label2]);

    jvm:labelVisit("visit", [label1]);
    jvm:methodVisit("ins", [ICONST_0]);

    jvm:labelVisit("visit", [label2]);

    int lhsOpIndex = getJVMIndexOfVarRef(lhsOp.variableDcl) but {() => 0};
    jvm:methodVisit("var_ins", [ISTORE, lhsOpIndex]);
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
            bir:BTypeString => {
                jvm:methodVisit("var_ins", [ALOAD, returnVarRefIndex]);
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
    //io:println("Call Ins : " + io:sprintf("%s", callIns));
    string jvmClass = "DEFAULT_CLASS"; //todo get the correct class name
    string methodName = callIns.name.value;
    string methodDesc = "(";
    foreach arg in callIns.args {

        int argIndex = getJVMIndexOfVarRef(arg.variableDcl) but {() => 0};

        match (arg.typeValue) {
            bir:BTypeInt => {
                jvm:methodVisit("var_ins", [LLOAD, argIndex]);
                methodDesc = methodDesc + "J";
            }
            bir:BTypeString => {
                jvm:methodVisit("var_ins", [ALOAD, argIndex]);
                methodDesc = methodDesc + "Ljava/lang/String;";
            }
            bir:BArrayType => {
                jvm:methodVisit("var_ins", [ALOAD, argIndex]);
                methodDesc = methodDesc + "[J";
            }
            any => {
                error err = { message: "JVM generation is not supported for type " +
                                io:sprintf("%s", arg.typeValue)};
                throw err;
            }
        }
    }

    methodDesc = methodDesc + ")Ljava/lang/Object;";

    // call method
    jvm:longTypeVisit("invoke_static", [jvmClass, methodName, methodDesc]);

    // store return
    bir:VariableDcl lhsOpVarDcl = callIns.lhsOp.variableDcl but {() => {}};
    int lhsLndex = getJVMIndexOfVarRef(lhsOpVarDcl) but {() => 0};

    match callIns.lhsOp.typeValue {
        bir:BTypeInt => {
            jvm:longTypeVisit("type_cast_long", []);
            jvm:longTypeVisit("invoke_virtual_long", []);
            jvm:methodVisit("var_ins", [LSTORE, lhsLndex]);
        }
        bir:BTypeString => {
            jvm:stringTypeVisit("checkcast", []);
            jvm:methodVisit("var_ins", [ASTORE, lhsLndex]);
        }
        bir:BArrayType => {
            jvm:longTypeVisit("type_cast_long_array", []);
            jvm:longTypeVisit("type_cast_long_array", []);
            jvm:methodVisit("var_ins", [ASTORE, lhsLndex]);
        }
        any => {
            error err = { message: "JVM generation is not supported for type " +
                            io:sprintf("%s", callIns.lhsOp.typeValue)};
            throw err;
        }
    }

    // goto thenBB
    jvm:labelVisit("goto", [currentFuncName + callIns.thenBB.id.value]);
}


function generateReturnType(bir:BType bType) returns string {
    match bType {
        bir:BTypeNil => return ")V";
        any => {
            return ")Ljava/lang/Object;";
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
        match varDcl.typeValue {
            bir:BTypeInt => {
                localVarIndex = localVarIndex + 2;
            }
            any => {
                localVarIndex = localVarIndex + 1;
            }
        }
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


