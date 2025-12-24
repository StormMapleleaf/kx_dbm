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

import org.dromara.dbswitch.admin.model.response.SystemUserDetailResponse;
import org.dromara.dbswitch.admin.entity.SystemUserEntity;
import org.dromara.dbswitch.common.converter.AbstractConverter;

public class SystemUserDetailConverter extends
    AbstractConverter<SystemUserEntity, SystemUserDetailResponse> {

  @Override
  public SystemUserDetailResponse convert(SystemUserEntity user) {
    SystemUserDetailResponse response = new SystemUserDetailResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setRealName(user.getRealName());
    response.setEmail(user.getEmail());
    response.setAddress(user.getAddress());
    response.setLocked(user.getLocked());
    response.setCreateTime(user.getCreateTime());
    response.setUpdateTime(user.getUpdateTime());

    return response;
  }
}
