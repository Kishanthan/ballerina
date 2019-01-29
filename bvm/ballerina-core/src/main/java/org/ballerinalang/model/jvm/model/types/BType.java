/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.jvm.model.types;

/**
 * {@code BType} represents a type in Ballerina.
 *
 */
public abstract class BType {

    public static final int INT_TAG = 1;
    public static final int BYTE_TAG = INT_TAG + 1;
    public static final int FLOAT_TAG = BYTE_TAG + 1;
    public static final int DECIMAL_TAG = FLOAT_TAG + 1;
    public static final int STRING_TAG = DECIMAL_TAG + 1;
    public static final int BOOLEAN_TAG = STRING_TAG + 1;
    public static final int ANY_TAG = BOOLEAN_TAG + 1;
    public static final int MAP_TAG = ANY_TAG + 1;

    public static final int OBJECT_TYPE_TAG = MAP_TAG + 1;
    public static final int RECORD_TYPE_TAG = OBJECT_TYPE_TAG + 1;
    public static final int FUNCTION_TYPE_TAG = RECORD_TYPE_TAG + 1;

    public static final int ARRAY_TYPE_TAG = FUNCTION_TYPE_TAG + 1;
    public static final int TUPLE_TYPE_TAG = ARRAY_TYPE_TAG + 1;
    public static final int UNION_TYPE_TAG = TUPLE_TYPE_TAG + 1;

    protected String typeName;

    protected BType(String typeName) {
        this.typeName = typeName;
    }

    public abstract int getTag();
    
    public String toString() {
        return typeName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BType) {
            BType other = (BType) obj;
            return this.typeName.equals(other.typeName);
        }

        return false;
    }

    public int hashCode() {
        return typeName.hashCode();
    }
}
