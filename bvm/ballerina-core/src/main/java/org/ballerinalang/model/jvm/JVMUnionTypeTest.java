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
package org.ballerinalang.model.jvm;

import org.ballerinalang.model.jvm.internal.TypeUtils;
import org.ballerinalang.model.jvm.model.types.BField;
import org.ballerinalang.model.jvm.model.types.BFunctionType;
import org.ballerinalang.model.jvm.model.types.BObjectType;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BFunction;
import org.ballerinalang.model.jvm.model.values.BObject;

import java.util.HashMap;
import java.util.Map;

public class JVMUnionTypeTest {

    public static void main(String[] args) {

        {
            //    (int | string | boolean) w = "Kicha";
            Object w = "Kicha";

            //    boolean b = w is string;
            boolean b = TypeUtils.checkIsLikeType(w, BTypes.typeString);

            System.out.println(b);
        }

        {
            //    type PersonObj object {
            //        public int age;
            //        public string firstName;
            //        public string lastName;
            //
            //        function Person__init(int age, string firstName, string lastName) {
            //            self.age = age;
            //            self.firstName = firstName;
            //            self.lastName = lastName;
            //        }
            //
            //        function getFullName() returns string {
            //            return self.firstName + " " + self.lastName;
            //        }
            //    };
            //
            //
            //    (PersonObj | string | int) a = new PersonObj(12, "Kicha", "Avi");

            BObjectType objectType = new BObjectType("PersonObj", 1);
            BField field1 = new BField(BTypes.typeInt, "age", 1);
            BField field2 = new BField(BTypes.typeString, "firstName", 1);
            BField field3 = new BField(BTypes.typeString, "lastName", 1);
            Map<String, BField> fields = new HashMap<>();
            fields.put("age", field1);
            fields.put("firstName", field2);
            fields.put("lastName", field3);
            objectType.setFields(fields);

            {
                BType param1 = BTypes.typeInt;
                BType param2 = BTypes.typeString;
                BType param3 = BTypes.typeString;
                BType[] paramTypes = new BType[]{param1, param2, param3};
                BType[] retParamTypes = new BType[]{};
                BFunctionType __initFnType = new BFunctionType("Person__init", paramTypes, retParamTypes, 1);
                objectType.addFunctionType(__initFnType);
            }

            {
                BType[] paramTypes = new BType[]{};
                BType retParam1 = BTypes.typeString;
                BType[] retParamTypes = new BType[]{retParam1};
                BFunctionType getFullNameFnType = new BFunctionType("getFullName", paramTypes, retParamTypes, 1);
                objectType.addFunctionType(getFullNameFnType);
            }

            class PersonObj extends BObject {
                public PersonObj(BObjectType objectType) {
                    super(objectType);
                    functions.put("Person__init", new __init());
                    functions.put("getFullName", new getFullName());
                }

                class __init extends BFunction {
                    long age;
                    String firstName;
                    String lastName;

                    public void invoke() {
                        put("age", age);
                        put("firstName", firstName);
                        put("lastName", lastName);
                    }
                }

                class getFullName extends BFunction {
                    String res;

                    public void invoke() {
                        res = get("firstName") + " " + get("lastName");
                    }
                }
            }

            Object personObj = new PersonObj(objectType);
        }
    }
}
