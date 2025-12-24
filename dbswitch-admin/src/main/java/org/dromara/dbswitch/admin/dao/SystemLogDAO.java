// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dromara.dbswitch.admin.type.LogTypeEnum;
import org.dromara.dbswitch.admin.entity.SystemLogEntity;
import org.dromara.dbswitch.admin.mapper.SystemLogMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class SystemLogDAO {

  @Resource
  private SystemLogMapper systemLogMapper;

  public void insert(SystemLogEntity systemLogEntity) {
    systemLogMapper.insert(systemLogEntity);
  }

  public List<SystemLogEntity> listAll(LogTypeEnum logType) {
    QueryWrapper<SystemLogEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(SystemLogEntity::getType, logType.getValue())
        .orderByDesc(SystemLogEntity::getCreateTime);
    return systemLogMapper.selectList(queryWrapper);
  }

  public SystemLogEntity getById(Long id) {
    return systemLogMapper.selectById(id);
  }

}
