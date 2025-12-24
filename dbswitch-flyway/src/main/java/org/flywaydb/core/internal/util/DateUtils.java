package org.flywaydb.core.internal.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
        private DateUtils() {
    }

        public static String formatDateAsIsoString(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

        public static String formatTimeAsIsoString(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

        public static Date toDate(int year, int month, int day) {
        return new GregorianCalendar(year, month - 1, day).getTime();
    }

        public static String toDateString(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        String year = "" + calendar.get(Calendar.YEAR);
        String month = StringUtils.trimOrLeftPad("" + (calendar.get(Calendar.MONTH) + 1), 2, '0');
        String day = StringUtils.trimOrLeftPad("" + calendar.get(Calendar.DAY_OF_MONTH), 2, '0');
        return year + "-" + month + "-" + day;
    }
}