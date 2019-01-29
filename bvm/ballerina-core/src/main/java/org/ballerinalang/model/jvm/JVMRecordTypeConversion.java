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
import org.ballerinalang.model.jvm.model.types.BRecordType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BFunction;
import org.ballerinalang.model.jvm.model.values.BRecord;

import java.util.HashMap;
import java.util.Map;

public class JVMRecordTypeConversion {

    //type Person record {
    //    string name;
    //    int age;
    //};
    //
    //type Employee record {
    //    string name;
    //    int age;
    //    int empNo;
    //};
    //
    //function assignEmployeeToPerson(Employee emp) {
    //    Person|error res = Person.convert(emp);
    //}
    //
    //public function main() {
    //    Employee emp = { name: "Jack Sparrow", age: 54, empNo: 100 };
    //    assignEmployeeToPerson(emp);
    //}

    static final BRecordType personRecordType = new BRecordType("Person", 1);

    {
        final BField field1 = new BField(BTypes.typeString, "name", 1);
        final BField field2 = new BField(BTypes.typeInt, "age", 1);
        final Map<String, BField> fields = new HashMap<>();
        fields.put("name", field1);
        fields.put("age", field2);
        personRecordType.setFields(fields);

    }

    static final BRecordType employeeRecordType = new BRecordType("Employee", 1);

    {
        final BField field1 = new BField(BTypes.typeString, "name", 1);
        final BField field2 = new BField(BTypes.typeInt, "age", 1);
        final BField field3 = new BField(BTypes.typeInt, "empNo", 1);
        final Map<String, BField> fields = new HashMap<>();
        fields.put("name", field1);
        fields.put("age", field2);
        fields.put("empNo", field3);
        employeeRecordType.setFields(fields);

    }


    class convertEmployeeToPerson extends BFunction {
        BRecord _0;
        BRecord _r;

        public void invoke() {
            _r = TypeUtils.convert(_0, personRecordType);
        }
    }

    public void runBallerina() {
        BRecord<String, Object> emp = new BRecord<String, Object>(employeeRecordType) {{
            put("name", "Jack Sparrow");
            put("age", 54);
            put("empNo", 100);
        }};

        convertEmployeeToPerson fn = new convertEmployeeToPerson();
        fn._0 = emp;
        fn.invoke();
        BRecord result = fn._r;
        System.out.println(result);
    }

    public static void main(String[] args) {
        JVMRecordTypeConversion test = new JVMRecordTypeConversion();
        test.runBallerina();
    }
}
