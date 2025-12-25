<template>
  <el-card>
    <el-header style="height: 80px">
      <div style="display: inline-block;float: left">
        <el-image style="width: 60px; height: 60px"
                  :src="require('@/assets/icons/' + this.selectedTypeName +'.png')"></el-image>
      </div>
      <h3 style="font-family: 楷体;margin-left: 60px"
          class="h-title">{{ this.selectedTypeName }}</h3>
    </el-header>
    <el-main>
      <el-form ref="dataform"
               :rules="rules"
               :model="dataform"
               label-width="120px"
               label-position="right"
               size="medium"
               status-icon>
        <div class="f1">
          <el-form-item prop="name"
                        label="数据源名称"
                        style="width:40%">
            <el-input v-model="dataform.name"
                      placeholder="请输入数据源名称"
                      :readonly=isReadOnly
                      auto-complete="off"></el-input>
            <label v-if="!isReadOnly"
                   class="tips-style">数据源名称不能包含 &、<、>、"、'、(、) ，长度为1~200字符</label>
          </el-form-item>
          <el-form-item :required=true
                        label="数据库类型">
            <label>{{ this.selectedTypeName }}</label>
          </el-form-item>
          <el-form-item prop="version"
                        label="驱动版本">
            <el-select v-model="dataform.version"
                       :readonly=isReadOnly
                       placeholder="请选择驱动版本">
              <el-option v-for="(item,index) in this.connectionDrivers"
                         :key="index"
                         :label="item.driverVersion"
                         :value="item.driverVersion"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="isShowUrlAndPort()"
                        prop="address"
                        label="连接地址">
            <el-input v-model="dataform.address"
                      auto-complete="off"
                      @blur="changeUrl()"
                      style="width:30%"
                      :readonly=isReadOnly
                      placeholder="请输入地址"></el-input>
            :
            <el-input v-model="dataform.port"
                      auto-complete="off"
                      @blur="changeUrl()"
                      style="width:10%"
                      :readonly=isReadOnly
                      placeholder="端口"></el-input>
          </el-form-item>

          <el-form-item v-if="isShowDatabaseName()"
                        prop="databaseName"
                        label="数据库名"
                        style="width:24%">
            <el-input v-model="dataform.databaseName"
                      auto-complete="off"
                      @blur="changeUrl()"
                      :readonly=isReadOnly
                      placeholder="请输入数据库名"></el-input>
          </el-form-item>

          <el-form-item label="用户名"
                        prop="username"
                        style="width:24%">
            <el-input v-model="dataform.username"
                      :readonly=isReadOnly
                      auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item label="密码"
                        prop="password"
                        style="width:24%">
            <el-input type="password"
                      v-model="dataform.password"
                      :readonly=isReadOnly
                      auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item label="JDBC连接串"
                        label-width="120px"
                        prop="url"
                        style="width:85%">
            <el-input type="textarea"
                      :rows="6"
                      :spellcheck="false"
                      placeholder="请输入"
                      :readonly=isReadOnly
                      v-model="dataform.url"
                      auto-complete="off">
            </el-input>
            <label v-if="!isReadOnly"
                   class="tips-style">JDBC连接串（因数据库连接方式，连接参数差异较大所以需要手动拼接好），以便测试连接。</label>
          </el-form-item>
        </div>
      </el-form>
    </el-main>
    <el-footer>
      <el-row style="text-align: center">
        <el-button type="success"
                   class="startTest"
                   @click="startTest">测试</el-button>
        <el-button v-if="!this.isReadOnly && (!this.selectedId || this.selectedId<=0)"
                   type="primary"
                   class="createDataSource"
                   @click="createDataSource">创建</el-button>
        <el-button v-if="!this.isReadOnly && (this.selectedId && this.selectedId>0)"
                   type="primary"
                   class="updateDataSource"
                   @click="updateDataSource">更新</el-button>
        <el-button class="cancel"
                   @click="cancel">取消</el-button>
      </el-row>
    </el-footer>
  </el-card>
