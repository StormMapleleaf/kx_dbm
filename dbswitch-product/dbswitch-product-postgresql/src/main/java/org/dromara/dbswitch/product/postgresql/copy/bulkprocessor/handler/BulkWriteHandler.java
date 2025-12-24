package org.dromara.dbswitch.product.postgresql.copy.bulkprocessor.handler;

import org.dromara.dbswitch.product.postgresql.copy.IPgBulkInsert;
import org.dromara.dbswitch.product.postgresql.copy.util.PostgreSqlUtils;
import java.sql.Connection;
import java.util.List;
import java.util.function.Supplier;
import org.postgresql.PGConnection;

public class BulkWriteHandler<TEntity> implements IBulkWriteHandler<TEntity> {

  private final IPgBulkInsert<TEntity> client;

  private final Supplier<Connection> connectionFactory;

  public BulkWriteHandler(IPgBulkInsert<TEntity> client, Supplier<Connection> connectionFactory) {
    this.client = client;
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void write(List<TEntity> entities) throws Exception {
        try (Connection connection = connectionFactory.get()) {
            final PGConnection pgConnection = PostgreSqlUtils.getPGConnection(connection);
            client.saveAll(pgConnection, entities.stream());
    }
  }
}
