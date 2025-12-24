package org.dromara.dbswitch.product.postgresql.copy.pgsql.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class TimeStampUtils {

  private TimeStampUtils() {
  }

  private static final LocalDateTime JavaEpoch = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

  private static final LocalDateTime PostgresEpoch = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

  private static final long DaysBetweenJavaAndPostgresEpochs = ChronoUnit.DAYS
      .between(JavaEpoch, PostgresEpoch);

  public static long convertToPostgresTimeStamp(LocalDateTime localDateTime) {

    if (localDateTime == null) {
      throw new IllegalArgumentException("localDateTime");
    }
        long timeInNanoseconds = localDateTime
        .toLocalTime()
        .toNanoOfDay();

        long timeInMicroseconds = timeInNanoseconds / 1000;

        if (localDateTime.isBefore(PostgresEpoch)) {
      long dateInMicroseconds =
          (localDateTime.toLocalDate().toEpochDay() - DaysBetweenJavaAndPostgresEpochs)
              * 86400000000L;

      return dateInMicroseconds + timeInMicroseconds;
    } else {
      long dateInMicroseconds =
          (DaysBetweenJavaAndPostgresEpochs - localDateTime.toLocalDate().toEpochDay())
              * 86400000000L;

      return -(dateInMicroseconds - timeInMicroseconds);
    }
  }

  public static int toPgDays(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        long secs = toPgSecs(getSecondsSinceJavaEpoch(dateTime));
        return (int) TimeUnit.SECONDS.toDays(secs);
  }

  public static Long toPgSecs(LocalDateTime dateTime) {
        long secs = toPgSecs(getSecondsSinceJavaEpoch(dateTime));
        return TimeUnit.SECONDS.toMicros(secs);
  }

  private static long getSecondsSinceJavaEpoch(LocalDateTime localDateTime) {
        OffsetDateTime zdt = localDateTime.atOffset(ZoneOffset.UTC);
        long milliseconds = zdt.toInstant().toEpochMilli();
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
  }


  @SuppressWarnings("checkstyle:magicnumber")
  private static long toPgSecs(final long seconds) {
    long secs = seconds;
        secs -= 946684800L;

        if (secs < -13165977600L) {       secs -= 86400 * 10;
      if (secs < -15773356800L) {         int years = (int) ((secs + 15773356800L) / -3155823050L);
        years++;
        years -= years / 4;
        secs += years * 86400;
      }
    }

    return secs;
  }
}