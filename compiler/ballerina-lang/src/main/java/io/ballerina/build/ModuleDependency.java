/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.build;

import java.util.Objects;

/**
 * Represents a dependency of a {@code Module}.
 *
 * @since 2.0.0
 */
public class ModuleDependency {
    private final PackageDependency packageDependency;
    private final ModuleId moduleId;

    public ModuleDependency(PackageDependency packageDependency, ModuleId moduleId) {
        this.packageDependency = packageDependency;
        this.moduleId = moduleId;
    }

    public PackageDependency packageDependency() {
        return packageDependency;
    }

    public ModuleId moduleId() {
        return moduleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleDependency that = (ModuleDependency) o;
        return packageDependency.equals(that.packageDependency) &&
                moduleId.equals(that.moduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageDependency, moduleId);
    }
}