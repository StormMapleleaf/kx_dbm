package org.dromara.dbswitch.product.postgresql.copy.bulkprocessor.handler;

import java.util.List;

public interface IBulkWriteHandler<TEntity> {

  void write(List<TEntity> entities) throws Exception;

}
