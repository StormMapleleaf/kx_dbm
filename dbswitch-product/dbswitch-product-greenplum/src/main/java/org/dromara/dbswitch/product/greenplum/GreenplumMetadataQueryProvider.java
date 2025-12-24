package org.dromara.dbswitch.product.greenplum;

import org.dromara.dbswitch.product.postgresql.PostgresMetadataQueryProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.schema.SourceProperties;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public class GreenplumMetadataQueryProvider extends PostgresMetadataQueryProvider {

  static {
    systemSchemas.add("pg_aoseg");
    systemSchemas.add("gp_toolkit");
  }

  public GreenplumMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void postAppendCreateTableSql(StringBuilder builder, String tblComment, List<String> primaryKeys,
      SourceProperties tblProperties) {
    List<String> distributed = determineDistributed(primaryKeys, tblProperties.getDistributedKeys());
    if (null == distributed) {
      return;
    }
    String dk = getPrimaryKeyAsString(distributed);
    builder.append("\n DISTRIBUTED BY (").append(dk).append(")");
  }

  private List<String> determineDistributed(List<String> primaryKeys, List<String> distributedKeys) {
    if (CollectionUtils.isEmpty(distributedKeys)) {
            return CollectionUtils.isEmpty(primaryKeys) ? null : primaryKeys;
    }
        return CollectionUtils.isEmpty(primaryKeys) || new HashSet<>(primaryKeys).containsAll(distributedKeys)
        ? distributedKeys : null;
  }

}
