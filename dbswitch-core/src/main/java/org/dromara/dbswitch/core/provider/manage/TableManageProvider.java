package org.dromara.dbswitch.core.provider.manage;

public interface TableManageProvider {

  void truncateTableData(String schemaName, String tableName);

  void dropTable(String schemaName, String tableName);
}
