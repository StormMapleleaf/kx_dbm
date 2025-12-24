package org.dromara.dbswitch.core.calculate;

import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.entity.ResultSetWrapper;
import org.dromara.dbswitch.common.util.ExamineUtils;
import org.dromara.dbswitch.common.util.JdbcTypesUtils;
import org.dromara.dbswitch.common.util.ObjectCastUtils;
import org.dromara.dbswitch.core.provider.ProductProviderFactory;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.provider.transform.RecordTransformProvider;
import org.dromara.dbswitch.core.service.DefaultMetadataService;
import org.dromara.dbswitch.core.service.MetadataService;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;


@Slf4j
public final class DefaultChangeCalculatorService implements RecordRowChangeCalculator {


  private boolean recordIdentical;


  private boolean checkJdbcType;


  private int queryFetchSize;


  private Runnable checkInterrupt;

  public DefaultChangeCalculatorService() {
    this(false, true);
  }

  public DefaultChangeCalculatorService(boolean recordIdentical, boolean checkJdbcType) {
    this.recordIdentical = recordIdentical;
    this.checkJdbcType = checkJdbcType;
    this.queryFetchSize = Constants.DEFAULT_FETCH_SIZE;
  }

  @Override
  public boolean isRecordIdentical() {
    return this.recordIdentical;
  }

  @Override
  public void setRecordIdentical(boolean recordOrNot) {
    this.recordIdentical = recordOrNot;
  }

  @Override
  public boolean isCheckJdbcType() {
    return this.checkJdbcType;
  }

  @Override
  public void setCheckJdbcType(boolean checkOrNot) {
    this.checkJdbcType = checkOrNot;
  }

  @Override
  public int getFetchSize() {
    return this.queryFetchSize;
  }

  @Override
  public void setFetchSize(int size) {
    ExamineUtils.check(size >= Constants.MINIMUM_FETCH_SIZE,
        "设置的批量处理行数的大小fetchSize不得小于%d",
        Constants.MINIMUM_FETCH_SIZE);
    this.queryFetchSize = size;
  }

  @Override
  public void setInterruptCheck(Runnable checkInterrupt) {
    this.checkInterrupt = checkInterrupt;
  }


