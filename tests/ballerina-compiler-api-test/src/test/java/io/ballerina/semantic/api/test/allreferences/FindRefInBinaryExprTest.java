/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.allreferences;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for the find all references API when the references are in binary expressions.
 *
 * @since 2.0.0
 */
@Test
public class FindRefInBinaryExprTest extends FindAllReferencesTest {

    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {22, 12, List.of(location(22, 12, 13),
                                 location(23, 21, 22),
                                 location(29, 37, 38),
                                 location(32, 25, 26))
                },
                {23, 25, List.of(location(19, 8, 9),
                                 location(23, 25, 26),
                                 location(29, 29, 30),
                                 location(35, 21, 22))
                },
                {16, 4, List.of(location(16, 4, 5),
                                location(19, 17, 18)),
                },
                {29, 20, List.of(location(29, 20, 21))},
                {29, 19, EMPTY_LIST}
        };
    }

    @Override
    public String getFileName() {
        return "find_var_ref_in_binary_expr.bal";
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_in_binary_expr.bal";
    }
}
