/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util.diagnotic;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition;
import org.ballerinalang.util.diagnostic.Diagnostic.Kind;
import org.ballerinalang.util.diagnostic.DiagnosticListener;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @since 0.94
 */
public class DefaultDiagnosticListener implements DiagnosticListener {

    private PrintStream console = System.err;

    private List<Diagnostic> diagnostics =  new ArrayList<>();

    @Override
    public void received(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public void process() {
        diagnostics.sort(Comparator.comparingInt(diag ->  diag.getPosition().getStartLine()));
        diagnostics.forEach(diag -> {
            BDiagnostic bDIag = ((BDiagnostic) diag);
            DiagnosticPosition pos = bDIag.pos;
            Kind kind = bDIag.kind;
            switch (kind) {
                case ERROR:
                    console.println("error: " + pos + " " + bDIag.msg);
                    break;
                case WARNING:
                    console.println("warning: " + pos + " " + bDIag.msg);
                    break;
                case NOTE:
                    break;
            }
        });
    }
}
