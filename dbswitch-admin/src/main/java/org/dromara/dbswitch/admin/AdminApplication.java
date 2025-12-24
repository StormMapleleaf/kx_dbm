package org.dromara.dbswitch.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan({"org.dromara.dbswitch.admin.mapper"})
@SpringBootApplication
public class AdminApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(AdminApplication.class);
    springApplication.setBannerMode(Banner.Mode.OFF);
    springApplication.run(args);
  }

}
