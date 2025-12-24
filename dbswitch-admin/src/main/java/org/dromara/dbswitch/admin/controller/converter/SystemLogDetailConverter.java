package org.dromara.dbswitch.admin.controller.converter;

import org.dromara.dbswitch.admin.model.response.SystemLogDetailResponse;
import org.dromara.dbswitch.admin.entity.SystemLogEntity;
import org.dromara.dbswitch.common.converter.AbstractConverter;

public class SystemLogDetailConverter extends
    AbstractConverter<SystemLogEntity, SystemLogDetailResponse> {

  @Override
  public SystemLogDetailResponse convert(SystemLogEntity systemLogEntity) {
    SystemLogDetailResponse response = new SystemLogDetailResponse();
    response.setId(systemLogEntity.getId());
    response.setUsername(systemLogEntity.getUsername());
    response.setIpAddress(systemLogEntity.getIpAddress());
    response.setModuleName(systemLogEntity.getModuleName());
    response.setContent(systemLogEntity.getContent());
    response.setUrlPath(systemLogEntity.getUrlPath());
    response.setUserAgent(systemLogEntity.getUserAgent());
    response.setFailed(systemLogEntity.getFailed());
    response.setException(systemLogEntity.getException());
    response.setElapseSeconds(systemLogEntity.getElapseSeconds());
    response.setCreateTime(systemLogEntity.getCreateTime());

    return response;
  }

}
