package org.dromara.dbswitch.product.openguass;

import org.dromara.dbswitch.product.postgresql.PostgresMetadataQueryProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OpenGaussMetadataQueryProvider extends PostgresMetadataQueryProvider {

  private static Set<String> systemSchemas = new HashSet<>();

  static {
    systemSchemas.add("blockchain");
    systemSchemas.add("cstore");
    systemSchemas.add("db4ai");
    systemSchemas.add("dbe_perf");
    systemSchemas.add("dbe_pldebugger");
    systemSchemas.add("dbe_pldeveloper");
    systemSchemas.add("dbe_sql_util");
    systemSchemas.add("information_schema");
    systemSchemas.add("pg_catalog");
    systemSchemas.add("pkg_service");
    systemSchemas.add("snapshot");
    systemSchemas.add("sqladvisor");
  }

  public OpenGaussMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    List<String> schemas = super.querySchemaList(connection);
    return schemas.stream()
        .filter(s -> !systemSchemas.contains(s))
        .collect(Collectors.toList());
  }

}
