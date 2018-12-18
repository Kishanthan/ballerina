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
package org.ballerinalang.compiler.backend.jvm;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.bir.BIREmitter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.writer.BIRBinaryWriter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Ballerina compiler JVM backend.
 */
public class JVMCodeGen {
    private static PrintStream out = System.out;
    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_jvm.balx";
    private String CLASS_FILE_NAME = "DEFAULT.class";


    public void genJVMExecutable(Path projectPath, String progPath, String targetPath) {
        CLASS_FILE_NAME = progPath.substring(0, progPath.indexOf("."));
        BLangPackage bLangPackage = compileProgram(projectPath, progPath);
        if (bLangPackage.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        BIRNode.BIRPackage bir = bLangPackage.symbol.bir;

        BIREmitter birEmitter = new BIREmitter();
        String birText = birEmitter.emit(bir);
        out.println(birText);

        generateJVMClassFile(bir, targetPath);
    }

    private void generateJVMClassFile(BIRNode.BIRPackage bir, String targetPath) {
        URI resURI = getExecResourceURIFromThisJar();

        byte[] resBytes = readExecResource(resURI);

        ProgramFile programFile = loadProgramFile(resBytes);

        Path outputPath = Paths.get(targetPath);

        Path classFileOutputPath = outputPath.resolve(CLASS_FILE_NAME.concat(".class"));

        BValue[] args = new BValue[2];
        BIRBinaryWriter binaryWriter = new BIRBinaryWriter(bir);
        args[0] = new BByteArray(binaryWriter.serialize());
        args[1] = new BString(CLASS_FILE_NAME);

        // Generate the class file
        try {
            Debugger debugger = new Debugger(programFile);
            programFile.setDebugger(debugger);
            BValue[] result = BLangFunctions.invokeEntrypointCallable(programFile, programFile.getEntryPkgName(),
                    "genJVMClassFile", args);
            BByteArray bvmBytes = (BByteArray) result[0];
            byte[] classBytes = bvmBytes.getBytes();

            Files.write(classFileOutputPath, classBytes);
        } catch (Exception e) {
            throw new BLangCompilerException("jvm class file generation failed: " + e.getMessage(), e);
        }
    }

    private BLangPackage compileProgram(Path projectPath, String progPath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(OFFLINE, Boolean.toString(true));
        options.put(LOCK_ENABLED, Boolean.toString(true));

        Compiler compiler = Compiler.getInstance(context);
        return compiler.build(progPath);
    }

    private URI getExecResourceURIFromThisJar() {
        URI resURI;
        try {
            URL resourceURL = JVMCodeGen.class.getClassLoader().getResource("META-INF/ballerina/" +
                    EXEC_RESOURCE_FILE_NAME);
            if (resourceURL == null) {
                throw new BLangCompilerException("missing embedded executable resource: " + EXEC_RESOURCE_FILE_NAME);
            }
            resURI = resourceURL.toURI();
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
        return resURI;
    }

    private byte[] readExecResource(URI resURI) {
        byte[] resBytes;
        try {
            resBytes = Files.readAllBytes(Paths.get(resURI));
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
        return resBytes;
    }

    private ProgramFile loadProgramFile(byte[] resBytes) {
        try (ByteArrayInputStream byteAIS = new ByteArrayInputStream(resBytes)) {
            ProgramFileReader programFileReader = new ProgramFileReader();
            return programFileReader.readProgram(byteAIS);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
    }
}
