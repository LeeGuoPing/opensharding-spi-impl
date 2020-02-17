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

package io.opensharding.transaction;

import io.opensharding.transaction.fixture.ShardingTransactionalTestService;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:shardingTransactionTest.xml")
public class ShardingTransactionalNameSpaceTest extends AbstractJUnit4SpringContextTests {
    
    @Autowired
    private ShardingTransactionalTestService testService;
    
    @Test
    public void assertChangeTransactionTypeToXA() {
        testService.testChangeTransactionTypeToXA();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertChangeTransactionTypeToBASE() {
        testService.testChangeTransactionTypeToBASE();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertChangeTransactionTypeToLocal() {
        TransactionTypeHolder.set(TransactionType.XA);
        testService.testChangeTransactionTypeToLOCAL();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertChangeTransactionTypeInClass() {
        testService.testChangeTransactionTypeInClass();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
}
