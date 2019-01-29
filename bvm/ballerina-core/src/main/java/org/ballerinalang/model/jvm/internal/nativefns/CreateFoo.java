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
import org.ballerinalang.model.jvm.model.types.BObjectType;
import org.ballerinalang.model.jvm.model.values.BObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class CreateFoo extends BNativeFunction {

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
    //extern function createFoo() returns Foo;

    @Override
    public void execute(Object... args) {

        //Approach - 01
        {
            BObjectType fooObjType = (BObjectType) args[0];
            BObject fooObj = new BObject(fooObjType);
            fooObj.put("name", "Kicha");
            fooObj.put("age", 12);

            result = fooObj;
        }


        //Approach - 02
        {
            class Foo extends BObject {
                public String name;
                public long age;

                public Foo(BObjectType objectType) {
                    super(objectType);
                }
            }

            BObjectType fooObjType = (BObjectType) args[0];

            Foo foo = new Foo(fooObjType);
            foo.name = "Kicha";
            foo.age = 12;

            result = foo;
        }


        //Approach - 03
        {
            try {
                BObjectType fooObjType = (BObjectType) args[0];
                String clsName = (String) args[1];
                Class<?> cls = Class.forName(clsName);
                Constructor constructor = cls.getConstructor(BObjectType.class);
                constructor.setAccessible(true);
                BObject fooObj = (BObject) constructor.newInstance(fooObjType);
                Field name__field = fooObj.getClass().getDeclaredField("name");
                name__field.setAccessible(true);
                name__field.set(fooObj, "Kicha");

                Field age__field = fooObj.getClass().getDeclaredField("age");
                age__field.setAccessible(true);
                age__field.set(fooObj, 12);

                result = fooObj;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
