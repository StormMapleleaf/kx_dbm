package org.dromara.dbswitch.core.schema;

public class ColumnValue {

  private int jdbcType;
  private Object value;

  public ColumnValue(int jdbcType, Object value) {
    this.jdbcType = jdbcType;
    this.value = value;
  }

  public int getJdbcType() {
    return jdbcType;
  }

  public Object getValue() {
    return value;
  }

}
