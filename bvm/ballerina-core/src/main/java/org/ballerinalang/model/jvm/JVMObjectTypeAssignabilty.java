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

public class JVMObjectTypeAssignabilty {

    //type Person object {
    //    string name;
    //    int age;
    //
    //    function Person__init(string name, int age) {
    //        self.name = name;
    //        self.age = age;
    //    }
    //};
    //
    //type Employee object {
    //    string name;
    //    int age;
    //    int empNo;
    //
    //    function Person__init(string name, int age, int empNo) {
    //        self.name = name;
    //        self.age = age;
    //        self.empNo = empNo;
    //    }
    //};
    //
    //function assignEmployeeToPerson() {
    //    Person p = new Employee("Jack Sparrow", 54, 100);
    //}
    //
    //public function main() {
    //    assignEmployeeToPerson();
    //}

    static final BObjectType personObjectType = new BObjectType("Person", 1);

    {
        final BField field1 = new BField(BTypes.typeString, "name", 1);
        final BField field2 = new BField(BTypes.typeInt, "age", 1);
        final Map<String, BField> fields = new HashMap<>();
        fields.put("name", field1);
        fields.put("age", field2);
        personObjectType.setFields(fields);

        BType param1 = BTypes.typeString;
        BType param2 = BTypes.typeInt;
        BType[] paramTypes = new BType[]{param1, param2};
        BType[] retParamTypes = new BType[]{};
        BFunctionType __initFnType = new BFunctionType("Person__init", paramTypes, retParamTypes, 1);
        personObjectType.addFunctionType(__initFnType);
    }

    class Person__init extends BFunction {
        String _0;
        long _1;

        public void invoke() {

            self.put("name", _0);
            self.put("age", _1);
        }
    }


    static final BObjectType employeeObjectType = new BObjectType("Employee", 1);

    {
        final BField field1 = new BField(BTypes.typeString, "name", 1);
        final BField field2 = new BField(BTypes.typeInt, "age", 1);
        final BField field3 = new BField(BTypes.typeInt, "empNo", 1);
        final Map<String, BField> fields = new HashMap<>();
        fields.put("name", field1);
        fields.put("age", field2);
        fields.put("empNo", field3);
        employeeObjectType.setFields(fields);

        BType param1 = BTypes.typeString;
        BType param2 = BTypes.typeInt;
        BType param3 = BTypes.typeInt;
        BType[] paramTypes = new BType[]{param1, param2, param3};
        BType[] retParamTypes = new BType[]{};
        BFunctionType __initFnType = new BFunctionType("Person__init", paramTypes, retParamTypes, 1);
        employeeObjectType.addFunctionType(__initFnType);

    }

    class Employee__init extends BFunction {

        String _0;
        long _1;
        long _2;

        public void invoke() {

            self.put("name", _0);
            self.put("age", _1);
            self.put("empNo", _2);
        }
    }

    class assignEmployeeToPerson extends BFunction {
        public void invoke() {
            //type assign
        }
    }

    public void runBallerina() {
        BObject emp = new BObject(employeeObjectType);

        Employee__init employee__init = new Employee__init();
        employee__init._0 = "Jack Sparrow";
        employee__init._1 = 54;
        employee__init._2 = 100;
        employee__init.invoke();

        assignEmployeeToPerson fn = new assignEmployeeToPerson();
        fn.invoke();
    }

    public static void main(String[] args) {
        JVMObjectTypeAssignabilty test = new JVMObjectTypeAssignabilty();
        test.runBallerina();
    }
}