  @Override
  public void executeCalculate(TaskParamEntity task, RecordRowHandler handler) {
    ExamineUtils.checkNotNull(task, "task");
    ExamineUtils.checkNotNull(handler, "handler");

    if (log.isDebugEnabled()) {
      log.debug("###### Begin execute calculate table CDC data now");
    }

    Map<String, String> columnsMap = task.getColumnsMap();
    boolean useOwnFieldsColumns = !CollectionUtils.isEmpty(task.getFieldColumns());

    MetadataService oldMd = new DefaultMetadataService(task.getOldDataSource(), task.getOldProductType());
    MetadataService newMd = new DefaultMetadataService(task.getNewDataSource(), task.getNewProductType());
    List<String> fieldsPrimaryKeyOld = oldMd
        .queryTablePrimaryKeys(task.getOldSchemaName(), task.getOldTableName());
    List<String> fieldsAllColumnOld = oldMd
        .queryTableColumnName(task.getOldSchemaName(), task.getOldTableName());
    List<String> fieldsPrimaryKeyNew = newMd
        .queryTablePrimaryKeys(task.getNewSchemaName(), task.getNewTableName());
    List<String> fieldsAllColumnNew = newMd
        .queryTableColumnName(task.getNewSchemaName(), task.getNewTableName());
    List<String> fieldsMappedPrimaryKeyNew = fieldsPrimaryKeyNew.stream()
        .map(s -> columnsMap.getOrDefault(s, s))
        .collect(Collectors.toList());
    List<String> fieldsMappedAllColumnNew = fieldsAllColumnNew.stream()
        .map(s -> columnsMap.getOrDefault(s, s))
        .collect(Collectors.toList());

    ExamineUtils.check(
        !fieldsPrimaryKeyOld.isEmpty() && !fieldsPrimaryKeyNew.isEmpty(),
        "计算变化量的表中存在无主键的表"
    );
    ExamineUtils.check(
        isListEqual(fieldsPrimaryKeyOld, fieldsMappedPrimaryKeyNew),
        "两个表的主键映射关系不匹配"
    );

    if (useOwnFieldsColumns) {
      List<String> mappedFieldColumns = task.getFieldColumns().stream()
          .map(s -> columnsMap.getOrDefault(s, s))
          .collect(Collectors.toList());
      if (!fieldsAllColumnNew.containsAll(task.getFieldColumns())
          || !fieldsAllColumnOld.containsAll(mappedFieldColumns)) {
        throw new RuntimeException("指定的字段列不完全在两个表中存在");
      }
      boolean same = (mappedFieldColumns.containsAll(fieldsPrimaryKeyOld)
          && task.getFieldColumns().containsAll(fieldsPrimaryKeyNew));
      if (!same) {
        throw new RuntimeException("提供的比较字段中未包含主键");
      }
      same = (fieldsAllColumnOld.containsAll(mappedFieldColumns)
          && fieldsAllColumnNew.containsAll(task.getFieldColumns()));
      if (!same) {
        throw new RuntimeException("提供的比较字段中存在表中不存在(映射关系对不上)的字段");
      }
    } else {
      boolean same = (fieldsMappedAllColumnNew.containsAll(fieldsPrimaryKeyOld)
          && fieldsAllColumnOld.containsAll(fieldsMappedAllColumnNew));
      if (!same) {
        throw new RuntimeException("两个表的字段映射关系不匹配");
      }
    }

    List<String> fieldsOfCompareValue = new ArrayList<>();
    if (useOwnFieldsColumns) {
      fieldsOfCompareValue.addAll(task.getFieldColumns());
    } else {
      fieldsOfCompareValue.addAll(fieldsAllColumnNew);
    }
    fieldsOfCompareValue.removeAll(fieldsPrimaryKeyNew);

    List<String> queryFieldColumn;
    List<String> mappedQueryFieldColumn;
    if (useOwnFieldsColumns) {
      queryFieldColumn = task.getFieldColumns();
    } else {
      queryFieldColumn = fieldsAllColumnOld;
    }
    mappedQueryFieldColumn = queryFieldColumn.stream()
        .map(s -> columnsMap.getOrDefault(s, s))
        .collect(Collectors.toList());

    ResultSetWrapper rsold = null;
    ResultSetWrapper rsnew = null;

    try {
      TableDataQueryProvider oldQuery = ProductProviderFactory
          .newProvider(task.getOldProductType(), task.getOldDataSource())
          .createTableDataQueryProvider();
      oldQuery.setQueryFetchSize(this.queryFetchSize);
      TableDataQueryProvider newQuery = ProductProviderFactory
          .newProvider(task.getNewProductType(), task.getNewDataSource())
          .createTableDataQueryProvider();
      newQuery.setQueryFetchSize(this.queryFetchSize);

      if (log.isDebugEnabled()) {
        log.debug("###### Query data from two table now");
      }
      Optional.ofNullable(checkInterrupt).ifPresent(Runnable::run);

      rsold = oldQuery
          .queryTableData(task.getOldSchemaName(), task.getOldTableName(),
              mappedQueryFieldColumn, fieldsMappedPrimaryKeyNew);
      Optional.ofNullable(checkInterrupt).ifPresent(Runnable::run);

      rsnew = newQuery
          .queryTableData(task.getNewSchemaName(), task.getNewTableName(),
              queryFieldColumn, fieldsPrimaryKeyNew);
      Optional.ofNullable(checkInterrupt).ifPresent(Runnable::run);

      ResultSetMetaData metaData = rsnew.getResultSet().getMetaData();

      if (log.isDebugEnabled()) {
        log.debug("###### Check data validate now");
      }

      int oldcnt = rsold.getResultSet().getMetaData().getColumnCount();
      int newcnt = rsnew.getResultSet().getMetaData().getColumnCount();
      if (oldcnt != newcnt) {
        throw new RuntimeException(String.format("两个表的字段总个数不相等，即：%d!=%d", oldcnt, newcnt));
      } else {
        for (int k = 1; k < metaData.getColumnCount(); ++k) {
          String key1 = rsnew.getResultSet().getMetaData().getColumnLabel(k);
          if (null == key1) {
            key1 = rsnew.getResultSet().getMetaData().getColumnName(k);
          }

          String key2 = rsold.getResultSet().getMetaData().getColumnLabel(k);
          if (null == key2) {
            key2 = rsold.getResultSet().getMetaData().getColumnName(k);
          }

          if (checkJdbcType) {
            int type1 = rsold.getResultSet().getMetaData().getColumnType(k);
            int type2 = rsnew.getResultSet().getMetaData().getColumnType(k);
            if (type1 != type2) {
              throw new RuntimeException(String.format("字段 [name=%s -> %s] 的数据类型不同，因 %s!=%s !",
                  key1, key2,
                  JdbcTypesUtils.resolveTypeName(type1), JdbcTypesUtils.resolveTypeName(type2)));
            }
          }

        }
      }

      int[] keyNumbers = new int[fieldsPrimaryKeyNew.size()];
      for (int i = 0; i < keyNumbers.length; ++i) {
        String fn = fieldsPrimaryKeyNew.get(i);
        keyNumbers[i] = getIndexOfField(fn, metaData);
      }

      int[] valNumbers = new int[fieldsOfCompareValue.size()];
      for (int i = 0; i < valNumbers.length; ++i) {
        String fn = fieldsOfCompareValue.get(i);
        valNumbers[i] = getIndexOfField(fn, metaData);
      }

      int[] jdbcTypes = new int[metaData.getColumnCount()];
      List<String> targetColumns = new ArrayList<>();
      for (int k = 1; k <= metaData.getColumnCount(); ++k) {
        String key = metaData.getColumnLabel(k);
        if (null == key) {
          key = metaData.getColumnName(k);
        }
        targetColumns.add(columnsMap.getOrDefault(key, key));
        jdbcTypes[k - 1] = metaData.getColumnType(k);
      }

      if (log.isDebugEnabled()) {
        log.debug("###### Enter CDC calculate now");
      }

      Optional.ofNullable(checkInterrupt).ifPresent(Runnable::run);

      RecordTransformProvider transformer = task.getTransformer();

      RowChangeTypeEnum flagField = null;
      Object[] outputRow;
      Object[] one = getRowData(rsold.getResultSet());
      Object[] two = transformer.doTransform(task.getNewSchemaName(), task.getNewTableName(),
          queryFieldColumn, getRowData(rsnew.getResultSet()));
      while (true) {
        Optional.ofNullable(checkInterrupt).ifPresent(Runnable::run);
        if (one == null && two == null) {
          break;
        } else if (one == null && two != null) {
          flagField = RowChangeTypeEnum.VALUE_INSERT;
          outputRow = two;
          two = transformer.doTransform(task.getNewSchemaName(), task.getNewTableName(),
              queryFieldColumn, getRowData(rsnew.getResultSet()));
        } else if (one != null && two == null) {
          flagField = RowChangeTypeEnum.VALUE_DELETED;
          outputRow = one;
          one = getRowData(rsold.getResultSet());
        } else {
          int compare = this.compare(one, two, keyNumbers, metaData);
          if (0 == compare) {
            int compareValues = this.compare(one, two, valNumbers, metaData);
            if (compareValues == 0) {
              flagField = RowChangeTypeEnum.VALUE_IDENTICAL;
              outputRow = one;
            } else {
              flagField = RowChangeTypeEnum.VALUE_CHANGED;
              outputRow = two;
            }

            one = getRowData(rsold.getResultSet());
            two = transformer.doTransform(task.getNewSchemaName(), task.getNewTableName(),
                queryFieldColumn, getRowData(rsnew.getResultSet()));
          } else {
            if (compare < 0) {
              flagField = RowChangeTypeEnum.VALUE_DELETED;
              outputRow = one;
              one = getRowData(rsold.getResultSet());
            } else {
              flagField = RowChangeTypeEnum.VALUE_INSERT;
              outputRow = two;
              two = transformer.doTransform(task.getNewSchemaName(), task.getNewTableName(), queryFieldColumn,
                  getRowData(rsnew.getResultSet()));
            }
          }
        }

        if (!this.recordIdentical && RowChangeTypeEnum.VALUE_IDENTICAL == flagField) {
          continue;
        }

        handler.handle(Collections.unmodifiableList(targetColumns), outputRow, jdbcTypes, flagField);
      }

      if (log.isDebugEnabled()) {
        log.debug("###### Calculate CDC Over now");
      }

      handler.destroy(Collections.unmodifiableList(targetColumns));

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (null != rsold) {
        rsold.close();
      }
      if (null != rsnew) {
        rsnew.close();
      }
    }

  }

