/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opensharding.transaction.base.hook.revert.executor.delete;

import com.google.common.base.Optional;
import io.opensharding.transaction.base.hook.revert.GenericSQLBuilder;
import io.opensharding.transaction.base.hook.revert.RevertSQLResult;
import io.opensharding.transaction.base.hook.revert.constant.DefaultKeyword;
import io.opensharding.transaction.base.hook.revert.executor.SQLRevertExecutor;
import io.opensharding.transaction.base.hook.revert.executor.SQLRevertExecutorContext;
import io.opensharding.transaction.base.hook.revert.snapshot.DeleteSnapshotAccessor;

import java.sql.SQLException;
import java.util.Map;

/**
 * Delete SQL revert executor.
 *
 * @author duhongjun
 * @author zhaojun
 */
public final class DeleteSQLRevertExecutor implements SQLRevertExecutor {
    
    private DeleteSQLRevertContext sqlRevertContext;
    
    private final GenericSQLBuilder sqlBuilder = new GenericSQLBuilder();
    
    public DeleteSQLRevertExecutor(final SQLRevertExecutorContext context, final DeleteSnapshotAccessor snapshotAccessor) throws SQLException {
        sqlRevertContext = new DeleteSQLRevertContext(context.getActualTableName(), snapshotAccessor.queryUndoData());
    }
    
    @Override
    public Optional<String> revertSQL() {
        if (sqlRevertContext.getUndoData().isEmpty()) {
            return Optional.absent();
        }
        sqlBuilder.appendLiterals(DefaultKeyword.INSERT);
        sqlBuilder.appendLiterals(DefaultKeyword.INTO);
        sqlBuilder.appendLiterals(sqlRevertContext.getActualTable());
        sqlBuilder.appendInsertValues(sqlRevertContext.getUndoData().iterator().next().size());
        return Optional.of(sqlBuilder.toSQL());
    }
    
    @Override
    public void fillParameters(final RevertSQLResult revertSQLResult) {
        for (Map<String, Object> each : sqlRevertContext.getUndoData()) {
            revertSQLResult.getParameters().add(each.values());
        }
    }
}
