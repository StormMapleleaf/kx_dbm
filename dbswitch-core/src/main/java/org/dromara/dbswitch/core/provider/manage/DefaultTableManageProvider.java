package org.dromara.dbswitch.core.provider.manage;

import org.dromara.dbswitch.core.provider.AbstractCommonProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultTableManageProvider
    extends AbstractCommonProvider
    implements TableManageProvider {

  public DefaultTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  protected final int executeSql(String sql) {
    if (log.isDebugEnabled()) {
      log.debug("Execute sql :{}", sql);
    }
    try (Connection connection = getDataSource().getConnection();
        Statement st = connection.createStatement()) {
      return st.executeUpdate(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    String fullTableName = quoteSchemaTableName(schemaName, tableName);
    String sql = String.format("TRUNCATE TABLE %s ", fullTableName);
    this.executeSql(sql);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String fullTableName = quoteSchemaTableName(schemaName, tableName);
    String sql = String.format("DROP TABLE %s ", fullTableName);
    this.executeSql(sql);
  }

}
