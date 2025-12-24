package org.dromara.dbswitch.product.postgresql.copy.pgsql.converter;

public interface IValueConverter<TSource, TTarget> {

  TTarget convert(TSource source);

}
