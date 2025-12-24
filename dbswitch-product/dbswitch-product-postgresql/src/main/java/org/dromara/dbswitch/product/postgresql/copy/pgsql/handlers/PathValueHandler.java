package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers.utils.GeometricUtils;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.model.geometric.Path;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.model.geometric.Point;
import java.io.DataOutputStream;
import java.io.IOException;

public class PathValueHandler extends BaseValueHandler<Path> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final Path value) throws IOException {
        byte pathIsClosed = (byte) (value.isClosed() ? 1 : 0);

        int totalBytesToWrite = 1 + 4 + 16 * value.size();

        buffer.writeInt(totalBytesToWrite);
        buffer.writeByte(pathIsClosed);
        buffer.writeInt(value.getPoints().size());
        for (Point p : value.getPoints()) {
      GeometricUtils.writePoint(buffer, p);
    }

  }

  @Override
  public int getLength(Path value) {
    throw new UnsupportedOperationException();
  }
}