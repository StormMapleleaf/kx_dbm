package org.dromara.dbswitch.product.hive;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.common.util.ExamineUtils;
import org.dromara.dbswitch.core.schema.SourceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HiveTblUtils {

  // hive.sql.database.type: MYSQL, POSTGRES, ORACLE, DERBY, DB2
  private final static List<ProductTypeEnum> supportedProductTypes =
      Arrays.asList(ProductTypeEnum.MYSQL, ProductTypeEnum.ORACLE,
          ProductTypeEnum.DB2, ProductTypeEnum.POSTGRESQL);

  /**
   * https://cwiki.apache.org/confluence/display/Hive/JDBC+Storage+Handler
   *
   * @return Map<String, String>
   */
  public static Map<String, String> getTblProperties(SourceProperties tblProperties) {
    ProductTypeEnum sourceProductType = tblProperties.getProductType();
    String sourceSchemaName = tblProperties.getSchemaName();
    String sourceTableName = tblProperties.getTableName();
    ExamineUtils.check(supportedProductTypes.contains(sourceProductType),
        "Unsupported data from %s to Hive", sourceProductType.name());

    Map<String, String> ret = new HashMap<>();

    String querySql = String.format("SELECT %s FROM %s",
        tblProperties.getColumnNames().stream()
            .map(s -> sourceProductType.quoteName(s))
            .collect(Collectors.joining(",")),
        sourceProductType.quoteSchemaTableName(sourceSchemaName, sourceTableName));

    String databaseType;
    if (ProductTypeEnum.POSTGRESQL == sourceProductType) {
      databaseType = "POSTGRES";
    } else if (ProductTypeEnum.SQLSERVER == sourceProductType) {
      databaseType = "MSSQL";
    } else {
      databaseType = sourceProductType.name().toUpperCase();
    }
    ret.put("hive.sql.database.type", databaseType);
    ret.put("hive.sql.jdbc.driver", tblProperties.getDriverClass());
    ret.put("hive.sql.jdbc.url", tblProperties.getJdbcUrl());
    ret.put("hive.sql.dbcp.username", tblProperties.getUsername());
    ret.put("hive.sql.dbcp.password", tblProperties.getPassword());
    ret.put("hive.sql.query", querySql);
    ret.put("hive.sql.jdbc.read-write", "read");
    ret.put("hive.sql.jdbc.fetch.size", "2000");
    ret.put("hive.sql.dbcp.maxActive", "1");

    return ret;
  }
}
