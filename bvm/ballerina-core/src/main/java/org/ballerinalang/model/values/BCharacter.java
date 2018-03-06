/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

/**
 * The {@code BCharacter} represents a char value in Ballerina.
 *
 * @since 0.964
 */
public final class BCharacter extends BValueType implements BRefType<Character> {

    private char value;

    public BCharacter(char value) {
        this.value = value;
    }

    @Override
    public long intValue() {
        return this.value;
    }

    @Override
    public char charValue() {
        return this.value;
    }

    @Override
    public double floatValue() {
        return (double) this.value;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public byte[] blobValue() {
        return null;
    }

    @Override
    public String stringValue() {
        return String.valueOf(value);
    }

    @Override
    public BType getType() {
        return BTypes.typeChar;
    }

    @Override
    public boolean equals(Object obj) {
        return ((BCharacter) obj).charValue() == value;
    }

    @Override
    public Character value() {
        return value;
    }

    @Override
    public BValue copy() {
        return new BCharacter(value);
    }
}
