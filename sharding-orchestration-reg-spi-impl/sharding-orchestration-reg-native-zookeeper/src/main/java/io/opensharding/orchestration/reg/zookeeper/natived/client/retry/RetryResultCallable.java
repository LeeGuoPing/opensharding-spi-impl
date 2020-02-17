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

package io.opensharding.orchestration.reg.zookeeper.natived.client.retry;

import io.opensharding.orchestration.reg.zookeeper.natived.client.action.IZookeeperProvider;
import lombok.Setter;
import org.apache.zookeeper.KeeperException;

/**
 * Sync retry call with result.
 *
 * @author lidongbo
 */
public abstract class RetryResultCallable<T> extends RetryCallable {
    
    @Setter
    private T result;
    
    public RetryResultCallable(final IZookeeperProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        super(provider, delayRetryPolicy);
    }
    
    /**
     * Get result.
     *
     * @return result
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    public T getResult() throws KeeperException, InterruptedException {
        if (null == result) {
            exec();
        }
        return result;
    }
}
