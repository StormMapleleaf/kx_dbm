<template>
  <div>
    <el-card>
      <el-tabs v-model="tabActiveTabName"
               type="border-card">
        <el-tab-pane label="元数据"
                     name="metadata">
          <div class="flex-between">
            <el-aside>
              <div class="select-datasource-container">
                <el-select placeholder="请选择数据源"
                           v-model="dataSourceId"
                           @change="loadTreeData">
                  <el-option v-for="(item,index) in connectionList"
                             :key="index"
                             :label="`[${item.id}] ${item.name}`"
                             :value="item.id"></el-option>
                </el-select>
                <el-button type="primary"
                           size="mini"
                           :disabled="metadataLoading"
                           icon="el-icon-refresh"
                           @click="loadTreeData">刷新</el-button>
              </div>
              <el-scrollbar style="height: 680px;">
                <el-tree ref="metadataTree"
                         empty-text="请选择数据源后查看"
                         :indent=6
                         :data="treeData"
                         :props="props"
                         :load="loadTreeNode"
                         :expand-on-click-node="true"
                         :highlight-current="true"
                         :render-content="renderContent"
                         @node-click="handleNodeClick"
                         lazy>
                </el-tree>
              </el-scrollbar>
            </el-aside>
            <el-main class="metadata-container">
              当前表：<el-tag size="medium">{{currentNode.schemaName}} / {{currentNode.tableName}}</el-tag>
              <el-tabs v-model="metadataActiveTabName"
                       type="border-card">
                <el-tab-pane label="基本信息"
                             name="first">
                  <el-descriptions size="small"
                                   :column="2"
                                   colon
                                   border>
                    <el-descriptions-item label="模式名"><el-tag size="small">{{tableMeta.schemaName}}</el-tag></el-descriptions-item>
                    <el-descriptions-item label="表名称"><el-tag size="small">{{tableMeta.tableName}}</el-tag></el-descriptions-item>
                    <el-descriptions-item label="表类型"><el-tag size="small">{{tableMeta.type}}</el-tag></el-descriptions-item>
                    <el-descriptions-item label="表注释"><span class="long-text">{{tableMeta.remarks}}</span></el-descriptions-item>
                    <el-descriptions-item label="建表DDL">
                    </el-descriptions-item>
                  </el-descriptions>
                  <ace ref="ddlEditor"
                       :value="tableMeta.createSql"
                       @init="initEditor"
                       lang="sql"
                       height="500"
                       theme="chrome"
                       :options="editorOption"
                       width="100%">
                  </ace>
                </el-tab-pane>
                <el-tab-pane label="字段信息"
                             name="seconde">
                  <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                            :data="tableMeta.columns"
                            size="small"
                            border
                            style="width: 100%">
                    <template slot="empty">
                      <span>单击点击左侧节点查看对应表的字段信息</span>
                    </template>
                    <el-table-column prop="fieldName"
                                     min-width="20%"
                                     show-overflow-tooltip
                                     label="名称">
                    </el-table-column>
                    <el-table-column prop="typeName"
                                     min-width="20%"
                                     label="数据类型">
                    </el-table-column>
                    <el-table-column prop="fieldType"
                                     min-width="7%"
                                     label="类型枚举">
                    </el-table-column>
                    <el-table-column prop="displaySize"
                                     min-width="7%"
                                     label="长度">
                    </el-table-column>
                    <el-table-column prop="precision"
                                     min-width="5%"
                                     label="精度">
                    </el-table-column>
                    <el-table-column prop="scale"
                                     min-width="5%"
                                     label="位数">
                    </el-table-column>
                    <el-table-column prop="isPrimaryKey"
                                     min-width="5%"
                                     label="主键">
                    </el-table-column>
                    <el-table-column prop="isAutoIncrement"
                                     min-width="5%"
                                     label="自增">
                    </el-table-column>
                    <el-table-column prop="isNullable"
                                     min-width="5%"
                                     label="可空">
                    </el-table-column>
                    <el-table-column prop="remarks"
                                     min-width="20%"
                                     show-overflow-tooltip
                                     label="注释">
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="索引信息"
                             name="third">
                  <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                            :data="tableMeta.indexes"
                            size="small"
                            border
                            style="width: 100%">
                    <template slot="empty">
                      <span>单击点击左侧节点查看对应表的索引信息</span>
                    </template>
                    <el-table-column prop="indexType"
                                     min-width="20%"
                                     label="索引类型">
                    </el-table-column>
                    <el-table-column prop="indexName"
                                     min-width="20%"
                                     label="索引名称">
                    </el-table-column>
                    <el-table-column prop="indexFields"
                                     :formatter="formatIndexFields"
                                     show-overflow-tooltip
                                     min-width="60%"
                                     label="索引字段">
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane class="table-container-data-table"
                             label="取样数据"
                             name="fourth">
                  <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                            :data="sampleData.rows"
                            border>
                    <template slot="empty">
                      <span>单击点击左侧节点查看对应表的取样数据</span>
                    </template>
                    <el-table-column v-for="(item,index) in sampleData.columns"
                                     :prop="item"
                                     :label="item"
                                     :key="index"
                                     show-overflow-tooltip>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </el-main>
          </div>
        </el-tab-pane>
        <el-tab-pane label="SQL在线"
                     name="sqlQuery">
          <el-row :gutter=12
                  class="padding-row-stype">
            <el-col :span="6">
              <div class="sqlonline-select-suffix">
                <span class="text-label">数据源：</span>
                <el-select size="small"
                           placeholder="请选择数据源"
                           v-model="sqlDataSourceId">
                  <template v-for="item in connectionList">
                    <el-option v-if="item.useSql"
                               :key="item.id"
                               :label="`[${item.id}]${item.name}`"
                               :value="item.id"></el-option>
                  </template>
                </el-select>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="sqlonline-select-suffix">
                <span class="text-label">最大记录数：</span>
                <el-select size="small"
                           placeholder="选择结果集MaxRow"
                           v-model="rsMaxRowCount">
                  <el-option v-for="(item,index) in maxRowCountList"
                             :key="index"
                             :label="item.name"
                             :value="item.id"></el-option>
                </el-select>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="sqlonline-select-suffix">
                <span class="text-label">编辑器高度：</span>
                <el-input-number v-model="editorHeightNum"
                                 size="small"
                                 :step="10"
                                 step-strictly></el-input-number>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="tool">
                <div class="item-button"
                     @click="runAll"><i class="el-icon-video-play"></i><span>执行</span></div>
                <div class="item-button"
                     @click="runSelected"><i class="el-icon-caret-right"></i><span>选中执行</span></div>
                <div class="item-button"
                     @click="formatSql"><i class="el-icon-postcard"></i><span>格式化</span></div>
              </div>
            </el-col>
          </el-row>
          <el-row class="padding-row-stype">
          </el-row>
          <el-row class="padding-row-stype">
            <div v-loading="sqlResultLoading"
                 :style="'height: ' + editorHeightNum + 'px'">
              <ace ref="sqlEditor"
                   @init="initEditor"
                   lang="sql"
                   width="100%"
                   height="100%"
                   theme="monokai"
                   :options="sqlEditorOption">
              </ace>
            </div>
          </el-row>
          <el-row class="padding-row-stype">
            <el-tabs v-model="activeResultTab"
                     tab-position="top"
                     type="border-card">
              <el-tab-pane label="信息"
                           name="0">
                <div v-for="(one,idx) in sqlExecuteResult.summaries"
                     :key="idx">
                  [SQL]: {{one.sql}}<br />{{one.summary}}<br /><br />
                </div>
              </el-tab-pane>
              <el-tab-pane v-for="(one,idx) in sqlExecuteResult.results"
                           :key="(idx+1)"
                           :label="'结果'+(idx+1)"
                           :name="''+(idx+1)">
                <el-table :header-cell-style="{background:'#eef1f6',color:'#606266','font-size': '12px'}"
                          style="width: 100%; max-height: 400px; overflow: auto;"
                          height="400px"
                          :data="one.rows"
                          border>
                  <template slot="empty">
                    <span>SQL结果为空</span>
                  </template>
                  <el-table-column v-for="(item,index) in one.columns"
                                   :prop="item.columnName"
                                   :key="index"
                                   show-overflow-tooltip>
                    <template slot="header">
                      {{item.columnName}}<br />({{item.columnType}})
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import urlencode from "urlencode";
import ace from 'vue2-ace-editor'
const sqlformatter = require("sql-formatter");

