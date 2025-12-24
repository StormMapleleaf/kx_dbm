package org.dromara.dbswitch.product.postgresql.copy.function;

@FunctionalInterface
public interface ToBooleanFunction<T> {


  boolean applyAsBoolean(T value);
}
