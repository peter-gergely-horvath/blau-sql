/*
 * Copyright (c) 2017-2025 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 
package com.github.blausql.core.connection;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

public final class StatementResult {

    private final boolean isResultSet;
    private final List<Map<String, Object>> queryResult;
    private final int updateCount;

    StatementResult(boolean isResultSet,
                    List<Map<String, Object>> queryResult, int updateCount) {

        this.isResultSet = isResultSet;
        this.queryResult = queryResult;
        this.updateCount = updateCount;
    }

    public boolean isResultSet() {
        return isResultSet;
    }

    public List<Map<String, Object>> getQueryResult() {
        Assert.isTrue(isResultSet, "Statement yielded update count");
        return queryResult;
    }

    public int getUpdateCount() {
        Assert.isTrue(!isResultSet, "Statement yielded result set");
        return updateCount;
    }
}
