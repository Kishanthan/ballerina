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

import org.ballerinalang.model.jvm.internal.NativeFunctionRegistry;
import org.ballerinalang.model.jvm.model.BNativeFunction;
import org.ballerinalang.model.jvm.model.types.BField;
import org.ballerinalang.model.jvm.model.types.BFunctionType;
import org.ballerinalang.model.jvm.model.types.BObjectType;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BMap;
import org.ballerinalang.model.jvm.model.values.BObject;

import java.util.HashMap;
import java.util.Map;

public class JVMNativeFunctionTest {

    public static void main(String[] args) {

        {
            // int i = 45;
            // string s = "Kicha";
            // float f = 4.5;
            // map<string> m = {"name" : "Kicha", "first": "Avinash"};
            // io:println(i, s, f, m);

            long i = 45;
            String s = "Kicha";
            double f = 4.5;
            BMap<String, String> m = new BMap<String, String>(BTypes.typeString) {{
                put("name", "Kicha");
                put("first", "Avinash");
            }};

            BNativeFunction print = NativeFunctionRegistry.getInstance().get("println");
            print.execute(i, s, f, m);
        }

        {
            //public type Foo object {
            //
            //    public string name = "Foo";
            //    public int age = 12;
            //
            //    public function getFooName() returns string {
            //          return self.name + " " + self.age;
            //    }
            //};
            //
            //
            // Foo foo = internal:createFoo();
            // string name = foo.name;
            // int age = foo.age;

            BObjectType objectType = new BObjectType("Foo", 1);
            BField field1 = new BField(BTypes.typeString, "name", 1);
            BField field2 = new BField(BTypes.typeInt, "age", 1);
            Map<String, BField> fields = new HashMap<>();
            fields.put("age", field2);
            fields.put("name", field1);
            objectType.setFields(fields);

            {
                BType param1 = BTypes.typeString;
                BType param2 = BTypes.typeInt;
                BType[] paramTypes = new BType[]{param1, param2};
                BType[] retParamTypes = new BType[]{};
                BFunctionType __getFooNameFnType = new BFunctionType("getFooName", paramTypes, retParamTypes, 1);
                objectType.addFunctionType(__getFooNameFnType);
            }

            class Foo extends BObject {

                public String name;
                public long age;

                public Foo(BObjectType objectType) {

                    super(objectType);
                }
            }

            BNativeFunction createFoo = NativeFunctionRegistry.getInstance().get("createFoo");
            createFoo.execute(objectType, Foo.class.getName());

            Foo foo = (Foo) createFoo.result;

            System.out.println(foo.name);
        }
    }
}

