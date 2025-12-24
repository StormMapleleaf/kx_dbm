// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.controller.converter;

import org.dromara.dbswitch.admin.model.response.DbConnectionDetailResponse;
import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.common.converter.AbstractConverter;

public class DbConnectionDetailConverter extends
    AbstractConverter<DatabaseConnectionEntity, DbConnectionDetailResponse> {

  @Override
  public DbConnectionDetailResponse convert(DatabaseConnectionEntity databaseConnectionEntity) {
    DbConnectionDetailResponse response = new DbConnectionDetailResponse();
    response.setId(databaseConnectionEntity.getId());
    response.setName(databaseConnectionEntity.getName());
    response.setTypeName(databaseConnectionEntity.getType().getName());
    response.setType(databaseConnectionEntity.getType());
    response.setVersion(databaseConnectionEntity.getVersion());
    response.setDriver(databaseConnectionEntity.getDriver());
    response.setAddress(databaseConnectionEntity.getAddress());
    response.setPort(databaseConnectionEntity.getPort());
    response.setDatabaseName(databaseConnectionEntity.getDatabaseName());
    response.setCharacterEncoding(databaseConnectionEntity.getCharacterEncoding());
    response.setUrl(databaseConnectionEntity.getUrl());
    response.setUsername(databaseConnectionEntity.getUsername());
    response.setPassword(databaseConnectionEntity.getPassword());
    response.setCreateTime(databaseConnectionEntity.getCreateTime());
    response.setUpdateTime(databaseConnectionEntity.getUpdateTime());

    return response;
  }
}
