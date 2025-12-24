package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers.utils.GeometricUtils;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.model.geometric.Circle;
import java.io.DataOutputStream;
import java.io.IOException;

public class CircleValueHandler extends BaseValueHandler<Circle> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final Circle value) throws IOException {
    buffer.writeInt(24);
    // First encode the Center Point:
    GeometricUtils.writePoint(buffer, value.getCenter());
    // ... and then the Radius:
    buffer.writeDouble(value.getRadius());
  }

  @Override
  public int getLength(Circle value) {
    return 24;
  }
}