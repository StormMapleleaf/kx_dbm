package org.dromara.dbswitch.admin.listener;

import cn.hutool.extra.spring.SpringUtil;
import org.dromara.dbswitch.admin.dao.JobLogbackDAO;
import org.dromara.dbswitch.admin.logback.LogbackAppenderRegister;
import org.dromara.dbswitch.admin.logback.LogbackEventContent;
import java.util.Arrays;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;

public class DbswitchAdminStartedEventListener implements ApplicationListener<ApplicationContextInitializedEvent> {

  private static final String[] LOGGER_CLASS_NAME = {
      "org.dromara.dbswitch.data.service.MigrationService",
      "org.dromara.dbswitch.data.service.DefaultReaderRobot",
      "org.dromara.dbswitch.data.handler.ReaderTaskThread",
      "org.dromara.dbswitch.data.service.DefaultWriterRobot",
      "org.dromara.dbswitch.data.handler.WriterTaskThread",
      "org.dromara.dbswitch.common.util.MachineInfoUtils"
  };

  @Override
  public void onApplicationEvent(ApplicationContextInitializedEvent event) {
    LogbackAppenderRegister.addDatabaseAppender(Arrays.asList(LOGGER_CLASS_NAME), this::recordLogContent);
  }

  private void recordLogContent(LogbackEventContent log) {
    SpringUtil.getBean(JobLogbackDAO.class).insert(log.getIdentity(), log.getContent());
  }

}