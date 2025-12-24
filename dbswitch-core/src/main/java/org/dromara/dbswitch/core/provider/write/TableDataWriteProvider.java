package org.dromara.dbswitch.core.provider.write;

import java.util.List;

public interface TableDataWriteProvider {

  void prepareWrite(String schemaName, String tableName, List<String> fieldNames);

  long write(List<String> fieldNames, List<Object[]> recordValues);
}