export default {
  components: {
    ace
  },
  data () {
    return {
      metadataLoading: false,
      props: {
        label: 'label',
        children: 'children',
        disabled: false,
        isLeaf: false
      },
      editorOption: {
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true,
        showPrintMargin: false,
        fontSize: 16,
        readOnly: true
      },
      dataSourceId: null,
      connectionList: [],
      treeData: [],
      currentNode: {
        tableName: '-',
        schemaName: '-'
      },
      tabActiveTabName: 'metadata',
      metadataActiveTabName: 'first',
      tableMeta: {
        tableName: '-',
        schemaName: '-',
        remarks: '',
        type: '-',
        createSql: "",
        primaryKeys: [],
        columns: []
      },
      count: 1,
      sampleData: [],
      sqlDataSourceId: null,
      sqlEditorOption: {
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true,
        showPrintMargin: false,
        showLineNumbers: true,
        tabSize: 6,
        fontSize: 18,
      },
      rsMaxRowCount: 200,
      maxRowCountList: [
        { id: 100, name: '100条' },
        { id: 200, name: '200条' },
        { id: 500, name: '500条' },
        { id: 1000, name: '1000条' },
        { id: 2000, name: '2000条' },
        { id: 5000, name: '5000条' },
        { id: 10000, name: '10000条' },
      ],
      editorHeightNum: 200,
      sqlResultLoading: false,
      sqlExecuteResult: {
        summaries: [],
        results: [],
      },
      activeResultTab: "0",
    };
  },
  methods: {
    initEditor (editor) {
      require('brace/ext/language_tools')
      // 设置语言
      require('brace/mode/sql')
      require('brace/snippets/sql')
      // 设置主题 按需加载
      require('brace/theme/monokai')
      require('brace/theme/chrome')
      require('brace/theme/crimson_editor')
    },
    loadConnections: function () {
      this.connectionList = [];
      this.$http({
        method: "GET",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/connection/list/name",
      }).then(
        res => {
          if (0 === res.data.code) {
            this.connectionList = res.data.data;
          } else {
            if (res.data.message) {
              alert("加载数据失败:" + res.data.message);
              this.connectionList = [];
            }
          }
        }
      );
    },
    loadTreeData: function () {
      if (this.dataSourceId && this.dataSourceId > 0) {
        this.treeData = []
        this.metadataLoading = true;
        setTimeout(() => {
          this.$http({
            method: "GET",
            url: "/dbswitch/admin/api/v1/connection/schemas/get/" + this.dataSourceId
          }).then(
            res => {
              this.metadataLoading = false;
              if (0 === res.data.code) {
                for (let element of res.data.data) {
                  let obj = new Object();
                  obj['dataSourceId'] = this.dataSourceId;
                  obj['label'] = element;
                  obj['parent'] = this.dataSourceId;
                  obj['value'] = element;
                  obj['hasChild'] = true;
                  obj['type'] = 'DATABASE';
                  this.treeData.push(obj);
                }
              } else {
                alert("加载失败，原因：" + res.data.message);
              }
            }
          );
        }, 500);
      }
    },
    loadTreeNode: function (node, resolve) {
      setTimeout(() => {
        if (node.level === 1) {
          let tableView = [
            {
              'dataSourceId': this.dataSourceId,
              'label': '表',
              'parent': this.dataSourceId,
              'value': node.label,
              'hasChild': true,
              'type': 'TABLE',
            },
            {
              'dataSourceId': this.dataSourceId,
              'label': '视图',
              'parent': this.dataSourceId,
              'value': node.label,
              'hasChild': true,
              'type': 'VIEW',
            }
          ]
          resolve(tableView);
        } else if (node.level === 2) {
          this.loadTablesList(resolve, this.dataSourceId, node.data.value, node.data.type)
        } else {
          resolve([]);
        }
      }, 500);
    },
    loadTablesList: function (resolve, dataSourceId, schema, type) {
      var tableType = 'VIEW' === type ? 'views' : 'tables'
      this.metadataLoading = true;
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/connection/" + tableType + "/get/" + dataSourceId + "?schema=" + urlencode(schema)
      }).then(
        res => {
          this.metadataLoading = false;
          if (0 === res.data.code) {
            let tableList = []
            for (let element of res.data.data) {
              let obj = new Object();
              obj['dataSourceId'] = dataSourceId;
              obj['label'] = element;
              obj['parent'] = dataSourceId;
              obj['value'] = schema;
              obj['hasChild'] = false;
              obj['type'] = type;
              tableList.push(obj);
            }
            return resolve(tableList);
          } else {
            this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
          }
        }
      );
    },
    renderContent (h, { node, data, store }) {
      // https://www.cnblogs.com/zhoushuang0426/p/11059989.html
      if (node.level === 1) {
        return (
          <div class="custom-tree-node">
            <i class="iconfont icon-shujuku1"></i>
            <el-tooltip class="item" effect="light" placement="left">
              <div slot="content">{node.label}</div>
              <span> {data.label}</span>
            </el-tooltip>
          </div>
        );
      } else if (node.level === 2) {
        // var icon_pic = "iconfont icon-shitu_biaoge";
        var icon_pic = <img style="width:16px" src="data:image/image/png;base64,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" />
        if (data.type === 'VIEW') {
          icon_pic = <i class='iconfont icon-viewList'></i>
        }
        return (
          <div class="custom-tree-node">
            {icon_pic}
            <span> {data.label}</span>
          </div>
        );
      } else if (node.level === 3) {
        var icon_pic = <i class='iconfont icon-shitu_biaoge'></i>;
        if (data.type === 'VIEW') {
          icon_pic = <img style="width:16px" src="data:image/png;base64,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" />
        }
        return (
          <div class="custom-tree-node">
            {icon_pic}
            <el-tooltip class="item" effect="light" placement="left">
              <div slot="content">{node.label}</div>
              <span> {data.label}</span>
            </el-tooltip>
          </div>
        );
      } else {
        return (
          <div class="custom-tree-node">
            <i class="el-icon-set-up"></i>
            <el-tooltip class="item" effect="light" placement="left">
              <div slot="content">{data.type}</div>
              <span>{data.label}({data.type})</span>
            </el-tooltip>
          </div>
        );
      }
    },
    formatIndexFields (row, column) {
      let list = row.indexFields;
      let fields = list.map(
        item => {
          if (item.ascOrder === null) {
            return item.fieldName;
          } else if (item.ascOrder) {
            return item.fieldName + " ASC";
          } else {
            return item.fieldName + " DESC";
          }
        })
      return fields.join(";");
    },
    handleNodeClick (data) {
      var type = data.type;
      if (type === 'VIEW' || type === 'TABLE') {
        var datasourceId = data.dataSourceId;
        var schema = data.value;
        var table = data.label;
        if (!data.hasChild && datasourceId && schema && table) {
          this.tabActiveTabName = 'metadata';
          this.metadataActiveTabName = 'first';
          this.clearDataSet();
          this.getTableMeta(datasourceId, schema, table);
          this.getTableData(datasourceId, schema, table);
        }
      }
    },
    clearDataSet () {
      this.tableData = [];
      this.tableMeta = {
        tableName: '-',
        schemaName: '-',
        remarks: '',
        type: '-',
        createSql: "",
        primaryKeys: [],
        columns: []
      };
      this.sampleData = []
    },
    getTableMeta (id, schema, table) {
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/metadata/meta/table/" + id + "?schema=" + urlencode(schema) + "&table=" + urlencode(table)
      }).then(
        res => {
          if (0 === res.data.code) {
            //console.log("list4:" + JSON.stringify(res.data.data))
            this.tableMeta = res.data.data;
            this.currentNode.tableName = table;
            this.currentNode.schemaName = schema;
          } else {
            this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
          }
        }
      );
    },
    async getTableData (id, schema, table) {
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/metadata/data/table/" + id + "?schema=" + urlencode(schema) + "&table=" + urlencode(table)
      }).then(
        res => {
          if (0 === res.data.code) {
            this.sampleData = res.data.data;
            //console.log(this.sampleData)
          } else {
            this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
          }
        }
      );
    },
    loadColumnList (resolve, id, schema, table) {
      //console.log("id=" + id);
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/metadata/columns/" + id + "/1/0?schema=" + urlencode(schema) + "&table=" + urlencode(table)
      }).then(
        res => {
          if (0 === res.data.code) {
            //console.log("list3:" + JSON.stringify(res.data.data))
            res.data.data.forEach(function (element) {
              element['label'] = element.fieldName.name;
              element['parent'] = schema;
              element['value'] = element.fieldName.value;
              element['hasChild'] = false;
              element['children'] = 'child';
            });
            this.tableData = res.data.data;
            return resolve([]);
          } else {
            this.$alert("加载失败，原因：" + res.data.message, '数据加载失败');
            this.clearDataSet();
          }
        }
      );
    },
    formatSql: function () {
      if (this.sqlResultLoading === true) {
        return
      }
      let editor = this.$refs.sqlEditor.editor;
      let sqlcontent = editor.getSelectedText();
      if (sqlcontent && sqlcontent.length > 0) {
        let formatSql = sqlformatter.format(sqlcontent)
        editor.session.replace(editor.getSelectionRange(), formatSql);
      } else {
        sqlcontent = editor.getValue();
        if (!sqlcontent || sqlcontent.length === 0) {
          alert("SQL文本内容为空");
          return
        }
        editor.setValue(sqlformatter.format(editor.getValue()));
      }
    },
    runAll: function () {
      let editor = this.$refs.sqlEditor.editor;
      let sqlcontent = editor.getValue();
      if (!sqlcontent || 0 === sqlcontent.length) {
        alert("SQL文本内容为空");
        return
      }
      this.executeSqlScript(sqlcontent)
    },
    runSelected: function () {
      let editor = this.$refs.sqlEditor.editor;
      let sqlcontent = editor.getSelectedText();
      if (!sqlcontent || 0 === sqlcontent.length) {
        alert("请首先选择SQL文本内容");
        return
      }
      this.executeSqlScript(sqlcontent)
    },
    executeSqlScript: function (sqlScript) {
      if (this.sqlResultLoading === true) {
        alert("已有一个查询正在进行中");
        return
      }
      if (!this.sqlDataSourceId || this.dataSourceId < 0) {
        alert("请首先选择一个数据源来");
        return
      }
      this.sqlResultLoading = true;
      this.sqlExecuteResult = {
        summaries: [],
        results: [],
      };
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/metadata/data/sql/" + this.sqlDataSourceId,
        data: JSON.stringify({
          script: sqlScript,
          page: 1,
          size: this.rsMaxRowCount
        })
      }).then(res => {
        this.sqlResultLoading = false;
        if (0 === res.data.code) {
          this.sqlExecuteResult = res.data.data;
          this.activeResultTab = "0";
        } else {
          alert("SQL执行报错:" + res.data.message);
        }
      }
      );
    },
  },
  created () {
    this.loadConnections();
    this.loadTreeData();
  },
}
</script>

