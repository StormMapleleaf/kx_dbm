<template>
  <div>
    <el-card>
      <div class="connection-list-top">
        <div class="left-search-input-group">
          <div class="left-search-input">
            <el-input placeholder="默认按照数据源名称搜索"
                      v-model="keyword"
                      @change="searchByKeyword"
                      :clearable=true
                      style="width:300px">
            </el-input>
          </div>
        </div>
        <div class="right-add-button-group">
          <el-button type="primary"
                     size="mini"
                     icon="el-icon-document-add"
                     @click="selectDataSource">创建数据源</el-button>
        </div>
      </div>

      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="tableData"
                size="small"
                border>
        <el-table-column prop="id"
                         label="序号"
                         min-width="5%"></el-table-column>
        <el-table-column prop="name"
                         label="数据源名称"
                         show-overflow-tooltip
                         min-width="20%"></el-table-column>
        <el-table-column prop="type"
                         label="数据库类型"
                         show-overflow-tooltip
                         min-width="10%"></el-table-column>
        <el-table-column prop="version"
                         label="驱动版本"
                         show-overflow-tooltip
                         min-width="12%"></el-table-column>
        <el-table-column prop="createTime"
                         label="创建时间"
                         min-width="18%"></el-table-column>
        <el-table-column prop="username"
                         label="用户名"
                         show-overflow-tooltip
                         min-width="10%"></el-table-column>
        <el-table-column label="操作"
                         min-width="35%">
          <template slot-scope="scope">
            <el-link class="btn-text"
                     type="primary"
                     @click="handleTest(scope.$index, scope.row)">测试</el-link>
            <label class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     @click="handleUpdate(scope.$index, scope.row)">编辑</el-link>
            <label class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
                     @click="handleDetail(scope.$index, scope.row)">详情</el-link>
            <label class="btn-style">&nbsp;|&nbsp;</label>
            <el-link class="btn-text"
                     type="primary"
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
      keyword: null,
      lists: [],
      currentPage: 1,
      pageSize: 10,
      totalCount: 2,
      databaseType: [],
      connectionDriver: [],
      tableData: [],
      queryForm: {
        title: "",
        type: "",
        url: "",
        diver: "",
        version: "",
        username: "",
        password: ""
      },
      createform: {
        title: "",
        type: "",
        diver: "",
        sample: "",
        url: "",
        version: "",
        username: "",
        password: ""
      },
      updateform: {
        id: 0,
        title: "",
        type: "",
        diver: "",
        version: "",
        username: "",
        password: ""
      },
      rules: {
        name: [
          {
            required: true,
            message: "名称不能为空",
            trigger: "blur"
          }
        ],
        type: [
          {
            required: true,
            message: "数据库类型必须选择",
            trigger: "change"
          }
        ],
        version: [
          {
            required: true,
            message: "驱动版本必须选择",
            trigger: "change"
          }
        ],
        url: [
          {
            required: true,
            message: "Jdbc URL必须提供",
            trigger: "blur"
          }
        ],
        username: [
          {
            required: true,
            message: "连接账号名必须提供",
            trigger: "blur"
          }
        ],
        password: [
          {
            required: true,
            message: "连接密码必须提供",
            trigger: "blur"
          }
        ]
      },
      dialogFormVisible: false,
      createFormVisible: false,
      updateFormVisible: false,
      dataSourceCreateStep1: false,
    }
  },
  methods: {
    loadData: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/connection/list",
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
      });
    },
    searchByKeyword: function () {
      this.currentPage = 1;
      this.loadData();
    },
    loadDatabaseTypes: function () {
      this.databaseType = [];
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/connection/types"
      }).then(
        res => {
          if (0 === res.data.code) {
            this.databaseType = res.data.data;
          } else {
            alert("加载任务列表失败:" + res.data.message);
          }
        },
        function () {
          console.log("failed");
        }
      );
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "此操作将此数据源ID=" + row.id + "删除么, 是否继续?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/dbswitch/admin/api/v1/connection/delete/" + row.id
        ).then(res => {
          if (0 === res.data.code) {
            this.loadData();
          } else {
            alert("删除任务失败:" + res.data.message);
          }
        });
      });
    },
    handleDetail: function (index, row) {
      this.$router.push({ path: "/connection/list/detail", query: { id: row.id } });
    },
    handleTest: function (index, row) {
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/test/" + row.id
      ).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '测试连接成功!',
            type: 'success'
          });
        } else {
          this.$message.error("测试连接失败," + res.data.message);
        }
      });
    },
    selectDataSource: function () {
      this.$router.push('/connection/list/select');

    },
    handleUpdate: function (index, row) {
      this.$router.push({
        path: "/connection/list/update", query: { id: row.id }
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
    },
  },
  created () {
    this.loadDatabaseTypes();
    this.loadData();
  }
};
</script>

<style scoped>
.el-table {
  width: 100%;
  height: 100%;
}

.el-card,
.el-message {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.connection-list-top {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.left-search-input-group {
  width: calc(100% - 100px);
  margin-right: auto;
  display: flex;
  justify-content: space-between;
}

.left-search-input {
  width: 300px;
  margin-right: auto;
  margin: 10px 5px;
}

.right-add-button-group {
  width: 100px;
  margin-left: auto;
  margin: 10px 5px;
}

.btn-style {
  color: #e9e9f3;
}

.btn-text {
  font-size: 12px;
  color: #6873ce;
}
</style>
