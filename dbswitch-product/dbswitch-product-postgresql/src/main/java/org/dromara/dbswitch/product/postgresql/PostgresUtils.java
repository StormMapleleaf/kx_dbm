package org.dromara.dbswitch.product.postgresql;

import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.util.GenerateSqlUtils;
import java.sql.Connection;
import java.util.List;

public final class PostgresUtils {

  public static String getTableDDL(MetadataProvider provider, Connection connection, String schema,
      String table) {
    List<ColumnDescription> columnDescriptions = provider.queryTableColumnMeta(connection, schema, table);
    List<String> pks = provider.queryTablePrimaryKeys(connection, schema, table);
    return GenerateSqlUtils.getDDLCreateTableSQL(
        provider, columnDescriptions, pks, schema, table, false);
  }


  private PostgresUtils() {
    throw new IllegalStateException();
  }

}
