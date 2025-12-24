package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.constants.DataType;

public interface IValueHandlerProvider {

  <TTargetType> IValueHandler<TTargetType> resolve(DataType targetType);
}
