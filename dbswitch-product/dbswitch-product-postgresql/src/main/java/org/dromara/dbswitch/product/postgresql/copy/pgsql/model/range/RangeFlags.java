package org.dromara.dbswitch.product.postgresql.copy.pgsql.model.range;

public class RangeFlags {

  public static final int None = 0;

  public static final int Empty = 1;

  public static final int LowerBoundInclusive = 2;

  public static final int UpperBoundInclusive = 4;

  public static final int LowerBoundInfinite = 8;

  public static final int UpperBoundInfinite = 16;

  public static final int Inclusive = LowerBoundInclusive | UpperBoundInclusive;

  public static final int Infinite = LowerBoundInfinite | UpperBoundInfinite;

  public static final int LowerInclusiveInfinite = LowerBoundInclusive | LowerBoundInfinite;

  public static final int UpperInclusiveInfinite = UpperBoundInclusive | UpperBoundInfinite;
}
