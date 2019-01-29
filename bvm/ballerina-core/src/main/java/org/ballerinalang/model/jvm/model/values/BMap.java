/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.jvm.model.values;

import org.ballerinalang.model.jvm.exceptions.BLangFreezeException;
import org.ballerinalang.model.jvm.internal.FreezeStatus;
import org.ballerinalang.model.jvm.internal.FreezeUtils;
import org.ballerinalang.model.jvm.internal.StampUtils;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.types.BTypes;

import java.util.HashMap;

import static org.ballerinalang.model.jvm.internal.FreezeUtils.handleInvalidUpdate;
import static org.ballerinalang.model.jvm.internal.FreezeUtils.isOpenForFreeze;

/**
 * {@code MapType} represents a map.
 *
 * @param <K> Key
 * @param <V> Value
 */
@SuppressWarnings("rawtypes")
public class BMap<K, V> extends HashMap<K, V> implements BValue {

    private BType type = BTypes.typeMap;
    private volatile FreezeStatus freezeStatus = new FreezeStatus(FreezeStatus.State.UNFROZEN);

    public BMap() {}

    public BMap(BType type) {
        this.type = type;
    }

    /**
     * Clear map entries.
     */
    public void clear() {

        if (freezeStatus.getState() != FreezeStatus.State.UNFROZEN) {
            handleInvalidUpdate(freezeStatus.getState());
        }
        super.clear();
    }

    @Override
    public BMap<K, V> clone() {
        return (BMap<K, V>) super.clone();
    }

    /**
     * Remove an item from the map.
     *
     * @param key key of the item to be removed
     * @return boolean to indicate whether given key is removed.
     */
    public V remove(Object key) {

        if (freezeStatus.getState() != FreezeStatus.State.UNFROZEN) {
            handleInvalidUpdate(freezeStatus.getState());
        }

        return super.remove(key);

    }

    @Override
    public BType getType() {
        return this.type;
    }

    @Override
    public void stamp(BType type) {
        this.values().forEach(val -> StampUtils.attemptStamp(val, type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void freeze(FreezeStatus freezeStatus) {

        if (this.type.getTag() == BType.OBJECT_TYPE_TAG) {
            throw new BLangFreezeException("'freeze()' not allowed on '" + getType() + "'");
        }

        if (isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
            this.values().forEach(val -> {
                FreezeUtils.attemptFreeze(val, freezeStatus);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFrozen() {
        return this.freezeStatus.isFrozen();
    }
}
