package org.dromara.dbswitch.product.oracle;

import org.dromara.dbswitch.common.util.JdbcTypesUtils;
import org.dromara.dbswitch.common.util.ObjectCastUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.SqlTypeValue;

@Slf4j
@UtilityClass
public class OracleCastUtils {

  /**
   * 将java.sql.Array 类型转换为java.lang.String
   * <p>
   * Oracle 没有数组类型，这里以文本类型进行存储
   * <p>
   * Oracle的CLOB和BLOB类型写入请见：
   * <p>
   * oracle.jdbc.driver.OraclePreparedStatement.setObjectCritical
   */
  public static Object castByJdbcType(int jdbcType, Object value, List<InputStream> iss) {
    if (null == value) {
      return null;
    }

    try {
      switch (jdbcType) {
        case Types.NUMERIC:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof Byte) {
                ps.setByte(paramIndex, (Byte) value);
              } else if (value instanceof Short) {
                ps.setShort(paramIndex, (Short) value);
              } else if (value instanceof Integer) {
                ps.setInt(paramIndex, (Integer) value);
              } else if (value instanceof BigInteger) {
                ps.setInt(paramIndex, ((BigInteger) value).intValue());
              } else if (value instanceof Long) {
                ps.setLong(paramIndex, (Long) value);
              } else if (value instanceof Float) {
                ps.setFloat(paramIndex, (Float) value);
              } else if (value instanceof Double) {
                ps.setDouble(paramIndex, (Double) value);
              } else if (value instanceof BigDecimal) {
                ps.setBigDecimal(paramIndex, (BigDecimal) value);
              } else {
                ps.setObject(paramIndex, value, sqlType);
              }
            }
          };
        case Types.TIME:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof Time) {
                ps.setTime(paramIndex, (Time) value);
              } else {
                LocalTime time = ObjectCastUtils.castToLocalTime(value);
                if (null == time) {
                  ps.setNull(paramIndex, jdbcType);
                } else {
                  ps.setTime(paramIndex, Time.valueOf(time));
                }
              }
            }
          };
        case Types.DATE:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof Date) {
                ps.setDate(paramIndex, (Date) value);
              } else {
                LocalDate date = ObjectCastUtils.castToLocalDate(value);
                if (null == date) {
                  ps.setNull(paramIndex, jdbcType);
                } else {
                  ps.setDate(paramIndex, Date.valueOf(date));
                }
              }
            }
          };
        case Types.TIMESTAMP:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof Timestamp) {
                ps.setTimestamp(paramIndex, (Timestamp) value);
              } else {
                LocalDateTime dateTime = ObjectCastUtils.castToLocalDateTime(value);
                if (null == dateTime) {
                  ps.setNull(paramIndex, jdbcType);
                } else {
                  ps.setTimestamp(paramIndex, Timestamp.valueOf(dateTime));
                }
              }
            }
          };
        case Types.BLOB:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof java.sql.Blob) {
                ps.setBlob(paramIndex, (java.sql.Blob) value);
              } else {
                InputStream is = new ByteArrayInputStream(ObjectCastUtils.castToByteArray(value));
                ps.setBlob(paramIndex, is);
                iss.add(is);
              }
            }
          };
        case Types.CLOB:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof java.sql.Clob) {
                ps.setClob(paramIndex, (java.sql.Clob) value);
              } else {
                java.io.Reader reader = new StringReader(ObjectCastUtils.castToString(value));
                ps.setClob(paramIndex, reader);
              }
            }
          };
        case Types.NCLOB:
          return new SqlTypeValue() {
            @Override
            public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
                throws SQLException {
              if (value instanceof java.sql.NClob) {
                ps.setNClob(paramIndex, (java.sql.NClob) value);
              } else {
                java.io.Reader reader = new StringReader(ObjectCastUtils.castToString(value));
                ps.setNClob(paramIndex, reader);
              }
            }
          };
        default:
          return ObjectCastUtils.castByJdbcType(jdbcType, value);
      }
    } catch (
        Exception e) {
      log.warn("Convert from {} to Oracle {} failed: {}",
          value.getClass().getName(),
          JdbcTypesUtils.resolveTypeName(jdbcType),
          e.getMessage()
      );
      return null;
    }
  }

}
