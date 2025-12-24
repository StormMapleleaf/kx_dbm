package org.dromara.dbswitch.core.provider.query;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import org.dromara.dbswitch.common.entity.IncrementPoint;
import org.dromara.dbswitch.common.entity.ResultSetWrapper;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.schema.ColumnValue;
import org.dromara.dbswitch.core.schema.SchemaTableData;


public interface TableDataQueryProvider {


  ProductTypeEnum getProductType();

  int getQueryFetchSize();

  void setQueryFetchSize(int size);

  default ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields) {
    return queryTableData(schemaName, tableName, fields, IncrementPoint.EMPTY, Collections.emptyList());
  }


  default ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      IncrementPoint point) {
    return queryTableData(schemaName, tableName, fields, point, Collections.emptyList());
  }


  default ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      List<String> orders) {
    return queryTableData(schemaName, tableName, fields, IncrementPoint.EMPTY, orders);
  }

  ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      IncrementPoint point, List<String> orders);

  SchemaTableData queryTableData(Connection connection, String schemaName, String tableName, int rowCount);

  default ColumnValue queryFieldMaxValue(Connection connection, String schemaName, String tableName, String filedName) {
    return null;
  }
}
