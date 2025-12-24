// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.model.request;

import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DbConnectionUpdateRequest {

  private Long id;
  private String name;
  private ProductTypeEnum type;
  private String version;
  private String driver;
  private String address;
  private String port;
  private String databaseName;
  private String characterEncoding;
  private String url;
  private String username;
  private String password;

  public DatabaseConnectionEntity toDatabaseConnection() {
    DatabaseConnectionEntity databaseConnectionEntity = new DatabaseConnectionEntity();
    databaseConnectionEntity.setId(id);
    databaseConnectionEntity.setName(name);
    databaseConnectionEntity.setType(type);
    databaseConnectionEntity.setVersion(version.trim());
    databaseConnectionEntity.setDriver(driver.trim());
    databaseConnectionEntity.setAddress(address.trim());
    databaseConnectionEntity.setPort(port.trim());
    databaseConnectionEntity.setDatabaseName(databaseName.trim());
    databaseConnectionEntity.setCharacterEncoding(characterEncoding.trim());
    databaseConnectionEntity.setUrl(url.trim());
    databaseConnectionEntity.setUsername(username);
    databaseConnectionEntity.setPassword(password);

    return databaseConnectionEntity;
  }

}
