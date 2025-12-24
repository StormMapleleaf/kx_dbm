// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.elasticsearch;

import cn.hutool.json.JSONUtil;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.write.DefaultTableDataWriteProvider;
import com.google.common.collect.Lists;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
public class ElasticsearchTableDataWriteProvider extends DefaultTableDataWriteProvider {

  private String indexName;

  public ElasticsearchTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void prepareWrite(String schemaName, String tableName, List<String> fieldNames) {
    this.indexName = tableName;
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    if (CollectionUtils.isEmpty(fieldNames) || CollectionUtils.isEmpty(recordValues)) {
      return 0L;
    }
    Map<String, Object> bulkDocuments = new HashMap<>();
    bulkDocuments.put("indexName", indexName);
    try (Connection connection = getDataSource().getConnection()) {
      Statement statement = connection.createStatement();
      for (List<Object[]> partRecordValues : Lists.partition(recordValues, 500)) {
        bulkDocuments.put("sources", asString(fieldNames, partRecordValues));
        String sql = JSONUtil.toJsonStr(bulkDocuments);
        statement.executeUpdate(sql);
      }
      return recordValues.size();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> asString(List<String> fieldNames, List<Object[]> recordValues) {
    int fieldCount = Math.min(fieldNames.size(), recordValues.get(0).length);
    List<String> rows = new ArrayList<>(recordValues.size());
    for (Object[] row : recordValues) {
      Map<String, Object> columns = new LinkedHashMap<>(fieldCount);
      for (int i = 0; i < fieldCount; ++i) {
        columns.put(fieldNames.get(i), row[i]);
      }
      rows.add(JSONUtil.toJsonStr(columns));
    }
    return rows;
  }
}
