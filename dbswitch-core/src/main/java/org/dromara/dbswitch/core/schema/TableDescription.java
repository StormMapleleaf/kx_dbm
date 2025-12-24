package org.dromara.dbswitch.core.schema;

import org.dromara.dbswitch.common.type.ProductTableEnum;

public class TableDescription {

  private String tableName;
  private String schemaName;
  private String remarks;
  private ProductTableEnum tableType;

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getRemarks() {
    return this.remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getTableType() {
    return tableType.name();
  }

  public void setTableType(String tableType) {
    if ("PARTITIONED TABLE".equals(tableType)) {
      tableType = "TABLE";
    }
    this.tableType = ProductTableEnum.valueOf(tableType.toUpperCase());
  }

  public boolean isViewTable() {
    return ProductTableEnum.VIEW == tableType;
  }
}
