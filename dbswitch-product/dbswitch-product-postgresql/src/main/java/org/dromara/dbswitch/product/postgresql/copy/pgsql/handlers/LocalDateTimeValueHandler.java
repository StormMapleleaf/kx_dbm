package org.dromara.dbswitch.product.postgresql.copy.pgsql.handlers;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.converter.IValueConverter;
import org.dromara.dbswitch.product.postgresql.copy.pgsql.converter.LocalDateTimeConverter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeValueHandler extends BaseValueHandler<LocalDateTime> {

  private IValueConverter<LocalDateTime, Long> dateTimeConverter;

  public LocalDateTimeValueHandler() {
    this(new LocalDateTimeConverter());
  }

  public LocalDateTimeValueHandler(IValueConverter<LocalDateTime, Long> dateTimeConverter) {
    this.dateTimeConverter = dateTimeConverter;
  }

  @Override
  protected void internalHandle(DataOutputStream buffer, final LocalDateTime value)
      throws IOException {
    buffer.writeInt(8);
    buffer.writeLong(dateTimeConverter.convert(value));
  }

  @Override
  public int getLength(LocalDateTime value) {
    return 8;
  }
}
