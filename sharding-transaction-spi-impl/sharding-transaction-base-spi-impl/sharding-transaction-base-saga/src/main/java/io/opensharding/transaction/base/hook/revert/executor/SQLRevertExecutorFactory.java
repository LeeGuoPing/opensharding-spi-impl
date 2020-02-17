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

package io.opensharding.transaction.base.hook.revert.executor;

import io.opensharding.transaction.base.hook.revert.executor.delete.DeleteSQLRevertExecutor;
import io.opensharding.transaction.base.hook.revert.executor.insert.InsertSQLRevertExecutor;
import io.opensharding.transaction.base.hook.revert.executor.update.UpdateSQLRevertExecutor;
import io.opensharding.transaction.base.hook.revert.snapshot.DeleteSnapshotAccessor;
import io.opensharding.transaction.base.hook.revert.snapshot.UpdateSnapshotAccessor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;

/**
 * SQL revert executor factory.
 *
 * @author duhongjun
 * @author zhaojun
 */
public final class SQLRevertExecutorFactory {
    
    /**
     * Create new revert SQL executor.
     *
     * @param context SQL revert executor context
     * @return revert SQL engine
     */
    @SneakyThrows
    public static SQLRevertExecutor newInstance(final SQLRevertExecutorContext context) {
        SQLStatement sqlStatement = context.getShardingStatement().getSQLStatement();
        if (sqlStatement instanceof InsertStatement) {
            return new InsertSQLRevertExecutor(context);
        } else if (sqlStatement instanceof DeleteStatement) {
            return new DeleteSQLRevertExecutor(context, new DeleteSnapshotAccessor(context));
        } else if (sqlStatement instanceof UpdateStatement) {
            return new UpdateSQLRevertExecutor(context, new UpdateSnapshotAccessor(context));
        } else {
            throw new UnsupportedOperationException("unsupported SQL statement");
        }
    }
}
