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
