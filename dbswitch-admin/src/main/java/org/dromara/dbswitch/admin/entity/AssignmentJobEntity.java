// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("DBSWITCH_ASSIGNMENT_JOB")
public class AssignmentJobEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("assignment_id")
  private Long assignmentId;

  @TableField("job_key")
  private String jobKey;

  @TableField("schedule_mode")
  private Integer scheduleMode;

  @TableField("start_time")
  private Timestamp startTime;

  @TableField("finish_time")
  private Timestamp finishTime;

  @TableField("status")
  private Integer status;

  @TableField(value = "error_log", jdbcType = JdbcType.LONGVARCHAR, insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
  private String errorLog;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp updateTime;
}
