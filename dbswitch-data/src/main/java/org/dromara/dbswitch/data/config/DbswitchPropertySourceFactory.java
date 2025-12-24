package org.dromara.dbswitch.data.config;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;


public class DbswitchPropertySourceFactory extends DefaultPropertySourceFactory {

  private static final String suffixYml = ".yml";
  private static final String suffixYaml = ".yaml";

  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource resource)
      throws IOException {
    String sourceName = name != null ? name : resource.getResource().getFilename();
    if (!resource.getResource().exists()) {
      return new PropertiesPropertySource(sourceName, new Properties());
    } else if (sourceName.endsWith(suffixYml) || sourceName.endsWith(suffixYaml)) {
      YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
      factory.setResources(resource.getResource());
      factory.afterPropertiesSet();
      return new PropertiesPropertySource(sourceName, factory.getObject());
    } else {
      return super.createPropertySource(name, resource);
    }
  }

}
