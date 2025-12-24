package org.dromara.dbswitch.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dromara.dbswitch.admin.entity.SystemUserEntity;
import org.dromara.dbswitch.admin.mapper.SystemUserMapper;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class SystemUserDAO {

  @Resource
  private SystemUserMapper systemUserMapper;

  public SystemUserEntity getById(Long id) {
    return systemUserMapper.selectById(id);
  }

  public SystemUserEntity findByUsername(String username) {
    QueryWrapper<SystemUserEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(SystemUserEntity::getUsername, username);
    return systemUserMapper.selectOne(queryWrapper);
  }

  public void updateUserPassword(String username, String newPassword) {
    SystemUserEntity userEntity = findByUsername(username);
    if (Objects.nonNull(userEntity)) {
      userEntity.setPassword(newPassword);
      systemUserMapper.updateById(userEntity);
    }
  }

}
