package org.dromara.dbswitch.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "DBSWITCH_DATABASE_CONNECTION", autoResultMap = true)
public class DatabaseConnectionEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("name")
  private String name;

  @TableField(value = "type", typeHandler = EnumTypeHandler.class)
  private ProductTypeEnum type;

  @TableField("version")
  private String version;

  @TableField("driver")
  private String driver;

  @TableField("address")
  private String address;

  @TableField("port")
  private String port;

  @TableField("database_name")
  private String databaseName;

  @TableField("character_encoding")
  private String characterEncoding;

  @TableField("url")
  private String url;

  @TableField("username")
  private String username;

  @TableField("password")
  private String password;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp updateTime;
}
