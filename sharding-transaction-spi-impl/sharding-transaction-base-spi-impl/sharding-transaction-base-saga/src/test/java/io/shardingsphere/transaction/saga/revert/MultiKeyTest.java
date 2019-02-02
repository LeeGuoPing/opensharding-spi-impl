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

package io.shardingsphere.transaction.saga.revert;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.SQLParsingEngine;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;

import io.shardingsphere.transaction.saga.revert.api.RevertContext;
import io.shardingsphere.transaction.saga.revert.api.RevertParameter;
import io.shardingsphere.transaction.saga.revert.api.SnapshotParameter;
import io.shardingsphere.transaction.saga.revert.impl.delete.RevertDelete;
import io.shardingsphere.transaction.saga.revert.impl.insert.RevertInsert;
import io.shardingsphere.transaction.saga.revert.impl.update.RevertUpdate;

public class MultiKeyTest {
    
    private static DataSource dataSource;
    
    private static YamlShardingConfiguration config;
    
    private static ShardingRule shardingRule;
    
    private static ShardingTableMetaData shardingTableMetaData;
    
    private static final String DROP_TABLE = "DROP TABLE t_order_history;";
    
    private static final String CREATE_TABLE = "CREATE TABLE t_order_history (user_id int(11) NOT NULL,order_id int(11) NOT NULL, "
            +
            "status varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL, operate_date datetime(0) NULL DEFAULT NULL,PRIMARY KEY (user_id, order_id) USING BTREE);";
    
    @BeforeClass
    public static void initEnvironment() throws Exception {
        createShardingDatasource();
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        try {
            statement.execute(DROP_TABLE);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
        }
        statement.execute(CREATE_TABLE);
        statement.close();
        connection.close();
    }
    
    private static void createShardingDatasource() throws Exception {
        InputStream inputStream = MultiKeyTest.class.getClassLoader().getResourceAsStream("config-sharding.yaml");
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        config = YamlEngine.unmarshal(bytes, YamlShardingConfiguration.class);
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfigurationYamlSwapper().swap(config.getShardingRule());
        dataSource = ShardingDataSourceFactory.createDataSource(
                config.getDataSources(), shardingRuleConfiguration, config.getConfigMap(), config.getProps());
        shardingRule = new ShardingRule(shardingRuleConfiguration, config.getDataSources().keySet());
        shardingTableMetaData = ((ShardingDataSource) dataSource).getShardingContext().getMetaData().getTable();
    }
    
    @Test
    public void assertInsert() throws Exception {
        List<Object> params = new LinkedList<>();
        params.add(1);
        params.add(1);
        params.add("1");
        params.add(new Date());
        Connection connection = dataSource.getConnection();
        String insertSQL = "insert into t_order_history values(?,?,?,?)";
        update(connection, insertSQL, params);
        assertSelect(connection, true, new Object[]{1, 1});
        Connection actualConnection = config.getDataSources().get("ds_1").getConnection();
        String actualSQL = "insert into t_order_history values(?,?,?,?)";
        SQLStatement statement = new SQLParsingEngine(DatabaseType.MySQL, insertSQL, shardingRule, shardingTableMetaData).parse(true);
        revertInsert((DMLStatement) statement, actualConnection, insertSQL, actualSQL, "t_order_history_1", params);
        assertSelect(connection, false, new Object[]{1, 1});
        update(connection, "delete from t_order_history", new ArrayList<>());
        connection.close();
    }
    
    @Test
    public void assertUpdate() throws Exception {
        List<Object> params = new LinkedList<>();
        params.add("2");
        params.add("1");
        Connection connection = dataSource.getConnection();
        insertDataForTest(connection);
        String updateSQL = "update t_order_history set status = ? where status = ?";
        SQLStatement statement = new SQLParsingEngine(DatabaseType.MySQL, updateSQL, shardingRule, shardingTableMetaData).parse(true);
        List<RevertContext> updateRevertContexts = createRevertUpdateContext((DMLStatement) statement, updateSQL, params);
        updateStatus(connection, updateSQL);
        checkUpdate(connection, "2", 2);
        revertUpdate(updateRevertContexts, (DMLStatement) statement);
        checkUpdate(connection, "1", 2);
        update(connection, "delete from t_order_history", new ArrayList<>());
        connection.close();
    }
    
