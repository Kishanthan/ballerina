/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.stdlib.streams.negative;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test if the proper error is thrown if the output attributes are used in select clause.
 *
 * @since 0.990.1
 */
public class OutputFieldsOnlyInHavingAndOrderByTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/negative/output-field-not-allowed-in-select.bal");
    }

    @Test
    public void testOutputputFieldInSelect() {
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "undefined symbol 'name'", 64, 16);
        BAssertUtil.validateError(result, 1, "alias not defined for expression in select clause", 64, 16);
        BAssertUtil.validateError(result, 2, "fields defined in select clause, incompatible with output fields" +
                " in type 'Teacher', expected '[name(string), age(int), status(string), " +
                "batch(string), school(string)]' but found '[school(string), batch(string), age(int), status(string)]'",
                65, 9);
    }
}
