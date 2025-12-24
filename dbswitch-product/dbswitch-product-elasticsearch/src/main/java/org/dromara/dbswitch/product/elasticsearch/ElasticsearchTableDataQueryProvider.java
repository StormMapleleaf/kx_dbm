// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.elasticsearch;

import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.entity.IncrementPoint;
import org.dromara.dbswitch.common.entity.ResultSetWrapper;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.schema.SchemaTableData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class ElasticsearchTableDataQueryProvider implements TableDataQueryProvider {

  private ProductFactoryProvider factoryProvider;
  private DataSource dataSource;

  public ElasticsearchTableDataQueryProvider(ProductFactoryProvider factoryProvider) {
    this.factoryProvider = factoryProvider;
    this.dataSource = factoryProvider.getDataSource();
  }

  @Override
  public ProductTypeEnum getProductType() {
    return this.factoryProvider.getProductType();
  }

  @Override
  public int getQueryFetchSize() {
    return 0;
  }

  @Override
  public void setQueryFetchSize(int size) {
  }

  @Override
  public ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      IncrementPoint point, List<String> orders) {
    String sql = tableName;
    try {
      Connection connection = this.dataSource.getConnection();
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(Constants.DEFAULT_QUERY_TIMEOUT_SECONDS);
      return ResultSetWrapper.builder()
          .connection(connection)
          .statement(statement)
          .resultSet(statement.executeQuery(sql))
          .build();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public SchemaTableData queryTableData(Connection connection, String schemaName, String tableName, int rowCount) {
    String querySQL = tableName;
    SchemaTableData data = new SchemaTableData();
    data.setSchemaName(schemaName);
    data.setTableName(tableName);
    data.setColumns(new ArrayList<>());
    data.setRows(new ArrayList<>());
    try (Statement st = connection.createStatement()) {
      try (ResultSet rs = st.executeQuery(querySQL)) {
        ResultSetMetaData m = rs.getMetaData();
        int count = m.getColumnCount();
        for (int i = 1; i <= count; i++) {
          data.getColumns().add(m.getColumnLabel(i));
        }

        while (rs.next()) {
          List<Object> row = new ArrayList<>(count);
          for (int i = 1; i <= count; i++) {
            Object value = rs.getObject(i);
            row.add(value);
          }
          if (data.getRows().size() > rowCount) {
            break;
          }
          data.getRows().add(row);
        }

        return data;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
