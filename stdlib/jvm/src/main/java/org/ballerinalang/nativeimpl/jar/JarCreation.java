/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jar;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Native function for handling Jar creation.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "create",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "JarFile", structPackage = "ballerina/jvm"),
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = STRUCT_GENERIC_ERROR,
                        structPackage = BALLERINA_BUILTIN_PKG)
        },
        isPublic = true
)
public class JarCreation extends BlockingNativeCallableUnit {

    private static final String OUTPUT_JAR_NAME = "outputJarName";
    private static final String MANIFEST_ATTRIBUTES = "manifestAttributes";
    private static final String JAR_ENTRIES = "jarEntries";

    private static final Logger log = LoggerFactory.getLogger(JarCreation.class);

    @Override
    public void execute(Context context) {

        try {
            BMap<String, BValue> jarFileStruct = (BMap<String, BValue>) context.getRefArgument(0);
            BString outputJarName = (BString) jarFileStruct.get(OUTPUT_JAR_NAME);
            BMap<String, BValue> manifestAttributes = (BMap<String, BValue>) jarFileStruct.get(MANIFEST_ATTRIBUTES);
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifestAttributes.getMap().forEach((k, v) -> {
                manifest.getMainAttributes().put(new Attributes.Name(k), v.stringValue());
            });

            BMap<String, BValue> jarEntries = (BMap<String, BValue>) jarFileStruct.get(JAR_ENTRIES);

            JarOutputStream target = new JarOutputStream(new FileOutputStream(outputJarName.stringValue()), manifest);

            LinkedHashMap<String, BValue> entries = jarEntries.getMap();

            for (String entryName : entries.keySet()) {
                JarEntry entry = new JarEntry(entryName);
                target.putNextEntry(entry);
                target.write(((BValueArray) entries.get(entryName)).getBytes());
                target.closeEntry();
            }

            target.close();
        } catch (IOException e) {
            String msg = "Jar file creation failed";
            log.error(msg, e);
            context.setReturnValues(BLangVMErrors.createError(context, msg));
        }
    }
}
