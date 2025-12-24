package org.dromara.dbswitch.common.converter;

public interface Converter<U, V> {

  V convert(U u);
}
