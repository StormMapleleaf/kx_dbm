<template>
  <div>
    <el-card>
      <el-row>
        <el-button size="mini"
                   icon="el-icon-switch-button"
                   :disabled=isSelected
                   plain
                   @click="batchStart()">启动
        </el-button>
        <el-button size="mini"
                   icon="el-icon-video-pause"
                   :disabled=isSelected
                   plain
                   @click="batchStop()">停用</el-button>
        <el-button size="mini"
                   :disabled=isSelected
                   plain
                   @click="batchExport()">导出</el-button>
        <el-button class="right-add-button-group"
                   type="primary"
                   size="mini"
                   icon="el-icon-document-add"
                   @click="handleCreate">创建任务
        </el-button>
      </el-row>
      <div class="assignment-list-top">
        <div class="left-search-input-group">
          <div class="left-search-input">
            <el-input size="mini"
                      placeholder="请输入任务名称关键字搜索"
                      v-model="keyword"
                      :clearable=true
                      @change="searchByKeyword"
                      style="width:100%">
              <el-button @click="searchByKeyword"
                         slot="append"
                         icon="el-icon-search"></el-button>
            </el-input>
          </div>
        </div>
      </div>

      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="tableData"
                size="small"
                border
                @selection-change="handleSelectionChange">
        <el-table-column prop="id"
                         label="编号"
                         type="selection"
                         min-width="6%"></el-table-column>
        <el-table-column label="任务名称"
                         :show-overflow-tooltip="true"
                         min-width="15%">
          <template slot-scope="scope">
            <span @click="handleDetail(scope.$index, scope.row)"
                  class="task-name">{{scope.row.name}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scheduleMode"
                         label="模式"
                         sortable
                         :formatter="stringFormatSchedule"
                         min-width="8%"></el-table-column>
        <el-table-column label="源端"
                         min-width="10%">
          <template slot-scope="scope">
            <el-popover trigger="hover"
                        placement="top">
              <p>源端数据源类型：{{ scope.row.sourceType }}</p>
              <p>源端模式名：{{ scope.row.sourceSchema }}</p>
              <div slot="reference"
                   class="name-wrapper">
                <el-tag size="medium"
                        class="name-wrapper-tag">{{ scope.row.sourceSchema }}</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="目标端"
                         min-width="10%">
          <template slot-scope="scope">
            <el-popover trigger="hover"
                        placement="top">
              <p>目标端数据源类型：{{ scope.row.targetType }}</p>
              <p>目标端模式名：{{ scope.row.targetSchema }}</p>
              <div slot="reference"
                   class="name-wrapper">
                <el-tag size="medium"
                        class="name-wrapper-tag">{{ scope.row.targetSchema }}</el-tag>
              </div>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column label="运行状态"
                         show-overflow-tooltip
                         sortable
                         min-width="10%">
          <template slot-scope="scope">
            <el-icon class="el-icon-success color-success"
                     v-if="scope.row.runStatus == '执行成功'"></el-icon>
            <el-icon class="el-icon-error color-error"
                     v-if="scope.row.runStatus == '执行异常'"></el-icon>
            <el-icon class="el-icon-remove color-cancel"
                     v-if="scope.row.runStatus == '任务取消'"></el-icon>
            <el-icon class="el-icon-video-play color-running"
                     v-if="scope.row.runStatus == '执行中'"></el-icon>
            <el-icon class="el-icon-loading color-await"
                     v-if="scope.row.runStatus == '待执行'"></el-icon>
            <span>{{ scope.row.runStatus }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="isPublished"
                         label="任务状态"
                         sortable
                         :formatter="boolFormatPublish"
                         :show-overflow-tooltip="true"
                         min-width="10%"></el-table-column>
        <el-table-column prop="scheduleTime"
                         label="调度时间"
                         :formatter="scheduleTimeFormat"
                         min-width="15%"></el-table-column>
        <el-table-column label="操作"
                         min-width="35%">
          <template slot-scope="scope">
            <el-tooltip content="跳转到监控调度"
                        placement="top">
              <el-link class="btn-text"
                       type="primary"
                       @click="schedulingLog(scope.$index, scope.row)">日志</el-link>
            </el-tooltip>
            <label class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     v-if="scope.row.isPublished===false"
                     @click="handlePublish(scope.$index, scope.row)">启动</el-link>
            <el-link class="btn-text"
                     type="primary"
                     v-if="scope.row.isPublished===true"
                     @click="handleRetireTask(scope.$index, scope.row)">停止</el-link>
            <label v-if="scope.row.isPublished===true"
                   class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     v-if="scope.row.isPublished===true"
                     @click="handleRunTask(scope.$index, scope.row)">执行</el-link>
            <label v-if="scope.row.isPublished===false"
                   class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     v-if="scope.row.isPublished===false"
                     @click="handleUpdate(scope.$index, scope.row)">修改</el-link>
            <label class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     @click="handleDetail(scope.$index, scope.row)">详情</el-link>
            <label v-if="scope.row.isPublished===false"
                   class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     v-if="scope.row.isPublished===false"
                     @click="handleDelete(scope.$index, scope.row)">删除</el-link>
          </template>
        </el-table-column>
      </el-table>
      <div class="page"
           align="right">
        <el-pagination @size-change="handleSizeChange"
                       @current-change="handleCurrentChange"
                       :current-page="currentPage"
                       :page-sizes="[5, 10, 20, 40]"
                       :page-size="pageSize"
                       layout="total, sizes, prev, pager, next, jumper"
                       :total="totalCount"></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {

  data () {
    return {
      loading: true,
      currentPage: 1,
      pageSize: 10,
      totalCount: 2,
      keyword: null,
      tableData: [],
      isSelected: true,
      idsSelected: []
    };
  },
  methods: {
    loadData: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/list",
        data: JSON.stringify({
          searchText: this.keyword,
          page: this.currentPage,
          size: this.pageSize
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.currentPage = res.data.pagination.page;
          this.pageSize = res.data.pagination.size;
          this.totalCount = res.data.pagination.total;
          this.tableData = res.data.data;
        } else {
          alert("加载任务列表失败:" + res.data.message);
        }
      },
        function () {
          console.log("load assignments list failed");
        }
      );
    },
    searchByKeyword: function () {
      this.currentPage = 1;
      this.loadData();
    },
    boolFormatPublish (row, column) {
      if (row.isPublished === true) {
        return "启动";
      } else {
        return "停止";
      }
    },
    scheduleTimeFormat (row, column) {
      if (row.scheduleTime == null) {
        return "--";
      }
      return row.scheduleTime;
    },
    stringFormatSchedule (row, column) {
      if (row.scheduleMode == "MANUAL") {
        return "手动";
      } else {
        return "定时";
      }
    },
    handleSelectionChange (val) {
      if (val.length > 0) {
        this.isSelected = false;
        this.idsSelected.push(val.map(item => item.id));
      } else {
        this.isSelected = true;
        this.idsSelected = []
      }
    },
    batchStart () {
      console.log(this.idsSelected)
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/deploy?ids=" + this.idsSelected,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '任务发布成功!',
            type: 'success'
          });
          this.loadData();
        } else {
          if (res.data.message) {
            this.$message.error("任务发布失败," + res.data.message);
          }
        }
      });
    },
    batchStop () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/retire?ids=" + this.idsSelected,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '下线任务成功!',
            type: 'success'
          });
          this.loadData();
        } else {
          if (res.data.message) {
            this.$message.error("下线任务失败," + res.data.message);
          }
        }
      });
    },
    batchExport () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/export?ids=" + this.idsSelected,
        responseType: 'blob',
      }).then(res => {
        if (200 === res.status) {
          this.downloadFile(res)
          this.$message({
            message: '导出任务成功!',
            type: 'success'
          });
          this.loadData();
        } else {
          if (res.data.message) {
            this.$message.error("导出任务失败," + res.data.message);
          }
        }
      });
    },
    downloadFile: function (resp) {
      const headers = resp.headers;
      const contentType = headers['content-type'];
      if (!resp.data) {
        console.error('响应异常：', resp);
        return false;
      } else {
        console.info('下载文件：', resp);
        const blob = new Blob([resp.data], {
          type: contentType
        });

        const contentDisposition = resp.headers['content-disposition'];
        let fileName = 'unknown';
        if (contentDisposition) {
          fileName = window.decodeURI(resp.headers['content-disposition'].split('=')[1]);
        }
        console.log('文件名称：', fileName);
        this.downFile(blob, fileName);
      }
    },
    downFile: function (blob, fileName) {
      if ('download' in document.createElement('a')) {
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob); 
        link.download = fileName; 
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click(); 
        window.URL.revokeObjectURL(link.href); 
        document.body.removeChild(link); 
      } else {
        window.navigator.msSaveBlob(blob, fileName);
      }
    },
    handleCreate: function () {
      this.$router.push('/task/create')
    },
    handleDetail: function (index, row) {
      this.$router.push({ path: '/task/detail', query: { id: row.id } })
    },
    handleUpdate: function (index, row) {
      this.$router.push({ path: '/task/update', query: { id: row.id } })
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "是否确定删除任务【" + row.name + "】?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/dbswitch/admin/api/v1/assignment/delete/" + row.id
        ).then(res => {
          if (0 === res.data.code) {
            this.loadData();
          } else {
            if (res.data.message) {
              alert("删除任务失败:" + res.data.message);
            }
          }
        });
      });
    },
    schedulingLog: function (index, row) {
      this.$router.push({
        path: "/task/schedule?id=" + row.id
      });
    },
    handlePublish: function (index, row) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/deploy?ids=" + row.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '任务发布成功!',
            type: 'success'
          });
          this.loadData();
        } else {
          if (res.data.message) {
            this.$message.error("任务发布失败," + res.data.message);
          }
        }
      });
    },
    handleRunTask: function (index, row) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/run",
        data: JSON.stringify([row.id])
      }).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '手动启动执行任务成功!',
            type: 'success'
          });
          this.loadData();
        } else {
          if (res.data.message) {
            this.$message.error("手动启动执行任务失败," + res.data.message);
          }
        }
      });
    },
    handleRetireTask: function (index, row) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/retire?ids=" + row.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '下线任务成功!',
            type: 'success'
          });
          this.loadData();
        } else {
          if (res.data.message) {
            this.$message.error("下线任务失败," + res.data.message);
          }
        }
      });
    },
    handleSizeChange: function (pageSize) {
      this.loading = true;
      this.pageSize = pageSize;
      this.loadData();
    },

    handleCurrentChange: function (currentPage) {
      this.loading = true;
      this.currentPage = currentPage;
      this.loadData();
    }
  },
  created () {
    this.loadData();
  },
};
</script>

<style scoped>
.el-card,
.el-message {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.el-table {
  width: 100%;
  height: 100%;
}

/deep/ .el-table--border .el-table__cell {
  border-right: 0px solid red !important;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}

.el-input.is-disabled .el-input__inner {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: pointer;
}

.assignment-list-top {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.left-search-input-group {
  width: 100%;
  margin-right: auto;
  display: flex;
  justify-content: space-between;
}

.left-search-input {
  width: 100%;
  margin-right: auto;
  margin: 10px 0px;
}

.right-add-button-group {
  width: 100px;
  margin-left: auto;
/ / margin: 0 px 0 px;
  float: right;
}

.color-await {
  color: #a0a6b8 !important;
}

.color-running {
  color: #6cdbbc !important;
}

.color-error {
  color: #ff9c86 !important;
}

.color-cancel {
  color: #a0a6b8 !important;
}

.color-success {
  color: #6cdbbc !important;
}

.task-name {
  color: #409EFF;
  cursor: pointer;
}

.task-name:hover {
  color: red;
  text-decoration: underline;
}

.btn-style {
  color: #e9e9f3;
}

.btn-text {
  font-size: 12px;
  color: #6873ce;
}

.name-wrapper-tag {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
}
</style>
