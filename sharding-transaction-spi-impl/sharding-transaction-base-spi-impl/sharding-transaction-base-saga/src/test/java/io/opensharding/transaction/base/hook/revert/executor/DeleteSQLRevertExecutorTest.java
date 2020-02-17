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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.opensharding.transaction.base.hook.revert.RevertSQLResult;
import io.opensharding.transaction.base.hook.revert.executor.delete.DeleteSQLRevertExecutor;
import io.opensharding.transaction.base.hook.revert.snapshot.DeleteSnapshotAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteSQLRevertExecutorTest {
    
    @Mock
    private DeleteSnapshotAccessor snapshotAccessor;
    
    @Mock
    private SQLRevertExecutorContext executorContext;
    
    private DeleteSQLRevertExecutor deleteSQLRevertExecutor;
    
    private List<Map<String, Object>> undoData = Lists.newLinkedList();
    
    private RevertSQLResult revertSQLResult = new RevertSQLResult("");
    
    @Before
    public void setUp() throws SQLException {
        when(executorContext.getActualTableName()).thenReturn("t_order_0");
        when(snapshotAccessor.queryUndoData()).thenReturn(undoData);
        addUndoData();
    }
    
    private void addUndoData() {
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("order_id", i);
            record.put("user_id", i);
            record.put("status", "init");
            undoData.add(record);
        }
    }
    
    @Test
    public void assertGenerateSQL() throws SQLException {
        deleteSQLRevertExecutor = new DeleteSQLRevertExecutor(executorContext, snapshotAccessor);
        Optional<String> actual = deleteSQLRevertExecutor.revertSQL();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("INSERT INTO t_order_0 VALUES (?,?,?)"));
    }
    
    @Test
    public void assertGenerateRevertSQLWithoutUndoData() throws SQLException {
        when(snapshotAccessor.queryUndoData()).thenReturn(Lists.<Map<String, Object>>newLinkedList());
        deleteSQLRevertExecutor = new DeleteSQLRevertExecutor(executorContext, snapshotAccessor);
        Optional<String> actual = deleteSQLRevertExecutor.revertSQL();
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertFillParameters() throws SQLException {
        deleteSQLRevertExecutor = new DeleteSQLRevertExecutor(executorContext, snapshotAccessor);
        deleteSQLRevertExecutor.fillParameters(revertSQLResult);
        assertThat(revertSQLResult.getParameters().size(), is(10));
        assertThat(revertSQLResult.getParameters().iterator().next().size(), is(3));
    }
}
