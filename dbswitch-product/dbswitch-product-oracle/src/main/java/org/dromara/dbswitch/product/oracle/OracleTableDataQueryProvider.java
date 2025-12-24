package org.dromara.dbswitch.product.oracle;

import java.sql.Types;
import org.dromara.dbswitch.common.entity.IncrementPoint;
import org.dromara.dbswitch.common.util.JdbcTypesUtils;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.query.DefaultTableDataQueryProvider;

public class OracleTableDataQueryProvider extends DefaultTableDataQueryProvider {

  private static final String TIMESTAMP_PATTERN = "yyyy-mm-dd hh24:mi:ss.ff";
  private static final String DATE_PATTERN = "yyyy-mm-dd hh24:mi:ss";

  public OracleTableDataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  protected String toGreaterThanCondition(IncrementPoint point) {
    StringBuilder sb = new StringBuilder();
    sb.append(quoteName(point.getColumnName()));
    sb.append(" > ");
    if (JdbcTypesUtils.isInteger(point.getJdbcType())) {
      sb.append(point.getMaxValue());
    } else if (JdbcTypesUtils.isDateTime(point.getJdbcType())) {
      if (Types.TIMESTAMP == point.getJdbcType() || Types.TIMESTAMP_WITH_TIMEZONE == point.getJdbcType()) {
        sb.append(String.format("TO_TIMESTAMP('%s', '%s')", point.getMaxValue(), TIMESTAMP_PATTERN));
      } else {
        sb.append(String.format("TO_DATE('%s', '%s')", point.getMaxValue(), DATE_PATTERN));
      }
    }
    return sb.toString();
  }
}
