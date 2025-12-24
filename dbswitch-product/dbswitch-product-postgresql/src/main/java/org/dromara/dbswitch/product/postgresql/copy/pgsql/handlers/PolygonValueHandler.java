package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers.utils.GeometricUtils;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.model.geometric.Point;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.model.geometric.Polygon;
import java.io.DataOutputStream;
import java.io.IOException;

public class PolygonValueHandler extends BaseValueHandler<Polygon> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final Polygon value) throws IOException {
        int totalBytesToWrite = 4 + 16 * value.size();

        buffer.writeInt(totalBytesToWrite);

        buffer.writeInt(value.getPoints().size());

        for (Point p : value.getPoints()) {
      GeometricUtils.writePoint(buffer, p);
    }

  }

  @Override
  public int getLength(Polygon value) {
    throw new UnsupportedOperationException();
  }
}