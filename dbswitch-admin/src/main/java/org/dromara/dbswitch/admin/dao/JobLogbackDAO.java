package org.dromara.dbswitch.admin.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.dbswitch.admin.entity.JobLogbackEntity;
import org.dromara.dbswitch.admin.mapper.JobLogbackMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class JobLogbackDAO {

  @Resource
  private JobLogbackMapper jobLogbackMapper;

  public void insert(String uuid, String content) {
    jobLogbackMapper.insert(JobLogbackEntity.builder().uuid(uuid).content(content).build());
  }

  public List<JobLogbackEntity> getTailByUuid(String uuid) {
    List<JobLogbackEntity> result = jobLogbackMapper.selectList(
        Wrappers.<JobLogbackEntity>lambdaQuery()
            .select(JobLogbackEntity::getId, JobLogbackEntity::getContent)
            .eq(JobLogbackEntity::getUuid, uuid)
            .orderByDesc(JobLogbackEntity::getId)
    );
    Collections.reverse(result);
    return result;
  }

  public List<JobLogbackEntity> getNextByUuid(String uuid, Long baseId) {
    return jobLogbackMapper.selectList(
        Wrappers.<JobLogbackEntity>lambdaQuery()
            .select(JobLogbackEntity::getId, JobLogbackEntity::getContent)
            .eq(JobLogbackEntity::getUuid, uuid)
            .gt(JobLogbackEntity::getId, baseId)
            .orderByAsc(JobLogbackEntity::getId)
    );
  }

  public void deleteOldest(Integer days) {
    if (Objects.nonNull(days)) {
      jobLogbackMapper.deleteByDays(days);
    }
  }

}
