package org.dromara.dbswitch.common.entity;

import java.io.Closeable;
import javax.sql.DataSource;

public interface CloseableDataSource extends DataSource, Closeable {

  String getJdbcUrl();

  String getDriverClass();

  String getUserName();

  String getPassword();
}
