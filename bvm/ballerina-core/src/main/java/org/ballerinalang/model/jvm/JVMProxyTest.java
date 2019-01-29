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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JVMProxyTest {

    interface If {

        void originalMethod(String s);
    }

    static class Original implements If {

        public void originalMethod(String s) {

            System.out.println(s);
        }
    }

    static class Handler implements InvocationHandler {

        private final If original;

        public Handler(If original) {

            this.original = original;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException {

            System.out.println("BEFORE");
            method.invoke(original, args);
            System.out.println("AFTER");
            return null;
        }
    }

    public static void main(String[] args) {

        Original original = new Original();
        Handler handler = new Handler(original);
        If f = (If) Proxy.newProxyInstance(If.class.getClassLoader(),
                new Class[]{If.class},
                handler);
        f.originalMethod("Hallo");
    }
}