    @Test
    public void assertDelete() throws Exception {
        String updateSQL = "delete from t_order_history";
        List<Object> params = new LinkedList<>();
        Connection connection = dataSource.getConnection();
        insertDataForTest(connection);
        SQLStatement statement = new SQLParsingEngine(DatabaseType.MySQL, updateSQL, shardingRule, shardingTableMetaData).parse(true);
        List<RevertContext> updateRevertContexts = createRevertDeleteContext((DMLStatement) statement, updateSQL, params);
        update(connection, "delete from t_order_history", new ArrayList<>());
        checkUpdate(connection, "1", 0);
        revertDelete(updateRevertContexts, (DMLStatement) statement);
        checkUpdate(connection, "1", 2);
        update(connection, "delete from t_order_history", new ArrayList<>());
        connection.close();
    }
    
    @Test
    public void assertMultiInsert() throws Exception {
        String insertSQL = "insert into t_order_history values(?,?,?,?),(?,?,?,?),(?,?,?,?),(?,?,?,?)";
        List<Object> params = buildMultiInsertParam();
        Connection connection = dataSource.getConnection();
        update(connection, insertSQL, params);
        checkUpdate(connection, "1", 4);
        SQLStatement statement = new SQLParsingEngine(DatabaseType.MySQL, insertSQL, shardingRule, shardingTableMetaData).parse(true);
        String actualSQLTemplate = "insert into t_order_history_%S values(?,?,?,?)";
        for (int i = 0; i < 2; i++) {
            String actualSQL = String.format(actualSQLTemplate, i);
            Connection actualConnection = config.getDataSources().get("ds_" + i).getConnection();
            List<Object> reverParams = new LinkedList<>();
            for (int j = 0; j < params.size(); j++) {
                if (i == ((j / 4) + 1) % 2) {
                    reverParams.add(params.get(j));
                }
            }
            revertInsert((DMLStatement) statement, actualConnection, insertSQL, actualSQL, "t_order_history_" + i, reverParams);
            actualConnection.close();
        }
        checkUpdate(connection, "1", 0);
        connection.close();
    }
    
    private List<Object> buildMultiInsertParam() {
        List<Object> params = new LinkedList<>();
        for (int i = 1; i <= 4; i++) {
            params.add(i);
            params.add(i);
            params.add("1");
            params.add(new Date());
        }
        return params;
    }
    
    private void insertDataForTest(final Connection connection) throws Exception {
        String insertSQL = "insert into t_order_history values(?,?,?,?)";
        for (int i = 1; i <= 2; i++) {
            List<Object> params = new LinkedList<>();
            params.add(i);
            params.add(i);
            params.add("1");
            params.add(new Date());
            update(connection, insertSQL, params);
            assertSelect(connection, true, new Object[]{i, i});
        }
    }
    
    private List<RevertContext> createRevertDeleteContext(final DMLStatement dmlStatement, final String updateSQL, final List<Object> params) throws Exception {
        String actualSQLTemplate = "delete from t_order_history_%s";
        List<RevertContext> result = new LinkedList<>();
        String logicTable = "t_order_history";
        for (int i = 0; i < 2; i++) {
            String actualSQL = String.format(actualSQLTemplate, i);
            Connection actualConnection = config.getDataSources().get("ds_" + i).getConnection();
            Optional<RevertContext> revertContext = createRevertDeleteSnapshot(dmlStatement, actualConnection, updateSQL, actualSQL, logicTable, logicTable + "_" + i, params);
            result.add(revertContext.get());
            actualConnection.close();
        }
        return result;
    }
    
    private void revertDelete(final List<RevertContext> revertContexts, final DMLStatement dmlStatement) throws Exception {
        RevertDelete revertDelete = new RevertDelete();
        for (int i = 0; i < 2; i++) {
            Connection actualConnection = config.getDataSources().get("ds_" + i).getConnection();
            RevertParameter revertParameter = new RevertParameter(actualConnection, revertContexts.get(i).getRevertSQL(), revertContexts.get(i).getRevertParams().get(0));
            revertDelete.revert(revertParameter);
            actualConnection.close();
        }
    }
    
