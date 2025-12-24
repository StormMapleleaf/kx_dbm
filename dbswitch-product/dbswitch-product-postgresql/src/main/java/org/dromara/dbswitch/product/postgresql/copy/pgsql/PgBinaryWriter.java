package org.dromara.dbswitch.product.postgresql.copy.pgsql;

import org.dromara.dbswitch.product.postgresql.copy.exceptions.BinaryWriteFailedException;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers.IValueHandler;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PgBinaryWriter implements AutoCloseable {

  private final transient DataOutputStream buffer;

  public PgBinaryWriter(final OutputStream out) {
    this(out, 65536);
  }

  public PgBinaryWriter(final OutputStream out, final int bufferSize) {
    buffer = new DataOutputStream(new BufferedOutputStream(out, bufferSize));
    writeHeader();
  }

  public void startRow(int numColumns) {
    try {
      buffer.writeShort(numColumns);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }

  public <TTargetType> void write(final IValueHandler<TTargetType> handler,
      final TTargetType value) {
    handler.handle(buffer, value);
  }


  public void writeBoolean(boolean value) {
    try {
      buffer.writeInt(1);
      if (value) {
        buffer.writeByte(1);
      } else {
        buffer.writeByte(0);
      }
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }



  public void writeByte(int value) {
    try {
      buffer.writeInt(1);
      buffer.writeByte(value);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }


  public void writeShort(int value) {
    try {
      buffer.writeInt(2);
      buffer.writeShort(value);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }


  public void writeInt(int value) {
    try {
      buffer.writeInt(4);
      buffer.writeInt(value);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }


  public void writeLong(long value) {
    try {
      buffer.writeInt(8);
      buffer.writeLong(value);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }


  public void writeFloat(float value) {
    try {
      buffer.writeInt(4);
      buffer.writeFloat(value);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }


  public void writeDouble(double value) {
    try {
      buffer.writeInt(8);
      buffer.writeDouble(value);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }


  public void writeNull() {
    try {
      buffer.writeInt(-1);
    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }

  @Override
  public void close() {
    try {
      buffer.writeShort(-1);

      buffer.flush();
      buffer.close();
    } catch (Exception e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }

  private void writeHeader() {
    try {

            buffer.writeBytes("PGCOPY\n\377\r\n\0");
            buffer.writeInt(0);
            buffer.writeInt(0);

    } catch (IOException e) {
      Throwable t = e.getCause();
      if (null != t) {
        throw new BinaryWriteFailedException(t);
      } else {
        throw new BinaryWriteFailedException(e);
      }
    }
  }
}
