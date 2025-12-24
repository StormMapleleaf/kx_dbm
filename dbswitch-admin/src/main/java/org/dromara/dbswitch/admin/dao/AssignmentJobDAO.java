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
import org.dromara.dbswitch.admin.model.ops.OpsTaskJobTrend;
import org.dromara.dbswitch.admin.type.JobStatusEnum;
import org.dromara.dbswitch.admin.entity.AssignmentJobEntity;
import org.dromara.dbswitch.admin.mapper.AssignmentJobMapper;
import java.sql.Timestamp;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class AssignmentJobDAO {

  @Resource
  private AssignmentJobMapper assignmentJobMapper;

  public AssignmentJobEntity newAssignmentJob(Long assignmentId, Integer scheduleMode,
      String jobKey) {
    AssignmentJobEntity assignmentJobEntity = new AssignmentJobEntity();
    assignmentJobEntity.setAssignmentId(assignmentId);
    assignmentJobEntity.setJobKey(jobKey);
    assignmentJobEntity.setScheduleMode(scheduleMode);
    assignmentJobEntity.setStartTime(new Timestamp(System.currentTimeMillis()));
    assignmentJobEntity.setFinishTime(new Timestamp(System.currentTimeMillis()));
    assignmentJobEntity.setStatus(JobStatusEnum.RUNNING.getValue());
    assignmentJobMapper.insert(assignmentJobEntity);
    return assignmentJobEntity;
  }

  public AssignmentJobEntity getById(Long id) {
    return assignmentJobMapper.selectById(id);
  }

  public List<AssignmentJobEntity> getByAssignmentId(Long assignmentId) {
    QueryWrapper<AssignmentJobEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentJobEntity::getAssignmentId, assignmentId)
        .orderByDesc(AssignmentJobEntity::getId);
    return assignmentJobMapper.selectList(queryWrapper);
  }

  public AssignmentJobEntity getLatestJobEntity(Long assignmentId) {
    QueryWrapper<AssignmentJobEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentJobEntity::getAssignmentId, assignmentId)
        .orderByDesc(AssignmentJobEntity::getId)
        .last(" limit 1 ");
    return assignmentJobMapper.selectOne(queryWrapper);
  }

  public void updateSelective(AssignmentJobEntity assignmentJobEntity) {
    assignmentJobMapper.updateById(assignmentJobEntity);
  }

  public int getCountByStatus(Integer status) {
    QueryWrapper<AssignmentJobEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentJobEntity::getStatus, status);
    return assignmentJobMapper.selectCount(queryWrapper).intValue();
  }

  public int getTotalCount() {
    return assignmentJobMapper.selectCount(null).intValue();
  }

  public List<OpsTaskJobTrend> queryTaskJobTrend(Integer days) {
    return assignmentJobMapper.queryTaskJobTrend(days);
  }

  public void updateStatus(JobStatusEnum originalStatus, JobStatusEnum targetStatus, String errorLog) {
    AssignmentJobEntity updateSet = new AssignmentJobEntity();
    updateSet.setStatus(targetStatus.getValue());
    if (JobStatusEnum.FAIL.equals(targetStatus)) {
      updateSet.setErrorLog(errorLog);
    }
    QueryWrapper<AssignmentJobEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(AssignmentJobEntity::getStatus, originalStatus.getValue());
    assignmentJobMapper.update(updateSet, queryWrapper);
  }

}
