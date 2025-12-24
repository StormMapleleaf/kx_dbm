package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers.utils.GeometricUtils;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.model.geometric.LineSegment;
import java.io.DataOutputStream;
import java.io.IOException;

public class LineSegmentValueHandler extends BaseValueHandler<LineSegment> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final LineSegment value)
      throws IOException {
    buffer.writeInt(32);

    GeometricUtils.writePoint(buffer, value.getP1());
    GeometricUtils.writePoint(buffer, value.getP2());
  }

  @Override
  public int getLength(LineSegment value) {
    return 32;
  }
}