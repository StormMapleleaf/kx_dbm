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
@TableName("DBSWITCH_SYSTEM_LOG")
public class SystemLogEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("type")
  private Integer type;

  @TableField("username")
  private String username;

  @TableField("ip_address")
  private String ipAddress;

  @TableField("module_name")
  private String moduleName;

  @TableField("content")
  private String content;

  @TableField("url_path")
  private String urlPath;

  @TableField("user_agent")
  private String userAgent;

  @TableField("failed")
  private Boolean failed;

  @TableField(value = "exception", jdbcType = JdbcType.LONGVARCHAR, insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
  private String exception;

  @TableField("elapse_seconds")
  private Long elapseSeconds;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;
}
