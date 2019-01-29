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
package org.ballerinalang.model.jvm.internal;

import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BValue;

/**
 * Class for stamp() util methods.
 *
 */
public class StampUtils {

    public static void attemptStamp(Object value, BType type) {
        if (value instanceof BValue) {
            ((BValue) value).stamp(type);
        }
    }

    public static BType getType(Object value) {
        if (value instanceof BValue) {
            ((BValue) value).getType();
        }

        if (value instanceof Long) {
            return BTypes.typeInt;
        }

        // TODO : rest of the simple value types

        return null;
    }
}
