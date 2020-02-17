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

package io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.base;

import io.opensharding.orchestration.reg.zookeeper.natived.client.action.IZookeeperClient;
import io.opensharding.orchestration.reg.zookeeper.natived.client.util.EmbedTestingServer;
import io.opensharding.orchestration.reg.zookeeper.natived.client.zookeeper.section.ClientContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class StartWaitTest {
    
    @Before
    public void start() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertStart() throws IOException, InterruptedException {
        IZookeeperClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        Assert.assertTrue(testClient.start(10000, TimeUnit.MILLISECONDS));
        testClient.close();
    }
    
    @Test
    public void assertNotStart() throws IOException, InterruptedException {
        TestClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        Assert.assertFalse(testClient.start(100, TimeUnit.MILLISECONDS));
        testClient.close();
    }
}