  private boolean isListEqual(List<String> left, List<String> right) {
    return left.containsAll(right) && right.containsAll(left);
  }

 
  private int getIndexOfField(String key, ResultSetMetaData metaData) throws SQLException {
    for (int k = 1; k <= metaData.getColumnCount(); ++k) {
      String fieldName = metaData.getColumnLabel(k);
      if (null == fieldName) {
        fieldName = metaData.getColumnName(k);
      }

      if (fieldName.equals(key)) {
        return k - 1;
      }
    }

    return -1;
  }


  private int compare(Object[] obj1, Object[] obj2, int[] fieldnrs, ResultSetMetaData metaData)
      throws SQLException {
    if (obj1.length != obj2.length) {
      throw new RuntimeException("Invalid compare object list !");
    }

    for (int nr : fieldnrs) {
      int jdbcType = metaData.getColumnType(nr + 1);
      Object o1 = obj1[nr];
      Object o2 = obj2[nr];

      int cmp = typeCompare(jdbcType, o1, o2);
      if (cmp != 0) {
        return cmp;
      }
    }

    return 0;
  }

  private int typeCompare(int type, Object o1, Object o2) {
    boolean n1 = (o1 == null);
    boolean n2 = (o2 == null);
    if (n1 && !n2) {
      return -1;
    }
    if (!n1 && n2) {
      return 1;
    }
    if (n1 && n2) {
      return 0;
    }

    if (JdbcTypesUtils.isString(type)) {
      String s1 = ObjectCastUtils.castToString(o1);
      String s2 = ObjectCastUtils.castToString(o2);
      return s1.compareTo(s2);
    } else if (JdbcTypesUtils.isNumeric(type) && o1 instanceof java.lang.Number
        && o2 instanceof java.lang.Number) {
      java.lang.Number s1 = (java.lang.Number) o1;
      java.lang.Number s2 = (java.lang.Number) o2;
      return Double.compare(s1.doubleValue(), s2.doubleValue());
    } else if (JdbcTypesUtils.isInteger(type) && o1 instanceof java.lang.Number
        && o2 instanceof java.lang.Number) {
      java.lang.Number s1 = (java.lang.Number) o1;
      java.lang.Number s2 = (java.lang.Number) o2;
      return Long.compare(s1.longValue(), s2.longValue());
    } else if (JdbcTypesUtils.isDateTime(type)) {
      if (o1 instanceof LocalTime) {
        o1 = java.sql.Time.valueOf((LocalTime) o1);
      }
      if (o1 instanceof LocalDate) {
        o1 = java.sql.Date.valueOf((LocalDate) o1);
      }
      if (o1 instanceof LocalDateTime) {
        o1 = java.sql.Timestamp.valueOf((LocalDateTime) o1);
      }
      if (o2 instanceof LocalTime) {
        o2 = java.sql.Time.valueOf((LocalTime) o2);
      }
      if (o2 instanceof LocalDate) {
        o2 = java.sql.Date.valueOf((LocalDate) o2);
      }
      if (o2 instanceof LocalDateTime) {
        o2 = java.sql.Timestamp.valueOf((LocalDateTime) o2);
      }
      if (o1 instanceof java.sql.Time && o2 instanceof java.sql.Time) {
        java.sql.Time t1 = (java.sql.Time) o1;
        java.sql.Time t2 = (java.sql.Time) o2;
        return t1.compareTo(t2);
      } else if (o1 instanceof java.sql.Timestamp && o2 instanceof java.sql.Timestamp) {
        java.sql.Timestamp t1 = (java.sql.Timestamp) o1;
        java.sql.Timestamp t2 = (java.sql.Timestamp) o2;
        return t1.compareTo(t2);
      } else if (o1 instanceof java.sql.Date && o2 instanceof java.sql.Date) {
        java.sql.Date t1 = (java.sql.Date) o1;
        java.sql.Date t2 = (java.sql.Date) o2;
        return t1.compareTo(t2);
      } else {
        String s1 = String.valueOf(o1);
        String s2 = String.valueOf(o2);
        return s1.compareTo(s2);
      }
    } else {
      try {
        return compareTo(
            ObjectCastUtils.castToByteArray(o1),
            ObjectCastUtils.castToByteArray(o2)
        );
      } catch (Exception e) {
        log.warn("CDC compare field value failed, return 0 instead,{}", e.getMessage());
        return 0;
      }
    }
  }


  public int compareTo(byte[] s1, byte[] s2) {
    int len1 = s1.length;
    int len2 = s2.length;
    int lim = Math.min(len1, len2);
    byte[] v1 = s1;
    byte[] v2 = s2;

    int k = 0;
    while (k < lim) {
      byte c1 = v1[k];
      byte c2 = v2[k];
      if (c1 != c2) {
        return c1 - c2;
      }
      k++;
    }
    return len1 - len2;
  }


  private Object[] getRowData(ResultSet rs) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();
    Object[] rowData = null;

    if (rs.next()) {
      rowData = new Object[metaData.getColumnCount()];
      for (int j = 1; j <= metaData.getColumnCount(); ++j) {
        rowData[j - 1] = rs.getObject(j);
      }
    }

    return rowData;
  }

}
