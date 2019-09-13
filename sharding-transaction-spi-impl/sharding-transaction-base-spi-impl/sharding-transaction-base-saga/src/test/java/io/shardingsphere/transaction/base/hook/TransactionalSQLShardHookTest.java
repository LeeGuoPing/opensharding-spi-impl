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

package io.shardingsphere.transaction.base.hook;

import io.shardingsphere.transaction.base.context.ShardingSQLTransaction;
import io.shardingsphere.transaction.base.context.TransactionContextHolder;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TransactionalSQLShardHookTest {
    
    @Mock
    private ShardingSQLTransaction sagaTransaction;
    
    @Mock
    private SQLRouteResult sqlRouteResult;
    
    @Mock
    private TableMetas tableMetas;
    
    
    private final TransactionalSQLRoutingHook sagaSQLShardHook = new TransactionalSQLRoutingHook();
    
    @Before
    public void setUp() {
        TransactionContextHolder.set(sagaTransaction);
    }
    
    @After
    public void tearDown() {
        TransactionContextHolder.clear();
    }
    
    @Test
    public void assertFinishSuccess() {
        sagaSQLShardHook.start("logicSQL");
        sagaSQLShardHook.finishSuccess(sqlRouteResult, tableMetas);
        verify(sagaTransaction).nextLogicSQLTransaction("logicSQL");
    }
    
    @Test
    public void assertFinishFailure() {
        sagaSQLShardHook.finishFailure(mock(Exception.class));
    }
}
