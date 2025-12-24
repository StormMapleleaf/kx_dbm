package org.dromara.dbswitch.core.provider.sync;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import java.util.List;
import javax.sql.DataSource;

public interface TableDataSynchronizeProvider {

  ProductTypeEnum getProductType();

  DataSource getDataSource();


  void prepare(String schemaName, String tableName, List<String> fieldNames, List<String> pks);

  long executeInsert(List<Object[]> records);

  long executeUpdate(List<Object[]> records);

  long executeDelete(List<Object[]> records);
}
