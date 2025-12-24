package org.dromara.dbswitch.product.postgresql.copy.pgsql.converter;

import org.dromara.dbswitch.product.postgresql.copy.pgsql.utils.TimeStampUtils;
import java.time.LocalDate;

public class LocalDateConverter implements IValueConverter<LocalDate, Integer> {

  @Override
  public Integer convert(final LocalDate date) {
    return TimeStampUtils.toPgDays(date);
  }

}
