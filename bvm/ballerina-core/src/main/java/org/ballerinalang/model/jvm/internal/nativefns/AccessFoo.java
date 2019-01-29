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
package org.ballerinalang.model.jvm.internal.nativefns;

import org.ballerinalang.model.jvm.model.BNativeFunction;
import org.ballerinalang.model.jvm.model.values.BFunction;
import org.ballerinalang.model.jvm.model.values.BObject;


public class AccessFoo extends BNativeFunction {

    //public type Foo object {
    //
    //    public string name = "Foo";
    //    public int age = 12;
    //
    //    public function getFooName() returns string {
    //          return name + " " + age;
    //    }
    //};
    //
    //
    //extern function accessFoo(Foo foo);

    @Override
    public void execute(Object... args) {

        //Approach - 01
        {
            BObject fooObj = (BObject) args[0];
            String name = (String) fooObj.get("name");

            BFunction getFooName = fooObj.getFunction("getFooName");
            getFooName.invoke();
            String fooName = (String) getFooName.__result;
        }
    }
}
