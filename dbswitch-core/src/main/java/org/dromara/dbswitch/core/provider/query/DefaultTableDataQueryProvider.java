package org.dromara.dbswitch.core.provider.query;

import cn.hutool.core.util.HexUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.entity.IncrementPoint;
import org.dromara.dbswitch.common.entity.ResultSetWrapper;
import org.dromara.dbswitch.common.util.JdbcTypesUtils;
import org.dromara.dbswitch.common.util.ObjectCastUtils;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.AbstractCommonProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.schema.ColumnValue;
import org.dromara.dbswitch.core.schema.SchemaTableData;

@Slf4j
public class DefaultTableDataQueryProvider
    extends AbstractCommonProvider
    implements TableDataQueryProvider {

  private final int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
  private final int concurrency = ResultSet.CONCUR_READ_ONLY;

  private int fetchSize = Constants.DEFAULT_FETCH_SIZE;

  public DefaultTableDataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public int getQueryFetchSize() {
    return this.fetchSize;
  }

  @Override
  public void setQueryFetchSize(int size) {
    if (size < Constants.MINIMUM_FETCH_SIZE) {
      throw new IllegalArgumentException(
          "设置的批量处理行数的大小fetchSize=" + size + "不得小于" + Constants.MINIMUM_FETCH_SIZE);
    }
    this.fetchSize = size;
  }

  @Override
  public ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      IncrementPoint point, List<String> orders) {
    StringBuilder sb = new StringBuilder("SELECT ");
    sb.append(quoteName(StringUtils.join(fields, quoteName(","))));
    sb.append(" FROM ");
    sb.append(quoteSchemaTableName(schemaName, tableName));
    if (IncrementPoint.EMPTY != point && point.isWorkable()) {
      sb.append(" WHERE ").append(toGreaterThanCondition(point));
    }
    if (CollectionUtils.isNotEmpty(orders)) {
      sb.append(" ORDER BY ");
      sb.append(quoteName(StringUtils.join(orders, quoteName(","))));
    }
    ProductFeatures features = getProductFeatures();
    return this.selectTableData(sb.toString(), features.convertFetchSize(this.fetchSize));
  }

  protected String toGreaterThanCondition(IncrementPoint point) {
    StringBuilder sb = new StringBuilder();
    sb.append(quoteName(point.getColumnName()));
    sb.append(" > ");
    if (JdbcTypesUtils.isInteger(point.getJdbcType())) {
      sb.append(point.getMaxValue());
    } else {
      sb.append("'").append(point.getMaxValue()).append("'");
    }
    return sb.toString();
  }

  protected ResultSetWrapper selectTableData(String sql, int fetchSize) {
    if (log.isDebugEnabled()) {
      log.debug("Query table data sql :{}", sql);
    }

    try {
      Connection connection = getDataSource().getConnection();
      Statement statement = connection.createStatement(resultSetType, concurrency);
      statement.setQueryTimeout(Constants.DEFAULT_QUERY_TIMEOUT_SECONDS);
      statement.setFetchSize(fetchSize);
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
  public SchemaTableData queryTableData(Connection connection, String schemaName, String tableName,
      int rowCount) {
    String fullTableName = quoteSchemaTableName(schemaName, tableName);
    String querySQL = String.format("SELECT * FROM %s ", fullTableName);
    SchemaTableData data = new SchemaTableData();
    data.setSchemaName(schemaName);
    data.setTableName(tableName);
    data.setColumns(new ArrayList<>());
    data.setRows(new ArrayList<>());
    try (Statement st = connection.createStatement()) {
      beforeExecuteQuery(connection, schemaName, tableName);
      try (ResultSet rs = st.executeQuery(querySQL)) {
        ResultSetMetaData m = rs.getMetaData();
        int count = m.getColumnCount();
        for (int i = 1; i <= count; i++) {
          data.getColumns().add(m.getColumnLabel(i));
        }

        int counter = 0;
        while (rs.next() && counter++ < rowCount) {
          List<Object> row = new ArrayList<>(count);
          for (int i = 1; i <= count; i++) {
            Object value = rs.getObject(i);
            if (value instanceof byte[]) {
              row.add(HexUtil.encodeHexStr((byte[]) value));
            } else if (value instanceof java.sql.Clob) {
              row.add(ObjectCastUtils.castToString(value));
            } else if (value instanceof java.sql.Blob) {
              byte[] bytes = ObjectCastUtils.castToByteArray(value);
              row.add(HexUtil.encodeHexStr(bytes));
            } else {
              row.add(null == value ? null : value.toString());
            }
          }
          data.getRows().add(row);
        }

        return data;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ColumnValue queryFieldMaxValue(Connection connection, String schemaName, String tableName, String filedName) {
    String fullTableName = quoteSchemaTableName(schemaName, tableName);
    String querySQL = String.format("SELECT MAX(%s) FROM %s ", quoteName(filedName), fullTableName);
    try (Statement st = connection.createStatement()) {
      try (ResultSet rs = st.executeQuery(querySQL)) {
        ResultSetMetaData m = rs.getMetaData();
        int dataType = m.getColumnType(1);
        if (rs.next()) {
          return new ColumnValue(dataType, rs.getObject(1));
        }
        return null;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void beforeExecuteQuery(Connection connection, String schema, String table) {
  }

}
