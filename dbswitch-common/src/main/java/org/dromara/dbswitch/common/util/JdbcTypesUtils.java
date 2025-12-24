package org.dromara.dbswitch.common.util;

import cn.hutool.core.convert.Convert;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JdbcTypesUtils {

  private static final Map<Integer, String> TYPE_NAMES = new HashMap<>();

  static {
    try {
      for (Field field : Types.class.getFields()) {
        TYPE_NAMES.put((Integer) field.get(null), field.getName());
      }
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to resolve JDBC Types constants", ex);
    }
  }

  public static String resolveTypeName(int sqlType) {
    return TYPE_NAMES.get(sqlType);
  }


  public static boolean isNumeric(int sqlType) {
    return (Types.DECIMAL == sqlType || Types.DOUBLE == sqlType || Types.FLOAT == sqlType
        || Types.NUMERIC == sqlType || Types.REAL == sqlType);
  }

  public static boolean isInteger(int sqlType) {
    return (Types.BIT == sqlType || Types.BIGINT == sqlType || Types.INTEGER == sqlType
        || Types.SMALLINT == sqlType
        || Types.TINYINT == sqlType);
  }

  public static boolean isString(int sqlType) {
    return (Types.CHAR == sqlType || Types.NCHAR == sqlType || Types.VARCHAR == sqlType
        || Types.LONGVARCHAR == sqlType || Types.NVARCHAR == sqlType
        || Types.LONGNVARCHAR == sqlType
        || Types.CLOB == sqlType || Types.NCLOB == sqlType || Types.SQLXML == sqlType
        || Types.ROWID == sqlType);
  }

  public static boolean isDateTime(int sqlType) {
    return (Types.DATE == sqlType || Types.TIME == sqlType || Types.TIMESTAMP == sqlType
        || Types.TIME_WITH_TIMEZONE == sqlType || Types.TIMESTAMP_WITH_TIMEZONE == sqlType);
  }

  public static boolean isBoolean(int sqlType) {
    return (Types.BOOLEAN == sqlType);
  }


  public static boolean isBinary(int sqlType) {
    return (Types.BINARY == sqlType || Types.VARBINARY == sqlType || Types.BLOB == sqlType
        || Types.LONGVARBINARY == sqlType);
  }

  public static boolean isTextile(int sqlType) {
    return isNumeric(sqlType) || isString(sqlType) || isDateTime(sqlType) || isBoolean(sqlType);
  }

  public static boolean isIncrement(int sqlType) {
    return (Types.BIT == sqlType
        || Types.BIGINT == sqlType
        || Types.INTEGER == sqlType
        || Types.TIMESTAMP == sqlType
        || Types.TIME_WITH_TIMEZONE == sqlType
        || Types.TIMESTAMP_WITH_TIMEZONE == sqlType);
  }

  public static long getObjectSize(int jdbcType, Object value) {
    if (null == value) {
      return 0;
    }

    if (isBinary(jdbcType)) {
      byte[] bytes = ObjectCastUtils.castToByteArray(value);
      return null == bytes ? 0 : bytes.length;
    } else if (isBoolean(jdbcType)) {
      return 1;
    } else {
      String strValue = Convert.toStr(value);
      return null == strValue ? 0 : strValue.length();
    }
  }

  public static long getRecordSize(Object[] record, int[] jdbcTypes) {
    long bytes = 0;
    if (record.length == jdbcTypes.length) {
      for (int i = 0; i < record.length; ++i) {
        bytes += getObjectSize(jdbcTypes[i], record[i]);
      }
    }
    return bytes;
  }
}
