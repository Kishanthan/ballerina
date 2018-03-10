/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BCharacter;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BCharacterType} represents character type in Ballerina.
 *
 * @since 0.964
 */
class BCharacterType extends BType {

    /**
     * Create a {@code BCharacterType} which represents the character type.
     *
     * @param typeName string name of the type
     */
    BCharacterType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BInteger.class);
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getZeroValue() {
        return (V) new BCharacter(Character.MIN_VALUE);
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BCharacter(Character.MIN_VALUE);
    }

    @Override
    public TypeSignature getSig() {
        return new TypeSignature(TypeSignature.SIG_CHAR);
    }

    @Override
    public int getTag() {
        return TypeTags.CHAR_TAG;
    }
}
