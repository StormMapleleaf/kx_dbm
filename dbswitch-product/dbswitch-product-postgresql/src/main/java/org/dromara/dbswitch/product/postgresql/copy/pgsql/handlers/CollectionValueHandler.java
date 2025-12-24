package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

public class CollectionValueHandler<TElementType, TCollectionType extends Collection<TElementType>> extends
    BaseValueHandler<TCollectionType> {

  private final int oid;
  private final IValueHandler<TElementType> valueHandler;

  public CollectionValueHandler(int oid, IValueHandler<TElementType> valueHandler) {
    this.oid = oid;
    this.valueHandler = valueHandler;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, TCollectionType value) throws IOException {

    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
    DataOutputStream arrayOutput = new DataOutputStream(byteArrayOutput);

    arrayOutput.writeInt(1);     arrayOutput.writeInt(1);     arrayOutput.writeInt(oid);     arrayOutput.writeInt(value.size());     arrayOutput.writeInt(1); 
        for (TElementType element : value) {
      valueHandler.handle(arrayOutput, element);
    }

    buffer.writeInt(byteArrayOutput.size());
    buffer.write(byteArrayOutput.toByteArray());
  }

  @Override
  public int getLength(TCollectionType value) {
    throw new UnsupportedOperationException();
  }
}
