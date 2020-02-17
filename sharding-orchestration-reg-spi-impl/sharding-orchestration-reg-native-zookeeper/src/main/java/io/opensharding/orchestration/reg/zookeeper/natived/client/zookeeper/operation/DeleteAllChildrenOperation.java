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

package io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.operation;

import io.opensharding.orchestration.reg.zookeeper.natived.client.action.IZookeeperProvider;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.base.BaseOperation;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.KeeperException;

/**
 * Async retry operation which delete all children action.
 *
 * @author lidongbo
 */
public final class DeleteAllChildrenOperation extends BaseOperation {
    
    private final String key;
    
    public DeleteAllChildrenOperation(final IZookeeperProvider provider, final String key) {
        super(provider);
        this.key = key;
    }
    
    @Override
    protected void execute() throws KeeperException, InterruptedException {
        new UsualStrategy(getProvider()).deleteAllChildren(key);
    }
    
    @Override
    public String toString() {
        return String.format("DeleteAllChildrenOperation key: %s", key);
    }
}
