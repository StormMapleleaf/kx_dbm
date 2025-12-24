package org.dromara.dbswitch.core.calculate;


public enum RowChangeTypeEnum {

  VALUE_IDENTICAL(0, "identical"),

  VALUE_CHANGED(1, "update"),

  VALUE_INSERT(2, "insert"),

  VALUE_DELETED(3, "delete");

  private Integer index;

  private String status;

  RowChangeTypeEnum(int idx, String flag) {
    this.index = idx;
    this.status = flag;
  }

  public int getIndex() {
    return index;
  }

  public String getStatus() {
    return this.status;
  }

}
