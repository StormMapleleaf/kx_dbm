package org.dromara.dbswitch.product.postgresql.copy.function;

@FunctionalInterface
public interface ToBooleanFunction<T> {

  /**
   * Applies this function to the given argument.
   *
   * @param value the function argument
   * @return the function result
   */
  boolean applyAsBoolean(T value);
}
