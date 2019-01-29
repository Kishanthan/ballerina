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
package org.ballerinalang.model.jvm.model.values.arrays;

import org.ballerinalang.model.jvm.internal.FreezeStatus;
import org.ballerinalang.model.jvm.model.types.BArrayType;
import org.ballerinalang.model.jvm.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import static org.ballerinalang.model.jvm.internal.FreezeUtils.handleInvalidUpdate;

public abstract class BArray implements BValue {

    protected volatile FreezeStatus freezeStatus = new FreezeStatus(FreezeStatus.State.UNFROZEN);

    private int maxArraySize = Integer.MAX_VALUE - 8;
    protected int size = 0;

    protected BArrayType arrayType;

    BArray(BArrayType arrayType) {
        this.arrayType = arrayType;
    }

    void handleFrozenArrayValue() {
        synchronized (this) {
            if (freezeStatus.getState() != FreezeStatus.State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
        }
    }

    void prepareForAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }

    private void ensureCapacity(int requestedCapacity, int currentArraySize) {
        //TODO - used same logic as BVM array size growing logic
        if ((requestedCapacity) - currentArraySize >= 0) {
            // Here the growth rate is 1.5. This value has been used by many other languages
            int newArraySize = currentArraySize + (currentArraySize >> 1);

            // Now get the maximum value of the calculate new array size and request capacity
            newArraySize = Math.max(newArraySize, requestedCapacity);

            // Now get the minimum value of new array size and maximum array size
            newArraySize = Math.min(newArraySize, maxArraySize);
            grow(newArraySize);
        }
    }

    abstract void grow(int newLength);

    private void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        if ((int) index < 0 || index >= maxArraySize) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    public abstract BValue clone();
}
