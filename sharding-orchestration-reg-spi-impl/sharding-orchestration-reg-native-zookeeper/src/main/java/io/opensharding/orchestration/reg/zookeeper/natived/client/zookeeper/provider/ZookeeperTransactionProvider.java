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

package io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.provider;

import io.opensharding.orchestration.reg.zookeeper.natived.client.action.IZookeeperTransactionProvider;
import io.opensharding.orchestration.reg.zookeeper.natived.client.utility.ZookeeperConstants;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.base.Holder;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.transaction.BaseTransaction;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.transaction.ZooKeeperTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Provider with transaction.
 *
 * @author lidongbo
 */
public final class ZookeeperTransactionProvider extends BaseZookeeperProvider implements IZookeeperTransactionProvider {
    
    public ZookeeperTransactionProvider(final String rootNode, final Holder holder, final boolean watched, final List<ACL> authorities) {
        super(rootNode, holder, watched, authorities);
    }
    
    @Override
    public void createInTransaction(final String key, final String value, final CreateMode createMode, final BaseTransaction transaction) {
        transaction.create(key, value.getBytes(ZookeeperConstants.UTF_8), getAuthorities(), createMode);
    }
    
    @Override
    public BaseTransaction transaction() {
        return new ZooKeeperTransaction(getRootNode(), getHolder());
    }
}
