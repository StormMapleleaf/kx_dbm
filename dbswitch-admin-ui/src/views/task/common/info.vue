<template>
  <div style="margin-top: 15px">
    <el-card class="box-card">
      <el-row class="row-gutter">
        <el-col :span="2">
          <label class="key-text">任务名称</label>
        </el-col>
        <el-col :span="10">
          <label class="value-text">{{ infoform.name }}</label>
        </el-col>
        <el-col :span="2">
          <label class="key-text">任务类型</label>
        </el-col>
        <el-col :span="10">
          <label class="value-text">
            普通任务
          </label>
        </el-col>
      </el-row>
      <el-row class="row-gutter">
        <el-col :span="2">
          <label class="key-text">集成模式</label>
        </el-col>
        <el-col :span="10">
          <label class="value-text">
            <span v-if="infoform.scheduleMode == 'MANUAL'">
              手动
            </span>
            <span v-if="infoform.scheduleMode == 'SYSTEM_SCHEDULED'">
              定时
            </span>
          </label>
        </el-col>
        <el-col :span="2">
          <label class="key-text">调度计划</label>
        </el-col>
        <el-col :span="10">
          <label class="value-text">
            <span v-if="infoform.scheduleMode == 'MANUAL'">
              --
            </span>
            <span v-if="infoform.scheduleMode == 'SYSTEM_SCHEDULED'">
              {{ infoform.cronExpression }}
            </span>
          </label>
        </el-col>
      </el-row>
      <el-row class="row-gutter">
        <el-col :span="2">
          <label class="key-text">描述</label>
        </el-col>
        <el-col :span="22">
          <label class="value-text">{{ infoform.description }}</label>
        </el-col>
      </el-row>
    </el-card>

    <div class="common-box">
      <div class="datainfo">
        <div class="source">
          <div class="head">
            <div class="head-img">
              <el-image style="width: 60px; height: 60px"
                        :src="require('@/assets/icons/' + infoform.sourceTypeName +'.png')"></el-image>
            </div>
            <div class="head-text">
              <div class="title">{{ infoform.sourceConnectionName }}</div>
              <div class="sub-title">源端数据源</div>
            </div>
          </div>
          <div class="body">
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">源端schema</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text">{{ infoform.sourceSchema }}</label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">源端表类型</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text">{{ infoform.tableType }}</label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">源端表选择方式</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text">{{
                        infoform.includeOrExclude === 'INCLUDE' ? '包含表' : '排除表'
                      }}</label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">源端表名列表</label>
              </el-col>
              <el-col :span="16">
                <div class="table-name-list">
                  <ul>
                    <li v-for="(item,index) in infoform.sourceTables"
                        :key="index">{{item}}</li>
                  </ul>
                </div>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="4">
                <label class="key-text">增量同步配置</label>
              </el-col>
              <el-col :span="20">
                <el-table :data="infoform.incrTableColumns"
                          style="width: 100%"
                          :row-class-name="tableRowClassName">
                  <el-table-column prop="tableName"
                                   label="增量同步表名">
                  </el-table-column>
                  <el-table-column prop="columnName"
                                   label="增量标识字段">
                  </el-table-column>
                </el-table>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">同步前置执行SQL脚本</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text">
                  <span v-show="!infoform.sourceBeforeSqlScripts || infoform.sourceBeforeSqlScripts.length==0">[SQL脚本内容为空]</span>
                  <span v-show="infoform.sourceBeforeSqlScripts && infoform.sourceBeforeSqlScripts.length>0">{{
                          infoform.sourceBeforeSqlScripts
                        }}</span>
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">同步后置执行SQL脚本</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text">
                  <span v-show="!infoform.sourceAfterSqlScripts || infoform.sourceAfterSqlScripts.length==0">[SQL脚本内容为空]</span>
                  <span v-show="infoform.sourceAfterSqlScripts && infoform.sourceAfterSqlScripts.length>0">{{
                          infoform.sourceAfterSqlScripts
                        }}</span>
                </label>
              </el-col>
            </el-row>
          </div>
        </div>
        <div class="target">
          <div class="head">
            <div class="head-img">
              <el-image style="width: 60px; height: 60px"
                        :src="require('@/assets/icons/' + infoform.targetTypeName +'.png')"></el-image>
            </div>
            <div class="head-text">
              <div class="title">{{ infoform.targetConnectionName }}</div>
              <div class="sub-title">目标端数据源</div>
            </div>
          </div>
          <div class="body">
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">目地端schema</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text">{{ infoform.targetSchema }}</label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">自动同步模式</label>
              </el-col>
              <el-col :span="16">
                <span class="value-text"
                      v-if="infoform.autoSyncMode == 2">
                  目标端建表并同步数据
                </span>
                <span class="value-text"
                      v-if="infoform.autoSyncMode == 1">
                  目标端只创建物理表
                </span>
                <span class="value-text"
                      v-if="infoform.autoSyncMode == 0">
                  目标端只同步表里数据
                </span>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">建表字段自增</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==0 ">{{ infoform.targetAutoIncrement }}
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">表名转换方法</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==0 ">
                  <span v-if="infoform.tableNameCase == 'NONE'">
                    无转换
                  </span>
                  <span v-if="infoform.tableNameCase == 'UPPER'">
                    转大写
                  </span>
                  <span v-if="infoform.tableNameCase == 'LOWER'">
                    转小写
                  </span>
                  <span v-if="infoform.tableNameCase == 'CAMEL'">
                    下划线转驼峰
                  </span>
                  <span v-if="infoform.tableNameCase == 'SNAKE'">
                    驼峰转下换线
                  </span>
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">列名转换方法</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==0 ">
                  <span v-if="infoform.columnNameCase == 'NONE'">
                    无转换
                  </span>
                  <span v-if="infoform.columnNameCase == 'UPPER'">
                    转大写
                  </span>
                  <span v-if="infoform.columnNameCase == 'LOWER'">
                    转小写
                  </span>
                  <span v-if="infoform.columnNameCase == 'CAMEL'">
                    下划线转驼峰
                  </span>
                  <span v-if="infoform.columnNameCase == 'SNAKE'">
                    驼峰转下换线
                  </span>
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">数据批次大小</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==1 ">{{ infoform.batchSize }}
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">通道队列大小</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==1 ">{{ infoform.channelSize }}
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">同步操作方法</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if="infoform.autoSyncMode!==1 ">{{ infoform.targetSyncOption }}
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">同步前置执行SQL脚本</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==1 ">
                  <span v-show="!infoform.targetBeforeSqlScripts || infoform.targetBeforeSqlScripts.length==0">[SQL脚本内容为空]</span>
                  <span v-show="infoform.targetBeforeSqlScripts && infoform.targetBeforeSqlScripts.length>0">{{
                          infoform.targetBeforeSqlScripts
                        }}</span>
                </label>
              </el-col>
            </el-row>
            <el-row class="row-gutter">
              <el-col :span="8">
                <label class="key-text">同步后置执行SQL脚本</label>
              </el-col>
              <el-col :span="16">
                <label class="value-text"
                       v-if=" infoform.autoSyncMode!==1 ">
                  <span v-show="!infoform.targetAfterSqlScripts || infoform.targetAfterSqlScripts.length==0">[SQL脚本内容为空]</span>
                  <span v-show="infoform.targetAfterSqlScripts && infoform.targetAfterSqlScripts.length>0">{{
                          infoform.targetAfterSqlScripts
                        }}</span>
                </label>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
      <div class="mapper">
        <div class="table-left">
          <div>表名映射规则</div>
          <div>
            <el-table :data="infoform.tableNameMapper"
                      style="width: 100%"
                      :row-class-name="tableRowClassName">
              <el-table-column prop="fromPattern"
                               label="表名匹配的正则名">
              </el-table-column>
              <el-table-column prop="toValue"
                               label="替换的目标值">
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div class="table-right">
          <div>字段名映射规则</div>
          <div>
            <el-table :data="infoform.columnNameMapper"
                      style="width: 100%"
                      :row-class-name="tableRowClassName">
              <el-table-column prop="fromPattern"
                               label="表名匹配的正则名">
              </el-table-column>
              <el-table-column prop="toValue"
                               label="替换的目标值">
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    infoform: {
      type: Object,
      default: {
        id: 0,
        name: "--",
        description: "--",
        scheduleMode: "MANUAL",
        cronExpression: "",
        sourceConnectionId: 0,
        sourceTypeName: 'MySQL',
        sourceSchema: "",
        runStatus: "",
        tableType: "TABLE",
        includeOrExclude: "",
        sourceTables: [],
        incrTableColumns: [],
        sourceBeforeSqlScripts: "",
        sourceAfterSqlScripts: "",
        tableNameMapper: [],
        columnNameMapper: [],
        tableNameCase: 'NONE',
        columnNameCase: 'NONE',
        targetConnectionId: 0,
        targetTypeName: 'MySQL',
        targetDropTable: true,
        targetOnlyCreate: false,
        autoSyncMode: 2,
        targetSchema: "",
        batchSize: 5000,
        channelSize: 100,
        targetSyncOption: 'INSERT_UPDATE_DELETE',
        targetBeforeSqlScripts: '',
        targetAfterSqlScripts: '',
      }
    }
  },
  data () {
    return {
    }
  },
  methods: {
    tableRowClassName ({ row, rowIndex }) {
      if (rowIndex === 1) {
        return 'warning-row';
      } else if (rowIndex === 3) {
        return 'success-row';
      }
      return '';
    }
  }
}
</script>

