// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public extern function classVisit(string visitType, string[] args);

public extern function methodVisit(string visitType, int[] args);

public extern function labelVisit(string visitType, string[] args);

public extern function getClassFileContent() returns byte[];

public extern function longTypeVisit(string visitType, string[] args);

public extern function stringTypeVisit(string visitType, string[] args);
