package org.dromara.dbswitch.product.oceanbase;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.product.oracle.OracleTableDataWriteProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.write.DefaultTableDataWriteProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;
import java.util.List;

public class OceanbaseTableDataWriteProvider extends DefaultTableDataWriteProvider {

  private final TableDataWriteProvider delegate;
  private final ProductTypeEnum dialect;

  public OceanbaseTableDataWriteProvider(ProductFactoryProvider factoryProvider, TableDataWriteProvider delegate) {
    super(factoryProvider);
    this.delegate = delegate;
    if (delegate instanceof OracleTableDataWriteProvider) {
      this.dialect = ProductTypeEnum.ORACLE;
    } else {
      this.dialect = ProductTypeEnum.MYSQL;
    }
  }

  @Override
  protected String quoteName(String name) {
    return this.dialect.quoteName(name);
  }

  @Override
  protected String quoteSchemaTableName(String schemaName, String tableName) {
    return this.dialect.quoteSchemaTableName(schemaName, tableName);
  }

  @Override
  public void prepareWrite(String schemaName, String tableName, List<String> fieldNames) {
    this.delegate.prepareWrite(schemaName, tableName, fieldNames);
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    return this.delegate.write(fieldNames, recordValues);
  }
}
