package org.dromara.dbswitch.admin.util;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.dromara.dbswitch.admin.common.exception.DbswitchException;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.quartz.CronExpression;


public final class CronExprUtils {

  public static final int MIN_INTERVAL_SECONDS = 120;


  public static void checkCronExpressionValid(String cronExpression, int minIntervalSeconds) {
    if (StringUtils.isNotBlank(cronExpression)) {
      CronExpression expression;
      try {
        expression = new CronExpression(cronExpression);
      } catch (ParseException e) {
        throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT, String.format("cron表达式%s无效", cronExpression));
      }
      Date nextDate = expression.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
      if (null == nextDate) {
        throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT,
            String.format("cron表达式[%s]不可以在历史时间运行", cronExpression));
      }
      Date calculateDate = expression.getNextValidTimeAfter(new Date(nextDate.getTime() + 1));
      if (null != calculateDate) {
        long intervalSeconds = (calculateDate.getTime() - nextDate.getTime()) / 1000;
        if (intervalSeconds < minIntervalSeconds) {
          throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT,
              String.format("cron表达式[%s]运行间隔时间为%d秒, 小于设定的阈值 [%s秒]",
                  cronExpression, intervalSeconds, minIntervalSeconds));
        }
      }
    }
  }
}
