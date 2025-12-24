package org.dromara.dbswitch.core.provider;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.common.util.ExamineUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductProviderFactory {

  private static Map<ProductTypeEnum, Class<? extends ProductFactoryProvider>> providers;

  static {
    providers = new ConcurrentHashMap<>();
  }

  public static void register(ProductTypeEnum type, String classPath) {
    log.info("Register product {} by subclass :{} ", type, classPath);
    ClassLoader classLoader = ProductProviderFactory.class.getClassLoader();

    Class<?> clazz = null;
    try {
      clazz = classLoader.loadClass(classPath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Check subclass
    ExamineUtils.check(ProductFactoryProvider.class.isAssignableFrom(clazz),
        "Class '%s' is not a subclass of " +
            "class ProductFactoryProvider", clazz.getName());

    // Check exists
    ExamineUtils.check(!providers.containsKey(type),
        "Exists ProductFactoryProvider: %s (%s)",
        type.name(), providers.get(type.name()));

    // Register class
    providers.put(type, (Class) clazz);
  }

  public static ProductFactoryProvider newProvider(ProductTypeEnum type, DataSource dataSource) {
    Class<? extends ProductFactoryProvider> clazz = providers.get(type);
    ExamineUtils.check(clazz != null,
        "Not exists ProductFactoryProvider: %s", type);

    assert ProductFactoryProvider.class.isAssignableFrom(clazz);
    ProductFactoryProvider instance = null;
    try {
      instance = clazz.getDeclaredConstructor(DataSource.class).newInstance(dataSource);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    ExamineUtils.check(type.equals(instance.getProductType()),
        "ProductFactoryProvider with type '%s' " +
            "can't be opened by product type '%s'",
        instance.getProductType(), type);
    return instance;
  }

}
