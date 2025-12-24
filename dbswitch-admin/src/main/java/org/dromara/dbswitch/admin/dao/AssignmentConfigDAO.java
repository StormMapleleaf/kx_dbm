package org.dromara.dbswitch.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dromara.dbswitch.admin.entity.AssignmentConfigEntity;
import org.dromara.dbswitch.admin.mapper.AssignmentConfigMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class AssignmentConfigDAO {

  @Resource
  private AssignmentConfigMapper assignmentConfigMapper;

  public void insert(AssignmentConfigEntity assignmentConfigEntity) {
    assignmentConfigMapper.insert(assignmentConfigEntity);
  }

  public AssignmentConfigEntity getById(Long id) {
    return assignmentConfigMapper.selectById(id);
  }

  public AssignmentConfigEntity getByAssignmentTaskId(Long taskId) {
    QueryWrapper<AssignmentConfigEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentConfigEntity::getAssignmentId, taskId);
    return assignmentConfigMapper.selectOne(queryWrapper);
  }

  public List<AssignmentConfigEntity> getByConnectionId(Long connId) {
    QueryWrapper<AssignmentConfigEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentConfigEntity::getSourceConnectionId, connId)
        .or().eq(AssignmentConfigEntity::getTargetConnectionId, connId);
    return assignmentConfigMapper.selectList(queryWrapper);
  }

  public void updateSelective(AssignmentConfigEntity assignmentConfigEntity) {
    assignmentConfigMapper.updateById(assignmentConfigEntity);
  }

  public void deleteByAssignmentTaskId(Long taskId) {
    QueryWrapper<AssignmentConfigEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentConfigEntity::getAssignmentId, taskId);
    assignmentConfigMapper.delete(queryWrapper);
  }

}
