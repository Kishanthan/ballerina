/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm;

/**
 * Java based impl for string related operations.
 */
public class StringOperation {
    long stringMatch(String[] content, String pattern) {
        int i = 0;
        int len = content.length;
        int count = 0;
        while (i < len) {
            String line = content[i];
            if (line.matches(pattern)) {
                count = count + 1;
            }
            i = i + 1;
        }
        return count;
    }

    public long execStringMatch(String[] content, String pattern) {
        int i = 0;
        long result = -1;
        while (i < 10) {
            result = stringMatch(content, pattern);
            i = i + 1;
        }

        return result;
    }

    long stringContains(String[] content, String textToSearch) {
        int i = 0;
        int len = content.length;
        while (i < len) {
            String line = content[i];
            if (line.contains(textToSearch)) {
                return i;
            }
            i = i + 1;
        }
        return -1;
    }

    public long execStringContains(String[] content, String textToSearch) {
        int i = 0;
        long result = -1;
        while (i < 100) {
            result = stringContains(content, textToSearch);
            i = i + 1;
        }

        return result;
    }


}
