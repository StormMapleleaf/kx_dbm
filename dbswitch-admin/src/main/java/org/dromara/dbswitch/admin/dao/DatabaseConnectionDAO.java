package org.dromara.dbswitch.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.admin.mapper.DatabaseConnectionMapper;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class DatabaseConnectionDAO {

  @Resource
  private DatabaseConnectionMapper databaseConnectionMapper;

  public void insert(DatabaseConnectionEntity databaseConnectionEntity) {
    databaseConnectionMapper.insert(databaseConnectionEntity);
  }

  public DatabaseConnectionEntity getById(Long id) {
    return databaseConnectionMapper.selectById(id);
  }

  public List<DatabaseConnectionEntity> getByIds(List<Long> ids) {
    if (CollectionUtils.isEmpty(ids)) {
      return Collections.emptyList();
    }
    return databaseConnectionMapper.selectBatchIds(ids);
  }

  public DatabaseConnectionEntity getByName(String name) {
    QueryWrapper<DatabaseConnectionEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(DatabaseConnectionEntity::getName, name);
    return databaseConnectionMapper.selectOne(queryWrapper);
  }

  public List<DatabaseConnectionEntity> listAll(String searchText) {
    return databaseConnectionMapper.selectList(
        Wrappers.<DatabaseConnectionEntity>lambdaQuery()
            .like(StringUtils.hasText(searchText), DatabaseConnectionEntity::getName, searchText)
            .orderByDesc(DatabaseConnectionEntity::getCreateTime)
    );
  }

  public void updateById(DatabaseConnectionEntity databaseConnectionEntity) {
    databaseConnectionMapper.updateById(databaseConnectionEntity);
  }

  public void deleteById(Long id) {
    databaseConnectionMapper.deleteById(id);
  }

  public int getTotalCount() {
    return databaseConnectionMapper.selectCount(null).intValue();
  }

}
