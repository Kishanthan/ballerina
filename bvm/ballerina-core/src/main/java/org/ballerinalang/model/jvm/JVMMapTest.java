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

import org.ballerinalang.model.jvm.internal.FreezeStatus;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BMap;

public class JVMMapTest {

    public static void main(String[] args) {

        //    map<string> m1 = {"a": "A", "b" : "B"};
        //    string val = m1.a;
        BMap<String, String> bMap = new BMap<String, String>(BTypes.typeString) {{
            put("a", "A");
            put("b", "B");
        }};

        String val = bMap.get("b");

        System.out.println(val);

        bMap.remove("b");

        bMap.values().forEach(System.out::println);

        bMap.freeze(new FreezeStatus(FreezeStatus.State.FROZEN));
        bMap.stamp(BTypes.typeInt);

        System.out.println(bMap.isFrozen());
        System.out.println(bMap.getType());


        //    map<int> m1 = {"i": 12, "j" : 33};
        //    int val = m1.i;
        BMap<String, Integer> intMap = new BMap<String, Integer>(BTypes.typeInt) {{
            put("i", 12);
            put("j", 33);
        }};

        Integer i = intMap.get("i");
        System.out.println(i);

        intMap.freeze(new FreezeStatus(FreezeStatus.State.FROZEN));
        intMap.stamp(BTypes.typeString);
    }
}
