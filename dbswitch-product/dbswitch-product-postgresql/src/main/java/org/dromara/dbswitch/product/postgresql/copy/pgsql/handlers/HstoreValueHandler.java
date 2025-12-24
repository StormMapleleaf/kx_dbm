package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class HstoreValueHandler extends BaseValueHandler<Map<String, String>> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final Map<String, String> value)
      throws IOException {

        ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();

        DataOutputStream hstoreOutput = new DataOutputStream(byteArrayOutput);

        hstoreOutput.writeInt(value.size());

        for (Map.Entry<String, String> entry : value.entrySet()) {
            writeKey(hstoreOutput, entry.getKey());
            writeValue(hstoreOutput, entry.getValue());
    }

        buffer.writeInt(byteArrayOutput.size());
    buffer.write(byteArrayOutput.toByteArray());
  }

  private void writeKey(DataOutputStream buffer, String key) throws IOException {
    writeText(buffer, key);
  }

  private void writeValue(DataOutputStream buffer, String value) throws IOException {
    if (value == null) {
      buffer.writeInt(-1);
    } else {
      writeText(buffer, value);
    }
  }

  private void writeText(DataOutputStream buffer, String text) throws IOException {
    byte[] textBytes = StringUtils.getUtf8Bytes(text);

    buffer.writeInt(textBytes.length);
    buffer.write(textBytes);
  }

  @Override
  public int getLength(Map<String, String> value) {
    throw new UnsupportedOperationException();
  }
}