<style scoped>
.el-card {
  width: 100%;
  height: 100%;
  min-height: 200px;
}

.el-message {
  width: 100%;
  height: 100%;
  overflow: auto;
}
.flex-between {
  display: flex;
}
.tree-container {
  min-width: calc(25%);
  position: relative;
  cursor: default;
  color: black;
  font-size: 14px;
  background-size: 16px;
}
.custom-tree-node {
  font-size: 8px;
  background-size: 16px;
}
/deep/ .el-scrollbar {
  //height: 100%;
  //overflow-x: auto;
  .el-scrollbar__wrap {
    overflow-x: hidden;
  }
  .el-scrollbar__bar{
    opacity: 1;
  }
  .el-scrollbar__thumb{
    background: #dcdfe6;
  }
}
.el-select {
  display: inline;
}
.tree-container .tree {
  overflow: auto;
}
.metadata-container {
  padding: 4px;
}
.table-container {
  width: 100%;
  border: darkblue;
}
.table-container-data-table {
  height: 90%;
  overflow-y: auto;
  overflow-x: hidden;
}
.long-text {
  display: -webkit-box;
  width: 300px; /* 定义长文本的显示宽度 */
  white-space: normal !important;
  overflow: hidden; /* 超出部分隐藏 */
  text-overflow: ellipsis; /* 显示省略号 */
  text-align: left;
}
el-tabs--border-card > .el-tabs__header .el-tabs__item {
  margin-left: 8px;
  border: none;
  border-radius: 8px 8px 0 0;
  background-color: #f3f7fe;
  padding: 4px 20px;
  color: #0065d5;
  line-height: 22px;
  height: 30px;
}
.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active {
  background-color: #0065d5;
  color: #ffffff;
}
.sqlonline-select-suffix {
  display: flex;
  justify-content: flex-start;
  align-items: center; /* 垂直居中 */
}
.text-label {
  font-size: 11px;
  font-weight: 700;
}
.select-datasource-container {
  display: flex;
}
.tool {
  display: flex;
  justify-content: flex-end;
}
.tool .item-button {
  display: inline-block;
  font-size: 20px;
  color: #009966;
  margin: 0 20px 0 0;
  line-height: 26px;
  cursor: pointer;
}
.tool .item-button span {
  color: #000;
  font-size: 16px;
}
.padding-row-stype {
  padding: 5px;
}
</style>
