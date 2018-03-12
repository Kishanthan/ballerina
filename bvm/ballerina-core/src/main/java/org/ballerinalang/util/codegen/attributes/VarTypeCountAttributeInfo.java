/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.codegen.attributes;

import static org.ballerinalang.util.BLangConstants.BLOB_OFFSET;
import static org.ballerinalang.util.BLangConstants.BOOL_OFFSET;
import static org.ballerinalang.util.BLangConstants.BYTE_OFFSET;
import static org.ballerinalang.util.BLangConstants.CHAR_OFFSET;
import static org.ballerinalang.util.BLangConstants.FLOAT_OFFSET;
import static org.ballerinalang.util.BLangConstants.INT_OFFSET;
import static org.ballerinalang.util.BLangConstants.NO_OF_VAR_TYPE_CATEGORIES;
import static org.ballerinalang.util.BLangConstants.REF_OFFSET;
import static org.ballerinalang.util.BLangConstants.STRING_OFFSET;

/**
 * {@code VariableCountAttributeInfo} contains global variables/constants details.
 *
 * @since 0.90
 */
public class VarTypeCountAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    private int[] varTypeCount = new int[NO_OF_VAR_TYPE_CATEGORIES];

    public VarTypeCountAttributeInfo(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public int getMaxLongVars() {
        return this.varTypeCount[INT_OFFSET];
    }

    public void setMaxLongVars(int maxLongVars) {
        this.varTypeCount[INT_OFFSET] = maxLongVars;
    }

    public int getMaxDoubleVars() {
        return this.varTypeCount[FLOAT_OFFSET];
    }

    public void setMaxDoubleVars(int maxDoubleVars) {
        this.varTypeCount[FLOAT_OFFSET] = maxDoubleVars;
    }

    public int getMaxStringVars() {
        return this.varTypeCount[STRING_OFFSET];
    }

    public void setMaxStringVars(int maxStringVars) {
        this.varTypeCount[STRING_OFFSET] = maxStringVars;
    }

    public int getMaxIntVars() {
        return this.varTypeCount[BOOL_OFFSET];
    }

    public void setMaxIntVars(int maxIntVars) {
        this.varTypeCount[BOOL_OFFSET] = maxIntVars;
    }

    public int getMaxCharVars() {
        return this.varTypeCount[CHAR_OFFSET];
    }

    public void setMaxCharVars(int maxCharVars) {
        this.varTypeCount[CHAR_OFFSET] = maxCharVars;
    }

    public int getMaxByteVars() {
        return this.varTypeCount[BYTE_OFFSET];
    }

    public void setMaxByteVars(int maxByteVars) {
        this.varTypeCount[BYTE_OFFSET] = maxByteVars;
    }


    public int getMaxBlobVars() {
        return this.varTypeCount[BLOB_OFFSET];
    }

    public void setMaxBlobVars(int maxByteVars) {
        this.varTypeCount[BLOB_OFFSET] = maxByteVars;
    }

    public int getMaxRefVars() {
        return this.varTypeCount[REF_OFFSET];
    }

    public void setMaxRefVars(int maxRefVars) {
        this.varTypeCount[REF_OFFSET] = maxRefVars;
    }

    public int[] getVarTypeCount() {
        return varTypeCount;
    }

    @Override
    public Kind getKind() {
        return Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}
