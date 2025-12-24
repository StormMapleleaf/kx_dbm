package org.dromara.dbswitch.product.tdengine;

import cn.hutool.core.util.HexUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.dromara.dbswitch.common.util.ObjectCastUtils;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.query.DefaultTableDataQueryProvider;
import org.dromara.dbswitch.core.schema.SchemaTableData;

public class TdengineTableDataQueryProvider extends DefaultTableDataQueryProvider {

  public TdengineTableDataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public SchemaTableData queryTableData(Connection connection, String schemaName, String tableName, int rowCount) {
    String fullTableName = quoteSchemaTableName(schemaName, tableName);
    String querySQL = String.format("SELECT * FROM %s limit %s", fullTableName, rowCount);
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
}
