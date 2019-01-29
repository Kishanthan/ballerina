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

import org.ballerinalang.model.jvm.model.types.BMapType;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.values.BMap;
import org.ballerinalang.model.jvm.model.values.BValue;

import java.lang.reflect.Constructor;

public class TypeUtils {

    public static boolean checkIsLikeType(Object sourceValue, BType targetType) {

        if (sourceValue instanceof BValue) {
            BValue source = (BValue) sourceValue;
            if (checkIsType(source.getType(), targetType)) {
                return true;
            }

            switch (targetType.getTag()) {
                case BType.MAP_TAG:
                    return checkIsLikeMapType(source, (BMapType) targetType);
                default:
                    return false;
            }
        }

        switch (targetType.getTag()) {
            case BType.INT_TAG:
                if (sourceValue instanceof Long) {
                    return true;
                }
            case BType.STRING_TAG:
                if (sourceValue instanceof String) {
                    return true;
                }
            case BType.BOOLEAN_TAG:
                if (sourceValue instanceof Boolean) {
                    return true;
                }
            default:
                return false;
        }
    }


    private static boolean checkIsType(BType sourceType, BType targetType) {
        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        switch (targetType.getTag()) {
            case BType.INT_TAG:
            case BType.STRING_TAG:
            case BType.BOOLEAN_TAG:
                return sourceType.getTag() == targetType.getTag();
            default:
                return false;
        }
    }


    private static boolean checkIsLikeMapType(BValue sourceValue, BMapType targetType) {

        if (!(sourceValue instanceof BMap)) {
            return false;
        }

        for (Object mapEntry : ((BMap) sourceValue).values()) {
            if (!checkIsLikeType(mapEntry, targetType.getConstrainedType())) {
                return false;
            }
        }
        return true;
    }

    public static <T extends BValue> T convert(Object source, BType targetType) {
        // TODO
        if (source instanceof BValue) {
            BValue sourceVal = (BValue) source;
            BValue clone = sourceVal.clone();
            clone.stamp(targetType);
            return (T) clone;
        }

        // TODO
        return (T) source;
    }
}
