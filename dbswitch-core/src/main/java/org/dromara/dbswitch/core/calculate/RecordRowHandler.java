package org.dromara.dbswitch.core.calculate;

import java.util.List;


public interface RecordRowHandler {


  void handle(List<String> fields, Object[] record, int[] jdbcTypes, RowChangeTypeEnum flag);


  void destroy(List<String> fields);
}
