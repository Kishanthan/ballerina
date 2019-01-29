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

public class FreezeStatus {
    /**
     * Representation of the current state of a freeze attempt.
     */
    public enum State {
        FROZEN, MID_FREEZE, UNFROZEN;
    }

    private FreezeStatus.State currentState;

    public FreezeStatus(FreezeStatus.State state) {
        this.currentState = state;
    }

    private void setFrozen() {
        this.currentState = FreezeStatus.State.FROZEN;
    }

    private void setUnfrozen() {
        this.currentState = FreezeStatus.State.UNFROZEN;
    }

    public FreezeStatus.State getState() {
        return currentState;
    }

    public boolean isFrozen() {
        return currentState == FreezeStatus.State.FROZEN;
    }

}
