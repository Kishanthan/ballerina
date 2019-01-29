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

import org.ballerinalang.model.jvm.model.types.BTupleType;
import org.ballerinalang.model.jvm.model.types.BType;
import org.ballerinalang.model.jvm.model.types.BTypes;
import org.ballerinalang.model.jvm.model.values.BMap;

import java.util.ArrayList;

public class JVMTupleTypeTest {

    public static void main(String[] args) {

        {
            //  (int, float) r = (4, 5.6);
            //  float f = r[1];

            BTupleType tupleType = new BTupleType(new ArrayList<BType>() {{
                add(BTypes.typeInt);
                add(BTypes.typeString);
            }});

            BMap<Integer, Object> r = new BMap<Integer, Object>(tupleType) {{
                put(0, 4);
                put(1, 5.6);
            }};

            double f = (double) r.get(1);

            System.out.println(f);
        }

        {
            //  ((int, string), float) y = ((23, "Kicha"), 6.5);
            //  string s = (y[0][1]);

            BTupleType tupleType1 = new BTupleType(new ArrayList<BType>() {{
                add(BTypes.typeInt);
                add(BTypes.typeString);
            }});

            BTupleType tupleType2 = new BTupleType(new ArrayList<BType>() {{
                add(tupleType1);
                add(BTypes.typeString);
            }});

            BMap<Integer, Object> y = new BMap<Integer, Object>(tupleType2) {{
                put(0, new BMap<Integer, Object>(tupleType1) {{
                    put(0, 23);
                    put(1, "Kicha");
                }});
                put(1, 6.5);
            }};

            String s = (String) ((BMap) y.get(0)).get(1);

            System.out.println(s);
        }

    }
}
