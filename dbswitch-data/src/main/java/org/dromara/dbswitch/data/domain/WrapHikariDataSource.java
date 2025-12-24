// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.data.domain;

import org.dromara.dbswitch.common.entity.CloseableDataSource;
import org.dromara.dbswitch.common.entity.InvisibleDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WrapHikariDataSource implements CloseableDataSource {

  private HikariDataSource hikariDataSource;
  private URLClassLoader urlClassLoader;

  public WrapHikariDataSource(HikariDataSource hikariDataSource, URLClassLoader urlClassLoader) {
    this.hikariDataSource = Objects.requireNonNull(hikariDataSource);
    this.urlClassLoader = Objects.requireNonNull(urlClassLoader);
  }

  @Override
  public String getJdbcUrl() {
    DataSource dataSource = hikariDataSource.getDataSource();
    if (null != dataSource && dataSource instanceof InvisibleDataSource) {
      InvisibleDataSource invisibleDataSource = (InvisibleDataSource) dataSource;
      return invisibleDataSource.getJdbcUrl();
    }

    return hikariDataSource.getJdbcUrl();
  }

  @Override
  public String getDriverClass() {
    DataSource dataSource = hikariDataSource.getDataSource();
    if (null != dataSource && dataSource instanceof InvisibleDataSource) {
      InvisibleDataSource invisibleDataSource = (InvisibleDataSource) dataSource;
      return invisibleDataSource.getDriverClassName();
    }
    return hikariDataSource.getDriverClassName();
  }

  @Override
  public String getUserName() {
    DataSource dataSource = hikariDataSource.getDataSource();
    if (null != dataSource && dataSource instanceof InvisibleDataSource) {
      InvisibleDataSource invisibleDataSource = (InvisibleDataSource) dataSource;
      return invisibleDataSource.getProperties().getProperty("user");
    }
    return hikariDataSource.getUsername();
  }

  @Override
  public String getPassword() {
    DataSource dataSource = hikariDataSource.getDataSource();
    if (null != dataSource && dataSource instanceof InvisibleDataSource) {
      InvisibleDataSource invisibleDataSource = (InvisibleDataSource) dataSource;
      return invisibleDataSource.getProperties().getProperty("password");
    }
    return hikariDataSource.getPassword();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return hikariDataSource.getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return hikariDataSource.getConnection(username, password);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return hikariDataSource.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return hikariDataSource.isWrapperFor(iface);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return hikariDataSource.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    hikariDataSource.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    hikariDataSource.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return hikariDataSource.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return hikariDataSource.getParentLogger();
  }

  public void close() {
    hikariDataSource.close();
  }
}