    private void updateStatus(final Connection connection, final String updateSQL) throws Exception {
        PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);
        preparedStatement.setObject(1, "2");
        preparedStatement.setObject(2, "1");
        preparedStatement.execute();
        preparedStatement.close();
    }
    
    private void checkUpdate(final Connection connection, final String staus, final int expectCount) throws Exception {
        PreparedStatement queryStatement = connection.prepareStatement("select count(1) from t_order_history where status = ?");
        queryStatement.setObject(1, staus);
        ResultSet resultSet = queryStatement.executeQuery();
        resultSet.next();
        assertEquals("Assert updated row count: ", expectCount, resultSet.getInt(1));
    }
    
    private void revertUpdate(final List<RevertContext> revertContexts, final DMLStatement dmlStatement) throws Exception {
        RevertUpdate revertUpdate = new RevertUpdate();
        for (int i = 0; i < 2; i++) {
            Connection actualConnection = config.getDataSources().get("ds_" + i).getConnection();
            RevertParameter revertParameter = new RevertParameter(actualConnection, revertContexts.get(i).getRevertSQL(), revertContexts.get(i).getRevertParams().get(0));
            revertUpdate.revert(revertParameter);
            actualConnection.close();
        }
    }
    
    private List<RevertContext> createRevertUpdateContext(final DMLStatement dmlStatement, final String updateSQL, final List<Object> params) throws Exception {
        String actualSQLTemplate = "update t_order_history_%s set status = ? where status = ?";
        List<RevertContext> result = new LinkedList<>();
        String logicTable = "t_order_history";
        for (int i = 0; i < 2; i++) {
            String actualSQL = String.format(actualSQLTemplate, i);
            Connection actualConnection = config.getDataSources().get("ds_" + i).getConnection();
            Optional<RevertContext> revertContext = createRevertUpdateSnapshot(dmlStatement, actualConnection, updateSQL, actualSQL, logicTable, logicTable + "_" + i, params);
            result.add(revertContext.get());
            actualConnection.close();
        }
        return result;
    }
    
    private Optional<RevertContext> createRevertUpdateSnapshot(final DMLStatement dmlStatement, final Connection actualConnection, final String logicSQL, final String actualSQL,
                                                               final String logicTable, final String actualTable, final List<Object> params) throws Exception {
        SnapshotParameter snapshotParameter = new SnapshotParameter(shardingTableMetaData.get(logicTable), dmlStatement, actualConnection, actualTable,
                logicSQL, actualSQL, params);
        RevertUpdate revertUpdate = new RevertUpdate();
        return revertUpdate.snapshot(snapshotParameter);
    }
    
    private Optional<RevertContext> createRevertDeleteSnapshot(final DMLStatement dmlStatement, final Connection actualConnection, final String logicSQL, final String actualSQL,
                                                               final String logicTable, final String actualTable, final List<Object> params) throws Exception {
        SnapshotParameter snapshotParameter = new SnapshotParameter(shardingTableMetaData.get(logicTable), dmlStatement, actualConnection, actualTable, logicSQL, actualSQL, params);
        RevertDelete revertDelete = new RevertDelete();
        return revertDelete.snapshot(snapshotParameter);
    }
    
    private void update(final Connection connection, final String insertSQL, final List<Object> params) throws Exception {
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
        int index = 1;
        for (Object each : params) {
            preparedStatement.setObject(index++, each);
        }
        preparedStatement.executeUpdate();
    }
    
    private void assertSelect(final Connection connection, final boolean expected, final Object[] params) throws SQLException {
        PreparedStatement queryStatement = connection.prepareStatement("select * from t_order_history where order_id = ? and user_id = ?");
        queryStatement.setObject(1, params[0]);
        queryStatement.setObject(2, params[1]);
        ResultSet resultSet = queryStatement.executeQuery();
        assertEquals("Assert result exist: ", expected, resultSet.next());
    }
    
    private void revertInsert(final DMLStatement dmlStatement, final Connection actualConnection, final String insertSQL, final String actualSQL, final String actualTable,
                              final List<Object> params) throws Exception {
        SnapshotParameter snapshotParameter = new SnapshotParameter(shardingTableMetaData.get("t_order_history"), dmlStatement, actualConnection, actualTable, insertSQL, actualSQL, params);
        RevertInsert revertInsert = new RevertInsert();
        Optional<RevertContext> revertContext = revertInsert.snapshot(snapshotParameter);
        for (Collection<Object> each : revertContext.get().getRevertParams()) {
            RevertParameter revertParameter = new RevertParameter(actualConnection, revertContext.get().getRevertSQL(), each);
            revertInsert.revert(revertParameter);
        }
    }
    
}
