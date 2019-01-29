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
package org.ballerinalang.model.jvm.internal;

import org.ballerinalang.model.jvm.internal.nativefns.BPrintln;
import org.ballerinalang.model.jvm.internal.nativefns.CreateFoo;
import org.ballerinalang.model.jvm.model.BNativeFunction;

import java.util.HashMap;
import java.util.Map;

public class NativeFunctionRegistry {

    private static NativeFunctionRegistry instance = new NativeFunctionRegistry();

    private final Map<String, BNativeFunction> nativeFunctions = new HashMap<>();

    private NativeFunctionRegistry() {
        this.nativeFunctions.put("println", new BPrintln());
        this.nativeFunctions.put("createFoo", new CreateFoo());
    }

    public static NativeFunctionRegistry getInstance() {
        return instance;
    }

    public void register(String functionName, BNativeFunction nativeFunction) {
        this.nativeFunctions.put(functionName, nativeFunction);
    }

    public BNativeFunction get(String functionName) {
        return this.nativeFunctions.get(functionName);
    }
}
