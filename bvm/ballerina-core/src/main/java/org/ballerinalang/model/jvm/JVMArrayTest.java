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

import org.ballerinalang.model.jvm.model.types.BArrayType;
import org.ballerinalang.model.jvm.model.types.BMapType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BMap;
import org.ballerinalang.model.jvm.model.values.arrays.BIntArray;
import org.ballerinalang.model.jvm.model.values.arrays.BValueArray;

public class JVMArrayTest {

    public static void main(String[] args) {

        {
            //    int[] a = [1,2,3,4];
            //    a[3] = 5;
            //    a[5] = 7;
            //    int b = a[5];

            BIntArray a = new BIntArray(new long[]{1, 2, 3, 4});
            a.add(3, 5);
            a.add(5, 7);
            long b = a.get(5);
            System.out.println(b);
            System.out.println(a);
        }


        {
            //    map<string> m1 = {"a": "A", "b" : "B"};
            //    map<string> m2 = {"a": "A", "b" : "B"};
            //    map<string>[] marr = [m1, m2, m3];
            //
            //    map<string> m3 = {"a": "A", "b" : "B"};
            //    marr[5] = m3;
            //    map<string> m4 = marr[5];

            BMap<String, String> m1 = new BMap<String, String>(BTypes.typeString) {{
                put("a", "b");
                put("c", "d");
            }};

            BMap<String, String> m2 = new BMap<String, String>(BTypes.typeString) {{
                put("a", "b");
                put("c", "d");
            }};

            BArrayType arrayType = new BArrayType(new BMapType(BTypes.typeString));
            BValueArray<BMap<String, String>> marr = new BValueArray<>(arrayType, new BMap[]{m1, m2});

            BMap<String, String> m3 = new BMap<String, String>(BTypes.typeString) {{
                put("a", "b");
                put("c", "d");
            }};

            marr.add(5, m3);

            BMap<String, String> m4 = marr.get(5);

            System.out.println(m4);
            System.out.println(marr);

        }
    }
}
