package org.dromara.dbswitch.core.provider.transform;

import java.util.List;

public interface RecordTransformProvider {

  String getTransformerName();

  Object[] doTransform(String schema, String table, List<String> fieldNames, Object[] recordValue);
}
