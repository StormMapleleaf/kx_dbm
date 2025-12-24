package org.dromara.dbswitch.admin.controller.converter;

import org.dromara.dbswitch.admin.model.response.AssignmentsDataResponse;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.common.converter.AbstractConverter;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;


public class AssignmentsConverter extends
    AbstractConverter<AssignmentTaskEntity, AssignmentsDataResponse> {

	@Override
	public AssignmentsDataResponse convert(AssignmentTaskEntity assignmentTaskEntity) {
		AssignmentsDataResponse response = new AssignmentsDataResponse();
		response.setId(assignmentTaskEntity.getId());
		response.setName(assignmentTaskEntity.getName());
		response.setDescription(assignmentTaskEntity.getDescription());
		response.setScheduleMode(assignmentTaskEntity.getScheduleMode().getName());
		response.setCronExpression(assignmentTaskEntity.getCronExpression());
		response.setIsPublished(assignmentTaskEntity.getPublished());
		response.setCreateTime(DateUtil.format(assignmentTaskEntity.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN));
		response.setUpdateTime(DateUtil.format(assignmentTaskEntity.getUpdateTime(), DatePattern.NORM_DATETIME_PATTERN));
		return response;
	}
}
