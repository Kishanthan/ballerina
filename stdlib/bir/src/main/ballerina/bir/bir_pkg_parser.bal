public type PackageParser object {
    BirChannelReader reader;
    TypeParser typeParser;

    public new(reader, typeParser) {
    }

    public function parseVariableDcl() returns VariableDcl {
        var kind = parseVarKind();
        VariableDcl dcl = {
            typeValue: typeParser.parseType(),
            name: { value: reader.readStringCpRef() },
            kind:kind
        };
        return dcl;
    }

    public function parseFunction() returns Function {
        var name = reader.readStringCpRef();
        var isDeclaration = reader.readBoolean();
        var visibility = parseVisibility();
        var typeTag = reader.readInt8();
        if(typeTag != typeParser.TYPE_TAG_INVOKABL_TYPE){
            error err = { message: "Illegal function signature type tag" + typeTag };
            throw err;
        }
        var sig = typeParser.parseInvokableType();
        var argsCount = reader.readInt32();
        var numLocalVars = reader.readInt32();

        VariableDcl[] dcls;
        map<VariableDcl> localVarMap;
        int i;
        while (i < numLocalVars) {
            var dcl = parseVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i++;
        }
        FuncBodyParser bodyParser = new(reader, typeParser, localVarMap);

        BasicBlock[] basicBlocks;
        var numBB = reader.readInt32();
        i = 0;
        while (i < numBB) {
            basicBlocks[i] = bodyParser.parseBB();
            i++;
        }

        return {
            name: { value: name },
            isDeclaration: isDeclaration,
            visibility: visibility,
            localVars: dcls,
            basicBlocks: basicBlocks,
            argsCount: argsCount,
            typeValue: sig
        };
    }

    public function parsePackage() returns Package {
        var pkgIdCp = reader.readInt32();
        var numFuncs = reader.readInt32();
        Function[] funcs;
        int i;
        while (i < numFuncs) {
            funcs[i] = parseFunction();
            i++;
        }
        return { functions:funcs };
    }


    public function parseVisibility() returns Visibility {
        int b = reader.readInt8();
        if (b == 0){
            return "PACKAGE_PRIVATE";
        } else if (b == 1){
            return "PRIVATE";
        } else if (b == 2){
            return "PUBLIC";
        }
        error err = { message: "unknown variable visiblity tag " + b };
        throw err;
    }

    public function parseVarKind() returns VarKind {
        int b = reader.readInt8();
        if (b == 1){
            return "LOCAL";
        } else if (b == 2){
            return "ARG";
        } else if (b == 3){
            return "TEMP";
        } else if (b == 4){
            return "RETURN";
        }
        error err = { message: "unknown var kind tag " + b };
        throw err;
    }

    public function parseSig(string sig) returns BInvokableType {
        BType returnType = "int";
        //TODO: add boolean
        if (sig.lastIndexOf("(N)") == (lengthof sig - 3)){
            returnType = "()";
        }
        return {
            retType:returnType
        };
    }

};

