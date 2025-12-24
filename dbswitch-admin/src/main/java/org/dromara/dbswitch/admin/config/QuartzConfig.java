package org.dromara.dbswitch.admin.config;

import java.io.IOException;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Slf4j
@Configuration("dbswitchQuartzConfig")
public class QuartzConfig {

  @Bean("quartzProperties")
  public Properties quartzProperties(DataSourceProperties dataSourceProperties) throws IOException {
    PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

    Properties prop = new Properties();


    prop.put("org.quartz.scheduler.instanceName", "DBSwitch-Quartz-Scheduler");
    prop.put("org.quartz.scheduler.instanceId", "AUTO");
    prop.put("org.quartz.scheduler.rmi.export", "false");
    prop.put("org.quartz.scheduler.rmi.proxy", "false");
    prop.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false");


    prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
    prop.put("org.quartz.threadPool.threadCount", "20");
    prop.put("org.quartz.threadPool.threadPriority", "5");
    prop.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

    prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
    prop.put("org.quartz.jobStore.driverDelegateClass",
        "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
    prop.put("org.quartz.jobStore.useProperties", "true");
    prop.put("org.quartz.jobStore.isClustered", "true");
    prop.put("org.quartz.jobStore.misfireThreshold", "12000");
    prop.put("org.quartz.jobStore.tablePrefix", "DBSWITCH_");

    if (StringUtils.isNotBlank(dataSourceProperties.getUrl())
        && dataSourceProperties.getUrl().startsWith("jdbc:postgresql://")) {
      prop.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
    }

    propertiesFactoryBean.setProperties(prop);
    propertiesFactoryBean.afterPropertiesSet();

    return propertiesFactoryBean.getObject();
  }

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory,
      Properties quartzProperties) {
    SchedulerFactoryBean factory = new SchedulerFactoryBean();
    factory.setOverwriteExistingJobs(true);
    factory.setAutoStartup(true);
    factory.setDataSource(dataSource);
    factory.setJobFactory(jobFactory);
    factory.setQuartzProperties(quartzProperties);
    return factory;
  }

  @Bean
  public JobFactory jobFactory(ApplicationContext applicationContext) {
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);
    return jobFactory;
  }

  public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements
      ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
      beanFactory = context.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
      final Object job = super.createJobInstance(bundle);
      beanFactory.autowireBean(job);
      return job;
    }

  }

}
