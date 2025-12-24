package org.dromara.dbswitch.admin.model.response;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础数据类
 *
 * @author Jiaju Zhuang
 **/
@Getter
@Setter
@ColumnWidth(25)
@EqualsAndHashCode
public class AssignmentsDataResponse {

    @ExcelProperty("ID编号")
    private Long id;

    @ExcelProperty("任务名")
    private String name;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("调度模式")
    private String scheduleMode;

    @ExcelProperty("Cron表达式")
    private String cronExpression;

    @ExcelProperty("是否已发布")
    private Boolean isPublished;

    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;

    @ExcelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String updateTime;

    @ExcelProperty("源端数据源")
    private String sourceSchema;

    @ExcelProperty("源端数据源类型")
    private String sourceType;

    @ExcelProperty("目标端数据源")
    private String targetSchema;

    @ExcelProperty("目标端数据源类型")
    private String targetType;

    @ExcelProperty("运行状态")
    private String runStatus;

}
