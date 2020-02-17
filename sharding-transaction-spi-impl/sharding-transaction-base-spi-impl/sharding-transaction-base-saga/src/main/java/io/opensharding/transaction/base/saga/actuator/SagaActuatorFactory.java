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

package io.opensharding.transaction.base.saga.actuator;

import com.google.common.util.concurrent.MoreExecutors;
import io.opensharding.transaction.base.saga.actuator.transport.SagaTransportFactory;
import io.opensharding.transaction.base.saga.config.SagaConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.servicecomb.saga.core.PersistentStore;
import org.apache.servicecomb.saga.core.SagaDefinition;
import org.apache.servicecomb.saga.core.application.SagaExecutionComponent;
import org.apache.servicecomb.saga.core.application.interpreter.FromJsonFormat;
import org.apache.servicecomb.saga.core.dag.GraphBasedSagaFactory;
import org.apache.servicecomb.saga.format.ChildrenExtractor;
import org.apache.servicecomb.saga.format.JacksonFromJsonFormat;
import org.apache.shardingsphere.core.execute.ShardingThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Saga actuator factory.
 *
 * @author yangyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SagaActuatorFactory {
    
    /**
     * Create new saga execution component.
     *
     * @param sagaConfiguration saga configuration
     * @param sagaPersistence saga persistence
     * @return saga execution component
     */
    public static SagaExecutionComponent newInstance(final SagaConfiguration sagaConfiguration, final PersistentStore sagaPersistence) {
        FromJsonFormat<SagaDefinition> fromJsonFormat = new JacksonFromJsonFormat(SagaTransportFactory.getInstance());
        GraphBasedSagaFactory sagaFactory = new GraphBasedSagaFactory(sagaConfiguration.getCompensationRetryDelayMilliseconds(),
            sagaPersistence, new ChildrenExtractor(), createExecutorService(sagaConfiguration.getExecutorSize()));
        return new SagaExecutionComponent(sagaPersistence, fromJsonFormat, null, sagaFactory);
    }
    
    private static ExecutorService createExecutorService(final int executorSize) {
        ThreadFactory threadFactory = ShardingThreadFactoryBuilder.build("Saga-%d");
        ExecutorService result = executorSize > 0 ? Executors.newFixedThreadPool(executorSize, threadFactory) : Executors.newCachedThreadPool(threadFactory);
        MoreExecutors.addDelayedShutdownHook(result, 60, TimeUnit.SECONDS);
        return result;
    }
}
