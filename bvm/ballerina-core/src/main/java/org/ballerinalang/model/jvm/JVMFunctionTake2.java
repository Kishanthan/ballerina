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

import org.ballerinalang.model.jvm.model.types.BField;
import org.ballerinalang.model.jvm.model.types.BFunctionType;
import org.ballerinalang.model.jvm.model.types.BObjectType;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BFunction;
import org.ballerinalang.model.jvm.model.values.BObject;

import java.util.HashMap;
import java.util.Map;

public class JVMFunctionTake2 {

    public static void main(String[] args) {

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
        //        function getFullName() returns string {
        //            return self.firstName + " " + self.lastName;
        //        }
        //
        //        function checkAndModifyAge(int condition, int a);
        //    };
        //
        //    function Person.checkAndModifyAge(int condition, int a) {
        //        if (self.age < condition) {
        //            self.age = a;
        //        }
        //    }

        BObjectType objectType = new BObjectType("PersonObj", 1);
        BField field1 = new BField(BTypes.typeInt, "age", 1);
        BField field2 = new BField(BTypes.typeString, "firstName", 1);
        BField field3 = new BField(BTypes.typeString, "lastName", 1);
        Map<String, BField> fields = new HashMap<>();
        fields.put("age", field1);
        fields.put("firstName", field2);
        fields.put("lastName", field3);
        objectType.setFields(fields);

        class Person__init extends BFunction {
            long _0;
            String _1;
            String _2;

            public void invoke() {

                self.put("age", _0);
                self.put("firstName", _1);
                self.put("lastName", _2);
            }
        }

        {
            BType param1 = BTypes.typeInt;
            BType param2 = BTypes.typeString;
            BType param3 = BTypes.typeString;
            BType[] paramTypes = new BType[]{param1, param2, param3};
            BType[] retParamTypes = new BType[]{};
            BFunctionType __initFnType = new BFunctionType("Person__init", paramTypes, retParamTypes, 1);
            objectType.addFunctionType(__initFnType);

        }

        class getFullName extends BFunction {
            String _ret;

            public void invoke() {

                _ret = self.get("firstName") + " " + self.get("lastName");
            }
        }

        {
            BType[] paramTypes = new BType[]{};
            BType retParam1 = BTypes.typeString;
            BType[] retParamTypes = new BType[]{retParam1};
            BFunctionType getFullNameFnType = new BFunctionType("getFullName", paramTypes, retParamTypes, 1);
            objectType.addFunctionType(getFullNameFnType);

        }

        class checkAndModifyAge extends BFunction {
            long _0;
            long _1;

            public void invoke() {
                if (((long) self.get("age")) < _0) {
                    self.put("age", _1);
                }
            }
        }


        //    PersonObj personObj =  new(12, "Kicha", "Avinash");
        //    string fullName = personObj.getFullName();
        //    personObj.checkAndModifyAge(14, 15);
        //    int age = person.age;

        BObject personObj = new BObject(objectType);
        Person__init person__init = new Person__init();
        person__init.self = personObj;

        person__init._0 = 12;
        person__init._1 = "Kicha";
        person__init._2 = "Avinash";
        person__init.invoke();

        getFullName getFullName = new getFullName();
        getFullName.invoke();
        String fullName = getFullName._ret;
        System.out.println(fullName);

        checkAndModifyAge checkAndModifyAge = new checkAndModifyAge();
        checkAndModifyAge._0 = 14;
        checkAndModifyAge._1 = 15;
        checkAndModifyAge.invoke();

        long age = (long) personObj.get("age");
        System.out.println(age);
    }
}

