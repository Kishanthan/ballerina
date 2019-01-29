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
package org.ballerinalang.model.jvm.model.values;

import org.ballerinalang.model.jvm.exceptions.BLangFreezeException;
import org.ballerinalang.model.jvm.internal.FreezeStatus;
import org.ballerinalang.model.jvm.model.types.BType;

/**
 * {@code BValue} represents any value in Ballerina.
 *
 */
public interface BValue {

    BType getType();

    void stamp(BType type);

    BValue clone();

    /**
     * Method to attempt freezing a {@link BValue}, to disallow further modification.
     *
     * @param freezeStatus  the {@link FreezeStatus} instance to keep track of the
     *                      freeze result of this attempt
     */
    default void freeze(FreezeStatus freezeStatus) {
        throw new BLangFreezeException("freeze not allowed on '" + getType() + "'");
    }

    /**
     * Method to retrieve if the {@link BValue} is frozen, if applicable. Compile time checks ensure that the check
     * is only possible on structured basic types.
     *
     * @return Whether the value is frozen
     */
    default boolean isFrozen() {
        return false;
    }
}
