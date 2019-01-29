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
package org.ballerinalang.model.jvm.model.types;

public class BTypes {

    public static BType typeInt = new BIntegerType();
    public static BType typeFloat = new BFloatType();
    public static BType typeString = new BStringType();
    public static BType typeBoolean = new BBooleanType();
    public static BType typeAny = new BAnyType();
    public static BType typeMap = new BMapType(typeAny);

    public static BArrayType typeIntArray = new BArrayType(typeInt);

    private BTypes() {

    }
}
