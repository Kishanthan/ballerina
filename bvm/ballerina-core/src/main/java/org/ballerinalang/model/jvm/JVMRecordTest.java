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
import org.ballerinalang.model.jvm.model.types.BRecordType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BRecord;

import java.util.HashMap;
import java.util.Map;

public class JVMRecordTest {

    public static void main(String[] args) {
        //  type Employee record {
        //      int id;
        //      int age;
        //      string name;
        //  };

        BRecordType recordType =  new BRecordType("Employee", 1);
        BField field1 = new BField(BTypes.typeInt, "id", 1);
        BField field2 = new BField(BTypes.typeInt, "age", 1);
        BField field3 = new BField(BTypes.typeString, "name", 1);
        Map<String, BField> fields =  new HashMap<>();
        fields.put("id", field1);
        fields.put("age", field2);
        fields.put("name", field3);
        recordType.setFields(fields);
        recordType.isOpen = true;

//        class Employee<K, V> extends BRecord<K, V> {
//            public Employee(BRecordType recordType) {
//                super(recordType);
//            }
//        }

        BRecord<String, Object> employee =  new BRecord<>(recordType);


        //  Employee employee = {id: 12, age : 34, name : "Kicha"};
        //  int id = employee.id;
        //  int age = employee.age;
        //  string name = employee.name;

        employee.put("id", 12L);
        employee.put("age", 34L);
        employee.put("name", "Kicha");

        long id = (long) employee.get("id");
        long age = (long) employee.get("age");
        String name = (String) employee.get("name");

        System.out.println(id);
        System.out.println(age);
        System.out.println(name);
    }
}
