package org.dromara.dbswitch.core.provider;

import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.common.util.ExamineUtils;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class AbstractFactoryProvider implements ProductFactoryProvider {

  private DataSource dataSource;

  protected AbstractFactoryProvider(DataSource dataSource) {
    ExamineUtils.checkNotNull(dataSource, "datasource");
    this.dataSource = dataSource;
  }

  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public final ProductTypeEnum getProductType() {
    Product annotation = getClass().getAnnotation(Product.class);
    ExamineUtils.checkState(
        Objects.nonNull(annotation),
        "Should use Product annotation for class : %s",
        getClass().getName());
    return annotation.value();
  }

}
