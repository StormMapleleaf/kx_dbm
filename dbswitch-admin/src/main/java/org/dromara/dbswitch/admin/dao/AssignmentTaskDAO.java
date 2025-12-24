package org.dromara.dbswitch.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.admin.mapper.AssignmentTaskMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class AssignmentTaskDAO {

  @Resource
  private AssignmentTaskMapper assignmentTaskMapper;

  public void insert(AssignmentTaskEntity assignment) {
    assignmentTaskMapper.insert(assignment);
  }

  public void updateById(AssignmentTaskEntity assignment) {
    assignmentTaskMapper.updateById(assignment);
  }

  public List<AssignmentTaskEntity> listAll(String searchText) {
    return assignmentTaskMapper.selectList(
        Wrappers.<AssignmentTaskEntity>lambdaQuery()
            .like(StringUtils.hasText(searchText), AssignmentTaskEntity::getName, searchText)
            .orderByDesc(AssignmentTaskEntity::getCreateTime)
    );
  }

  public AssignmentTaskEntity getById(Long id) {
    return assignmentTaskMapper.selectById(id);
  }

  public void deleteById(Long id) {
    assignmentTaskMapper.deleteById(id);
  }

  public int getTotalCount() {
    return assignmentTaskMapper.selectList(null).size();
  }

  public int getPublishedCount() {
    QueryWrapper<AssignmentTaskEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentTaskEntity::getPublished, Boolean.TRUE);
    return assignmentTaskMapper.selectCount(queryWrapper).intValue();
  }

}
