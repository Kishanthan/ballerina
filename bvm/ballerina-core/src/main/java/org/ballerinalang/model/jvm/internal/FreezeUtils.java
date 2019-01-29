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

import org.ballerinalang.model.jvm.exceptions.BLangFreezeException;
import org.ballerinalang.model.jvm.exceptions.BallerinaErrorReasons;
import org.ballerinalang.model.jvm.exceptions.BallerinaException;
import org.ballerinalang.model.jvm.model.values.BValue;

/**
 * Class for freeze() util methods.
 *
 */
public class FreezeUtils {

    /**
     * Method to check if a value is open to a new freeze attempt.
     *
     * @param currentFreezeStatus   the current freeze status of the value
     * @param receivedFreezeStatus  the received freeze status of the new freeze attempt
     * @return true if the state is unfrozen, false if not. Would throw a {@link BLangFreezeException} if the value
     * is already part of a different freeze attempt.
     */
    public static boolean isOpenForFreeze(FreezeStatus currentFreezeStatus, FreezeStatus receivedFreezeStatus) {
        switch (currentFreezeStatus.getState()) {
            case FROZEN:
                return false;
            case MID_FREEZE:
                if (currentFreezeStatus == receivedFreezeStatus) {
                    return false;
                }
                throw new BallerinaException(BallerinaErrorReasons.CONCURRENT_MODIFICATION_ERROR,
                                             "concurrent 'freeze()' attempts not allowed");
        }
        return true;
    }

    /**
     * Method to handle an update to a value, that is invalid due to a freeze related state.
     *
     * An update to a value would panic either if a value is frozen or if a value is currently in the process of
     * being frozen.
     *
     * @param currentState the current {@link FreezeStatus.State} of the value
     */
    public static void handleInvalidUpdate(FreezeStatus.State currentState) {
        switch (currentState) {
            case FROZEN:
                throw new BLangFreezeException(BallerinaErrorReasons.INVALID_UPDATE_ERROR,
                                               "modification not allowed on frozen value");
            case MID_FREEZE:
                throw new BLangFreezeException(BallerinaErrorReasons.INVALID_UPDATE_ERROR,
                                               "modification not allowed during freeze");
        }
    }

    public static void attemptFreeze(Object value, FreezeStatus freezeStatus) {
        if (value instanceof BValue) {
            ((BValue) value).freeze(freezeStatus);
        }
    }

    public static boolean isValueFrozen(Object value){
        if (value instanceof BValue) {
            return ((BValue) value).isFrozen();
        }

        return true;
    }
}
