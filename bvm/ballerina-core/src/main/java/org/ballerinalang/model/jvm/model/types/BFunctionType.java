/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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


import java.util.Arrays;

/**
 * {@code {@link BFunctionType }} represents a function type in ballerina.
 *
 */
public class BFunctionType extends BType {

    private String funcName;
    private BType[] paramTypes;
    private BType[] retParamTypes;
    private int flags;

    public BFunctionType(String funcName, BType[] paramTypes, BType[] retParamType, int flags) {
        super("function");
        this.funcName = funcName;
        this.paramTypes = paramTypes;
        this.retParamTypes = retParamType;
        this.flags = flags;
    }

    public void setParamTypes(BType[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public BType[] getParamTypes() {
        return paramTypes;
    }

    public void setRetParamTypes(BType[] retParamTypes) {
        this.retParamTypes = retParamTypes;
    }

    public BType[] getReturnParamTypes() {
        return retParamTypes;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }

    @Override
    public int getTag() {
        return BType.FUNCTION_TYPE_TAG;
    }

    public static String getTypeName(BType[] parameterType, BType[] returnParameterType) {
        return "function (" + (parameterType != null ? getBTypeListAsString(parameterType) : "") + ")"
                + (returnParameterType != null ? " returns (" + getBTypeListAsString(returnParameterType) +
                ")" : "");
    }

    private static String getBTypeListAsString(BType[] typeNames) {
        StringBuilder br = new StringBuilder();
        int i = 0;
        for (BType type : typeNames) {
            br.append(type.typeName);
            if (++i < typeNames.length) {
                br.append(",");
            }
        }
        return br.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BFunctionType)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BFunctionType that = (BFunctionType) o;
        if (!Arrays.equals(paramTypes, that.paramTypes)) {
            return false;
        }
        return Arrays.equals(retParamTypes, that.retParamTypes);
    }


    @Override
    public String toString() {
        return getTypeName(paramTypes, retParamTypes);
    }

}