</template>

<script>
export default {
  props: {
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      selectedType: 'MYSQL',
      selectedTypeName: 'MySQL',
      selectedId: 0,
      connectionDrivers: [],
      databaseTypeDetail: {},
      dataform: {
        id: 0,
        diver: "",
        name: "",
        type: "",
        driver: "",
        version: "",
        address: "",
        port: "",
        databaseName: "",
        username: "",
        password: "",
        sample: "",
        url: "",
        templateUrl: "",
      },
      rules: {
        name: [
          {
            required: true,
            message: "数据源名称不能为空",
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
        address: [
          {
            required: true,
            message: "连接地址不能为空",
            trigger: "change"
          }
        ],
        port: [
          {
            required: true,
            message: "连接端口号不能为空",
            trigger: "change"
          }
        ],
        databaseName: [
          {
            required: true,
            message: "数据库名不能为空",
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
            message: "用户名不能为空",
            trigger: "blur"
          }
        ],
        password: [
          {
            required: true,
            message: "密码不能为空",
            trigger: "blur"
          }
        ]
      },
    };
  },

  methods: {
    loadDatabaseTypeDetail: function () {
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/connection/type/" + this.selectedType
      }).then(
        res => {
          if (0 === res.data.code) {
            this.databaseTypeDetail = res.data.data;
            if (!this.selectedId || this.selectedId <= 0) {
              this.selectedTypeName = this.databaseTypeDetail.name;
              this.dataform.driver = this.databaseTypeDetail.driver;
              this.dataform.url = this.databaseTypeDetail.sample;
            }
            this.dataform.templateUrl = this.databaseTypeDetail.url.replace("[\\?{params}]", "");
          } else {
            alert("加载任务列表失败:" + res.data.message);
          }
        }
      );
    },
    loadConnectionDetail: function () {
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/get/" + this.selectedId
      ).then(res => {
        if (0 === res.data.code) {
          var detail = res.data.data;
          this.selectedType = detail.type;
          this.selectedTypeName = detail.typeName;
          this.dataform.id = detail.id;
          this.dataform.name = detail.name;
          this.dataform.type = detail.type;
          this.dataform.version = detail.version;
          this.dataform.driver = detail.driver;
          this.dataform.address = detail.address;
          this.dataform.port = detail.port;
          this.dataform.databaseName = detail.databaseName;
          this.dataform.username = detail.username;
          this.dataform.password = detail.password;
          this.dataform.url = detail.url;

          this.selectedType = detail.type;
          this.loadDatabaseTypeDetail();
          this.selectChangedDriverVersion();
        } else {
          this.$message.error("查询连接详情失败," + res.data.message);
        }
      });
    },
    selectChangedDriverVersion: function () {
      if (!this.selectedType || this.isReadOnly) {
        return;
      }
      this.connectionDrivers = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/" + this.selectedType + "/drivers"
      ).then(res => {
        if (0 === res.data.code) {
          this.connectionDrivers = res.data.data;
        } else {
          this.connectionDrivers = [];
          this.$message.error("查询数据库可用的驱动版本失败," + res.data.message);
        }
      });
    },
    changeUrl: function () {
      var params = this.dataform.url.split("?");
      var tplUrl = this.dataform.templateUrl

      var flag = false
      if (Object.keys(this.dataform.address).length > 0) {
        var address = this.dataform.address
        tplUrl = tplUrl.replaceAll("{host}", address)
        flag = true
      }
      if (Object.keys(this.dataform.port).length > 0) {
        var port = this.dataform.port
        tplUrl = tplUrl.replaceAll("{port}", port)
        flag = true
      }
      if (Object.keys(this.dataform.databaseName).length > 0) {
        var databaseName = this.dataform.databaseName
        tplUrl = tplUrl.replaceAll("{database}", databaseName)
        tplUrl = tplUrl.replaceAll("{file}", databaseName)
        flag = true
      }
      if (flag) {
        if (Object.keys(params).length > 1) {
          this.dataform.url = tplUrl + "?" + params[1]
        } else {
          this.dataform.url = tplUrl
        }
      } else {
        if (Object.keys(params).length > 1) {
          this.dataform.url = this.selectedTypeSample.split("?")[0] + "?" + params[1]
        } else {
          this.dataform.url = this.selectedTypeSample
        }
      }
    },
    isShowDatabaseName: function () {
      var type = this.selectedType
      var flag = true;
      if (type === "ELASTICSEARCH") {
        flag = false
      }
      return flag;
    },
    isShowUrlAndPort: function () {
      var type = this.selectedType
      var flag = true;
      if (type === "SQLITE3") {
        flag = false
      }
      return flag;
    },
    startTest: function () {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/connection/preTest",
        data: JSON.stringify({
          name: this.dataform.name,
          type: this.selectedType,
          version: this.dataform.version,
          driver: this.dataform.driver,
          url: this.dataform.url,
          username: this.dataform.username,
          password: this.dataform.password
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.$message({
            message: '测试连接成功!',
            type: 'success'
          });
        } else {
          this.$message.error('测试连接失败!');
        }
      });
    },
    createDataSource: function () {
      let driverClass = "";
      if (this.connectionDrivers.length > 0) {
        for (let i = 0; i < this.connectionDrivers.length; i++) {
          if (this.connectionDrivers[i].driverVersion == this.dataform.version) {
            driverClass = this.connectionDrivers[i].driverClass;
            break;
          }
        }
      }

      this.$refs['dataform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            url: "/dbswitch/admin/api/v1/connection/create",
            data: JSON.stringify({
              name: this.dataform.name,
              type: this.selectedType,
              version: this.dataform.version,
              driver: this.dataform.driver,
              address: this.dataform.address,
              port: this.dataform.port,
              databaseName: this.dataform.databaseName,
              characterEncoding: "",
              url: this.dataform.url,
              username: this.dataform.username,
              password: this.dataform.password
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.$message({
                message: '添加连接信息成功!',
                type: 'success'
              });
              this.$router.push("/connection/list")
            } else {
              this.$message.error("添加连接信息失败：" + res.data.message);
            }
          });
        } else {
          this.$message.error("请检查输入");
        }
      });
    },
    updateDataSource: function () {
      this.$refs['dataform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            url: "/dbswitch/admin/api/v1/connection/update",
            data: JSON.stringify({
              id: this.dataform.id,
              name: this.dataform.name,
              type: this.dataform.type,
              version: this.dataform.version,
              driver: this.dataform.driver,
              address: this.dataform.address,
              port: this.dataform.port,
              databaseName: this.dataform.databaseName,
              characterEncoding: "",
              url: this.dataform.url,
              username: this.dataform.username,
              password: this.dataform.password
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.$message({
                message: '修改连接信息成功!',
                type: 'success'
              });
              this.$router.push("/connection/list")
            } else {
              this.$message.error("修改连接信息失败：" + res.data.message);
            }
          });
        } else {
          alert("请检查输入");
          this.$message.error("请检查输入");
        }
      });
    },
    cancel: function () {
      this.$router.push("/connection/list")
    },
  },
  created () {
    this.selectedType = this.$route.query.type;
    this.selectedId = this.$route.query.id;
    if (this.selectedId && this.selectedId > 0) {
      this.loadConnectionDetail();
    } else {
      this.loadDatabaseTypeDetail();
      this.selectChangedDriverVersion();
    }
  }
}
</script>

<style scoped>
.el-card {
  border-radius: 4px;
  overflow: visible;
}

.el-header,
.el-main,
.el-footer {
  background-color: white;
}

.h-title {
  font-weight: bolder;
  font-size: 20px;
  margin-left: 20px;
}

.button {
  padding: 0;
  float: right;
}

.tips-style {
  font-size: 10px;
  color: red;
}
</style>