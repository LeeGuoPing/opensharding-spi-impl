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

package io.shardingsphere.transaction.base.saga;

import com.google.common.collect.Lists;
import io.shardingsphere.transaction.base.context.TransactionContext;
import io.shardingsphere.transaction.base.context.TransactionContextHolder;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.execute.ShardingExecuteDataMap;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SagaShardingTransactionManagerTest {
    
    private SagaShardingTransactionManager transactionManager;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    @Before
    public void setUp() {
        transactionManager = new SagaShardingTransactionManager();
    }
    
    @After
    public void tearDown() {
        TransactionContextHolder.clear();
    }
    
    @Test
    public void assertInit() {
        Collection<ResourceDataSource> resourceDataSources = Lists.newLinkedList();
        resourceDataSources.add(new ResourceDataSource("ds", dataSource));
        transactionManager.init(DatabaseType.MySQL, resourceDataSources);
        Map<String, DataSource> actual = getDataSourceMap();
        assertThat(actual.get("ds"), is(dataSource));
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(transactionManager.getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertIsInTransaction() {
        assertFalse(transactionManager.isInTransaction());
        TransactionContextHolder.set(new TransactionContext());
        assertTrue(transactionManager.isInTransaction());
    }
    
    @Test
    @SneakyThrows
    public void assertGetConnection() {
        dataSourceMap.put("ds1", dataSource);
        setDataSourceMap(dataSourceMap);
        when(dataSource.getConnection()).thenReturn(connection);
        TransactionContextHolder.set(new TransactionContext());
        Connection actual = transactionManager.getConnection("ds1");
        assertThat(actual, is(connection));
        assertThat(TransactionContextHolder.get().getCachedConnections().get("ds1"), is(connection));
    }
    
    @Test
    public void assertBegin() {
        transactionManager.begin();
        TransactionContext expect = TransactionContextHolder.get();
        assertNotNull(expect);
        TransactionContext actual = (TransactionContext) ShardingExecuteDataMap.getDataMap().get(SagaShardingTransactionManager.SAGA_TRANSACTION_KEY);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void commit() {
    }
    
    @Test
    public void rollback() {
    }
    
    @Test
    public void close() {
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Map<String, DataSource> getDataSourceMap() {
        Field field = transactionManager.getClass().getDeclaredField("dataSourceMap");
        field.setAccessible(true);
        return (Map<String, DataSource>) field.get(transactionManager);
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void setDataSourceMap(final Map<String, DataSource> dataSourceMap) {
        Field field = transactionManager.getClass().getDeclaredField("dataSourceMap");
        field.setAccessible(true);
        field.set(transactionManager, dataSourceMap);
    }
}