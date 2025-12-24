<template>
  <el-card>
    <div style="margin-top: 15px">
      <commonInfo :infoform="infoform"></commonInfo>
      <el-button type="primary"
                 size="mini"
                 icon="el-icon-arrow-left"
                 @click="handleGoBack"
                 style="margin: 12px 0px 20px;float: right">
        返回
      </el-button>
    </div>
  </el-card>
</template>

<script>
import commonInfo from '@/views/task/common/info'

export default {
  components: { commonInfo },
  data () {
    return {
      infoform: {
        id: 0,
        name: "",
        description: "",
        scheduleMode: "MANUAL",
        cronExpression: "",
        sourceConnectionId: 0,
        sourceTypeName: 'MySQL',
        sourceSchema: "",
        runStatus: "",
        tableType: "TABLE",
        includeOrExclude: "",
        sourceTables: [],
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
        beforeSqlScripts: '',
        afterSqlScripts: '',
      },
    }
  },
  methods: {
    loadAssignmentDetail: function () {
      this.$http.get(
        "/dbswitch/admin/api/v1/assignment/detail/id/" + this.$route.query.id
      ).then(res => {
        if (0 === res.data.code) {
          let detail = res.data.data;
          let varAutoSyncMode = 2;
          if (detail.configuration.targetDropTable && detail.configuration.targetOnlyCreate) {
            varAutoSyncMode = 1;
          } else if (!detail.configuration.targetDropTable && !detail.configuration.targetOnlyCreate) {
            varAutoSyncMode = 0;
          } else {
            varAutoSyncMode = 2;
          }
          this.infoform = {
            id: detail.id,
            name: detail.name,
            description: detail.description,
            scheduleMode: detail.scheduleMode,
            cronExpression: detail.cronExpression,
            sourceConnectionId: detail.configuration.sourceConnectionId,
            sourceConnectionName: detail.configuration.sourceConnectionName,
            sourceTypeName: detail.configuration.sourceTypeName,
            sourceSchema: detail.configuration.sourceSchema,
            tableType: detail.configuration.tableType,
            includeOrExclude: detail.configuration.includeOrExclude,
            sourceTables: detail.configuration.sourceTables,
            incrTableColumns: detail.configuration.incrTableColumns,
            sourceBeforeSqlScripts: detail.configuration.sourceBeforeSqlScripts,
            sourceAfterSqlScripts: detail.configuration.sourceAfterSqlScripts,
            tableNameMapper: detail.configuration.tableNameMapper,
            columnNameMapper: detail.configuration.columnNameMapper,
            tableNameCase: detail.configuration.tableNameCase,
            columnNameCase: detail.configuration.columnNameCase,
            targetConnectionId: detail.configuration.targetConnectionId,
            targetConnectionName: detail.configuration.targetConnectionName,
            targetTypeName: detail.configuration.targetTypeName,
            targetDropTable: detail.configuration.targetDropTable,
            targetOnlyCreate: detail.configuration.targetOnlyCreate,
            targetAutoIncrement: detail.configuration.targetAutoIncrement,
            autoSyncMode: varAutoSyncMode,
            targetSchema: detail.configuration.targetSchema,
            batchSize: detail.configuration.batchSize,
            channelSize: detail.configuration.channelSize,
            targetSyncOption: detail.configuration.targetSyncOption,
            targetBeforeSqlScripts: detail.configuration.targetBeforeSqlScripts,
            targetAfterSqlScripts: detail.configuration.targetAfterSqlScripts,
          }
        } else {
          if (res.data.message) {
            alert("查询任务失败," + res.data.message);
          }
        }
      });
    },
    handleGoBack () {
      this.$router.go(-1);
    }
  },
  created () {
    this.loadAssignmentDetail();
  },

}
</script>

<style scoped>
.el-card {
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>