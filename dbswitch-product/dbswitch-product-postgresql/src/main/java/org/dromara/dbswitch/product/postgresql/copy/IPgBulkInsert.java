package org.dromara.dbswitch.product.postgresql.copy;

import java.sql.SQLException;
import java.util.stream.Stream;
import org.postgresql.PGConnection;

public interface IPgBulkInsert<TEntity> {

  void saveAll(PGConnection connection, Stream<TEntity> entities) throws SQLException;
}
