import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

BalToJVMIndexMap indexMap = new();
int returnVarRefIndex;

public function main(string... args) {
    //do nothing
}

function genJVMClassFile(byte[] birBinary) returns byte[] {
    io:ByteChannel byteChannel = io:createMemoryChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    bir:ConstPoolParser cpParser = new(reader);
    bir:BirChannelReader birReader = new(reader, cpParser.parse());
    bir:PackageParser p = new(birReader);
    bir:Package pkg = p.parsePackage();
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
        generateMethodDesc(func);
        generateMethodBody(func);
    }
}

function generateMethodDesc(bir:Function func) {
    string name = func.name.value;

    string desc = "(";
    int i;
    while (i < func.argsCount) {
        //todo check the type
        desc = desc + "J";
        i++;
    }

    string returnType = generateReturnType(func.typeValue.retType);

    desc = desc + returnType;

    jvm:classVisit("method", [name, desc]);
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
        io:println("Basic Block Is : ", bb.id.value);

        // create jvm label
        jvm:labelVisit("visit", [bb.id.value]);

        // visit instructions
        int j = 0;
        while(j < lengthof bb.instructions) {
            bir:Instruction inst = bb.instructions[j];
            match inst {
                bir:ConstantLoad constIns => visitConstantLoadIns(constIns);
                bir:Move moveIns => visitMoveIns(moveIns);
                bir:BinaryOp binaryIns => int b = 5; //todo not supported yet
            }
            j++;
        }

        // visit terminator
        visitTerminator(bb, isVoidFunc);
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
    io:println("Const Store Index is :::::::::::", index);
    jvm:methodVisit("var_ins", [LSTORE, index]);
}

function visitMoveIns(bir:Move moveIns) {
    //load
    int rhsIndex = getJVMIndexOfVarRef(moveIns.rhsOp.variableDcl) but {() => 0};
    io:println("RHS Index is :::::::::::", rhsIndex);
    jvm:methodVisit("var_ins", [LLOAD, rhsIndex]);

     //store
    int lhsLndex = getJVMIndexOfVarRef(moveIns.lhsOp.variableDcl) but {() => 0};
    io:println("LHS Index is :::::::::::", lhsLndex);
    jvm:methodVisit("var_ins", [LSTORE, lhsLndex]);
}


function visitTerminator(bir:BasicBlock bb, boolean isVoidFunc) {
    match bb.terminator {
        bir:GOTO gotoIns => genGoToTerm(gotoIns);
        bir:Return returnIns => genReturnTerm(returnIns, isVoidFunc);
        bir:Branch brIns => int a = 4; //todo not supported yet
        bir:Call callIns => int b = 4; //todo not supported yet
    }
}

function genGoToTerm(bir:GOTO gotoIns) {
    jvm:labelVisit("goto", [gotoIns.targetBB.id.value]);
}

function genReturnTerm(bir:Return returnIns, boolean isVoidFunc) {
    if (isVoidFunc) {
        jvm:methodVisit("ins", [RETURN]);
    } else {
        jvm:methodVisit("var_ins", [LLOAD, returnVarRefIndex]);
        jvm:methodVisit("method_ins", []);
        jvm:methodVisit("ins", [ARETURN]);
    }
}

function generateReturnType(bir:BType bType) returns string {
    match bType {
        bir:BTypeInt => return ")Ljava/lang/Object;";
        bir:BTypeBoolean => return ")Ljava/lang/Object;";
        bir:BTypeNil => return ")V";
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
        error err = { message: "Unsupported BIR version " + birVersion + ", supports version " + supportedBirVersion };
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


