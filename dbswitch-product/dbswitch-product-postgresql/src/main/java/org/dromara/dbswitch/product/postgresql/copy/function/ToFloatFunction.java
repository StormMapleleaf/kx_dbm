package org.dromara.dbswitch.product.postgresql.copy.function;

@FunctionalInterface
public interface ToFloatFunction<T> {


  float applyAsFloat(T value);
}