<style scoped>
.box-card {
  width: 100%;
}

.row-gutter {
  margin-bottom: 12px;
}

.common-box {
  margin-top: 16px;
  width: 100%;
  display: flex;
  flex-direction: column;
  row-gap: 16px;
  padding-bottom: 24px;
}

.common-box .datainfo {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  gap: 24px;
}

.common-box .datainfo .source {
  flex: 1;
  border-radius: 4px;
}

.common-box .datainfo .target {
  flex: 1;
  border-radius: 4px;
}

.common-box .datainfo .head {
  display: flex;
  height: 72px;
  align-items: center;
  background-color: #f7fbff;
  width: 100%;
  justify-content: space-between;
}

.common-box .datainfo .body {
  background-color: white;
  padding: 24px;
  min-height: 328px;
}

.common-box .datainfo .head .head-img {
  width: 40px;
  padding-left: 24px;
}

.common-box .datainfo .head .head-text {
  flex-grow: 1;
  padding-left: 48px;
}

.common-box .datainfo .head .head-text .title {
  font-size: 14px;
  color: #0051ff;
  font-weight: bold;
}

.common-box .datainfo .head .head-text .sub-title {
  margin-top: 4px;
  font-size: 12px;
  color: #7d7d7d;
}

.mapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  gap: 24px;
}

.mapper .table-left {
  background-color: white;
  width: 100%;
  padding: 24px;
}

.mapper .table-right {
  background-color: white;
  width: 100%;
  padding: 24px;
}

.table-name-list ul {
  padding: 0px 0px;
  white-space: nowrop;
}

.table-name-list ul li {
  padding: 2px 2px;
  border-bottom: 1px solid #e0e0e0;
}

/deep/.el-table .el-table__cell {
  padding: 0px;
}
</style>