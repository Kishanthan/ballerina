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
package org.ballerinalang.model.jvm.model.types;

import org.ballerinalang.model.jvm.model.values.BFunction;

import java.util.HashMap;
import java.util.Map;

public class BObjectType extends BType {

    private Map<String, BField> fields;
    private Map<String, BFunctionType> functionTypes = new HashMap<>();
    public int flags;

    public BObjectType(String typeName, int flags) {
        super(typeName);
        this.flags = flags;
    }

    public Map<String, BField> getFields() {
        return fields;
    }

    public void setFields(Map<String, BField> fields) {
        this.fields = fields;
    }

    public void addFunctionType(BFunctionType functionType) {
        this.functionTypes.put(functionType.getFuncName(), functionType);
    }

    public BFunctionType getFunctionType(String functionName) {
        return this.functionTypes.get(functionName);
    }

    @Override
    public int getTag() {
        return OBJECT_TYPE_TAG;
    }
}
