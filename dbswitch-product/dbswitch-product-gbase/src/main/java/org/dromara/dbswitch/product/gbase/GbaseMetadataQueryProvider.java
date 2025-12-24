package org.dromara.dbswitch.product.gbase;

import org.dromara.dbswitch.product.mysql.MysqlMetadataQueryProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.schema.SourceProperties;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class GbaseMetadataQueryProvider extends MysqlMetadataQueryProvider {

  public GbaseMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void postAppendCreateTableSql(StringBuilder builder, String tblComment, List<String> primaryKeys,
      SourceProperties tblProperties) {
    builder.append("ENGINE=EXPRESS DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin");
    if (StringUtils.isNotBlank(tblComment)) {
      builder.append(String.format(" COMMENT='%s' ", tblComment.replace("'", "\\'")));
    }
  }
}
