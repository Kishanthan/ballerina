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
import org.ballerinalang.model.jvm.internal.FreezeUtils;
import org.ballerinalang.model.jvm.internal.StampUtils;
import org.ballerinalang.model.jvm.model.types.BArrayType;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.values.BValue;

import java.util.Arrays;

public class BValueArray<T extends BValue> extends BArray {

    private T[] values;

    public BValueArray(BArrayType arrayType) {
        super(arrayType);
    }

    public BValueArray(BArrayType arrayType, T[] values) {
        super(arrayType);
        this.values = values;
    }

    public void add(long index, T value) {
        handleFrozenArrayValue();
        prepareForAdd(index, values.length);
        values[(int) index] = value;
    }

    @Override
    public BValue clone() {
        return new BValueArray<>(arrayType, values.clone());
    }

    public T get(long index) {
        return values[(int) index];
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void stamp(BType type) {
        Arrays.stream(values).forEach(value -> StampUtils.attemptStamp(value, type));
    }

    @Override
    public void freeze(FreezeStatus freezeStatus) {
        Arrays.stream(values).forEach(val -> FreezeUtils.attemptFreeze(val, freezeStatus));
    }

    void grow(int newLength) {
        values = Arrays.copyOf(values, newLength);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
