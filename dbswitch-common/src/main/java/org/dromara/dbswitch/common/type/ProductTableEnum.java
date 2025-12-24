package org.dromara.dbswitch.common.type;


public enum ProductTableEnum {

  TABLE(0),


  VIEW(1);

  private int index;

  ProductTableEnum(int idx) {
    this.index = idx;
  }

  public int getIndex() {
    return index;
  }
}
