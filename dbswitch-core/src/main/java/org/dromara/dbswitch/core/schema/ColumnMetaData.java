package org.dromara.dbswitch.core.schema;

import org.dromara.dbswitch.common.consts.Constants;


public class ColumnMetaData {

  public static final int TYPE_NONE = 0;

  public static final int TYPE_NUMBER = 1;

  public static final int TYPE_STRING = 2;

  public static final int TYPE_DATE = 3;

  public static final int TYPE_BOOLEAN = 4;

  public static final int TYPE_INTEGER = 5;

  public static final int TYPE_BIGNUMBER = 6;

  public static final int TYPE_SERIALIZABLE = 7;

  public static final int TYPE_BINARY = 8;

  public static final int TYPE_TIMESTAMP = 9;

  public static final int TYPE_TIME = 10;

  public static final int TYPE_INET = 11;

  public static final String[] TYPE_CODES = new String[]{"-", "Number", "String", "Date", "Boolean",
      "Integer", "BigNumber", "Serializable", "Binary", "Timestamp", "Time", "Internet Address",};


  protected String name;
  protected int length;
  protected int precision;
  protected int type;
  protected String remarks;
  protected String defaultValue;

  public ColumnMetaData(ColumnDescription desc) {
    this.create(desc);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public boolean isHaveDefault() {
    return defaultValue != null && !defaultValue.isEmpty();
  }

  public boolean isString() {
    return type == TYPE_STRING;
  }

  public boolean isDate() {
    return type == TYPE_DATE;
  }

  public boolean isTime() {
    return type == TYPE_TIME;
  }

  public boolean isDateTime() {
    return type == TYPE_TIMESTAMP;
  }

  public boolean isBigNumber() {
    return type == TYPE_BIGNUMBER;
  }


  public boolean isNumber() {
    return type == TYPE_NUMBER;
  }

  public boolean isBoolean() {
    return type == TYPE_BOOLEAN;
  }

  public boolean isSerializableType() {
    return type == TYPE_SERIALIZABLE;
  }

  public boolean isBinary() {
    return type == TYPE_BINARY;
  }

  public boolean isInteger() {
    return type == TYPE_INTEGER;
  }

  public boolean isNumeric() {
    return isInteger() || isNumber() || isBigNumber();
  }

  public static final boolean isNumeric(int t) {
    return t == TYPE_INTEGER || t == TYPE_NUMBER || t == TYPE_BIGNUMBER;
  }


  public String getTypeDesc() {
    return TYPE_CODES[type];
  }

  private void create(ColumnDescription desc) {
    int length = -1;
    int precision = -1;
    int valtype = ColumnMetaData.TYPE_NONE;
    int type = desc.getFieldType();
    boolean signed = desc.isSigned();

    switch (type) {
      case java.sql.Types.CHAR:
      case java.sql.Types.NCHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.NVARCHAR:
        valtype = ColumnMetaData.TYPE_STRING;
        length = desc.getDisplaySize();
        break;

      case java.sql.Types.LONGVARCHAR:
      case java.sql.Types.LONGNVARCHAR:
      case java.sql.Types.CLOB:
      case java.sql.Types.NCLOB:
      case java.sql.Types.SQLXML:
      case java.sql.Types.ROWID:
        valtype = ColumnMetaData.TYPE_STRING;
        length = Constants.CLOB_LENGTH;
        break;

      case java.sql.Types.BIGINT:
  
        if (signed) {
          valtype = ColumnMetaData.TYPE_INTEGER;
          precision = 0; 
          length = 15;
        } else {
          valtype = ColumnMetaData.TYPE_BIGNUMBER;
          precision = 0; 
          length = 16;
        }
        break;

      case java.sql.Types.INTEGER:
        valtype = ColumnMetaData.TYPE_INTEGER;
        precision = 0; 
        length = 9;
        break;

      case java.sql.Types.SMALLINT:
        valtype = ColumnMetaData.TYPE_INTEGER;
        precision = 0; 
        length = 4;
        break;

      case java.sql.Types.TINYINT:
        valtype = ColumnMetaData.TYPE_INTEGER;
        precision = 0; 
        length = 2;
        break;

      case java.sql.Types.DECIMAL:
      case java.sql.Types.DOUBLE:
      case java.sql.Types.FLOAT:
      case java.sql.Types.REAL:
      case java.sql.Types.NUMERIC:
        valtype = ColumnMetaData.TYPE_NUMBER;
        length = desc.getPrecisionSize();
        precision = desc.getScaleSize();
        if (length >= 126) {
          length = -1;
        }
        if (precision >= 126) {
          precision = -1;
        }

        if (type == java.sql.Types.DOUBLE || type == java.sql.Types.FLOAT
            || type == java.sql.Types.REAL) {
          if (precision == 0) {
            if (!signed) {
              precision = -1; 
            } else {
              length = 18;
              precision = 4;
            }
          }

          if ((desc.getProductType().isLikePostgres())
              && type == java.sql.Types.DOUBLE
              && precision >= 16
              && length >= 16) {
            precision = -1;
            length = -1;
          }

          if (desc.getProductType().isLikeMysql()) {
            if (precision >= length) {
              precision = -1;
              length = -1;
            }
          }

          if (desc.getProductType().isLikeHive()) {
            if (type == java.sql.Types.DOUBLE
                && precision >= 15
                && length >= 15) {
              precision = 6;
              length = 25;
            }

            if (type == java.sql.Types.FLOAT
                && precision >= 7
                && length >= 7) {
              precision = 6;
              length = 25;
            }
          }

        } else {
          if (precision == 0) {
            if (length <= 18 && length > 0) { 
              valtype = ColumnMetaData.TYPE_INTEGER; 
            } else if (length > 18) {
              valtype = ColumnMetaData.TYPE_BIGNUMBER;
            }
          } else { 
            if (length > 15 || precision > 15) {
              valtype = ColumnMetaData.TYPE_BIGNUMBER;
            }
          }
        }

        if (desc.getProductType().isLikePostgres()) {
          if (type == java.sql.Types.NUMERIC && length == 0 && precision == 0) {
            valtype = ColumnMetaData.TYPE_BIGNUMBER;
            length = -1;
            precision = -1;
          }
        }

        if (desc.getProductType().isLikeOracle()) {
          if (precision == 0 && length == 38) {
            valtype = ColumnMetaData.TYPE_INTEGER;
          }
          if (precision <= 0 && length <= 0) {
            valtype = ColumnMetaData.TYPE_BIGNUMBER;
            length = -1;
            precision = -1;
          }
        }

        break;

      case java.sql.Types.TIMESTAMP:
      case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
        valtype = ColumnMetaData.TYPE_TIMESTAMP;
        length = desc.getScaleSize();
        break;

      case java.sql.Types.DATE:
        valtype = ColumnMetaData.TYPE_DATE;
        break;

      case java.sql.Types.TIME:
      case java.sql.Types.TIME_WITH_TIMEZONE:
        valtype = ColumnMetaData.TYPE_TIME;
        break;

      case java.sql.Types.BOOLEAN:
      case java.sql.Types.BIT:
        valtype = ColumnMetaData.TYPE_BOOLEAN;
        break;

      case java.sql.Types.BINARY:
      case java.sql.Types.BLOB:
      case java.sql.Types.VARBINARY:
      case java.sql.Types.LONGVARBINARY:
        valtype = ColumnMetaData.TYPE_BINARY;
        precision = -1;
        break;

      default:
        valtype = ColumnMetaData.TYPE_STRING;
        length = desc.getDisplaySize();
        break;
    }

    this.name = desc.getFieldName();
    this.length = length;
    this.precision = precision;
    this.type = valtype;
    this.remarks = desc.getRemarks();
    this.defaultValue = desc.getDefaultValue();
  }

}
