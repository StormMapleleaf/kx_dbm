<template>
  <el-card>
    <el-steps :active="active"
              finish-status="success">
      <el-step title="基本信息配置" />
      <el-step title="同步源端配置" />
      <el-step title="目标端配置" />
      <el-step title="映射转换配置" />
      <el-step title="配置确认提交" />
    </el-steps>

    <el-form :model="dataform"
             status-icon
             :rules="rules"
             ref="dataform">

      <div v-show="active == 1"
           class="common-top">
        <el-form-item label="任务名称"
                      label-width="160px"
                      :required=true
                      prop="name"
                      style="width:65%">
          <el-input v-model="dataform.name"
                    auto-complete="off"
                    placeholder="请输入任务名称"
                    style="width:80%"></el-input>
          <label class="tips-style block">请输入任务名称，只能以字母、数字为开头，包含字母、数字和._-，3-100个字符</label>
        </el-form-item>
        <el-form-item label="描述"
                      label-width="160px"
                      prop="description"
                      style="width:65%">
          <el-input v-model="dataform.description"
                    type="textarea"
                    :rows="3"
                    auto-complete="off"
                    placeholder="请输入任务描述"
                    style="width:80%"></el-input>
        </el-form-item>
        <el-form-item label="集成模式"
                      label-width="160px"
                      :required=true
                      prop="scheduleMode"
                      style="width:80%">
          <el-input v-model="dataform.scheduleMode"
                    v-if="false"></el-input>
          <el-radio-group v-model="dataform.scheduleModeName"
                          size="mini"
                          @change="scheduleModeUpdate">
            <el-radio-button value="MANUAL"
                             label="手动调度"></el-radio-button>
            <el-radio-button value="SYSTEM_SCHEDULED"
                             label="系统调度"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行周期"
                      label-width="160px"
                      style="width:80%"
                      :required="true"
                      v-if="dataform.scheduleMode=='SYSTEM_SCHEDULED'">
          <el-select v-model="dataform.cronExpression"
                     filterable
                     allow-create>
            <el-option v-for="(item,index) in cronExprOptionList"
                       :key="index"
                       :label="item.name"
                       :value="item.value"></el-option>
          </el-select>
          <label class="tips-style block">执行周期为CRON表达式，即可以选择以下内置的周期，也可以自行输入或粘贴合法的CRON表达式(最小间隔时间为2分钟)。</label>
        </el-form-item>
      </div>
      <div v-show="active == 2"
           class="common-top">
        <el-form-item label="源端数据源"
                      label-width="160px"
                      :required=true
                      prop="sourceConnectionId"
                      style="width:80%">
          <el-select v-model="dataform.sourceConnectionId"
                     @change="selectChangedSourceConnection"
                     placeholder="请选择">
            <el-option v-for="(item,index) in connectionNameList"
                       :key="index"
                       :label="`[${item.id}] ${item.name}`"
                       :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="源端模式名"
                      label-width="160px"
                      :required=true
                      prop="sourceSchema"
                      style="width:80%">
          <el-select v-model="dataform.sourceSchema"
                     filterable
                     @change="selectCreateChangedSourceSchema"
                     placeholder="请选择">
            <el-option v-for="(item,index) in sourceConnectionSchemas"
                       :key="index"
                       :label="item"
                       :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="源端表类型"
                      label-width="160px"
                      :required=true
                      prop="tableType"
                      style="width:80%">
          <el-select placeholder="请选择表类型"
                     @change="selectCreateChangedTableType"
                     v-model="dataform.tableType">
            <el-option label="物理表"
                       value="TABLE"></el-option>
            <el-option label="视图表"
                       value="VIEW"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="配置方式"
                      label-width="160px"
                      :required=true
                      prop="includeOrExclude"
                      style="width:80%">
          <el-select placeholder="请选择配置方式"
                     v-model="dataform.includeOrExclude">
            <el-option label="包含表"
                       value="INCLUDE"></el-option>
            <el-option label="排除表"
                       value="EXCLUDE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表名配置"
                      label-width="160px"
                      :required=false
                      prop="sourceTables"
                      style="width:80%">
          <el-select placeholder="请选择表名"
                     multiple
                     filterable
                     v-model="dataform.sourceTables">
            <el-option v-for="(item,index) in sourceSchemaTables"
                       :key="index"
                       :label="item"
                       :value="item"></el-option>
          </el-select>
          <label class="tips-style block">当为包含表时，选择所要精确包含的表名，如果不选则代表选择所有；当为排除表时，必须选择要精确排除的表名。</label>
        </el-form-item>
        <el-form-item label="增量同步配置"
                      label-width="160px"
                      :required=false
                      style="width:80%">
          &nbsp;&nbsp;
          <i class="el-icon-plus"
             @click="handleAddInputIncrTable"></i>
          &nbsp;&nbsp;&nbsp;&nbsp;
          <i class="el-icon-question"
             @click="showDataSyncMessageDialogVisible=true"></i>
          <el-table :data="dataform.incrTableColumns"
                    :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                    size="mini"
                    border>
            <el-table-column label="表名"
                             prop="tableName"
                             min-width="45%">
            </el-table-column>
            <el-table-column label="增量字段名"
                             prop="columnName"
                             min-width="45%">
            </el-table-column>
            <el-table-column label="操作"
                             min-width="10%">
              <template slot-scope="scope">
                <el-link icon="el-icon-delete"
                         @click="handleDeleteIncrTable(scope.$index)"></el-link>
              </template>
            </el-table-column>
          </el-table>
          <label class="tips-style block">可点击加号+按钮为需要增量同步的大表配置增量同步的字段来加快数据同步速度</label>
        </el-form-item>
        <el-form-item label="同步前置执行SQL脚本"
                      label-width="160px"
                      prop="sourceBeforeSqlScripts"
                      style="width:80%">
          <el-input v-model="dataform.sourceBeforeSqlScripts"
                    type="textarea"
                    :rows="3"
                    auto-complete="off"
                    style="width: 80%"></el-input>
          <label class="tips-style block">数据同步查询源端数据库前执行的SQL，多个SQL间以英文逗号分隔。</label>
        </el-form-item>
        <el-form-item label="同步后置执行SQL脚本"
                      label-width="160px"
                      prop="sourceAfterSqlScripts"
                      style="width:80%">
          <el-input v-model="dataform.sourceAfterSqlScripts"
                    type="textarea"
                    :rows="3"
                    auto-complete="off"
                    style="width: 80%"></el-input>
          <label class="tips-style block">数据同步查询源端数据库后执行的SQL，多个SQL间以英文逗号分隔。</label>
        </el-form-item>
      </div>
      <div v-show="active == 3"
           class="common-top">
        <el-form-item label="目的端数据源"
                      label-width="160px"
                      :required=true
                      prop="targetConnectionId"
                      style="width:80%">
          <el-select v-model="dataform.targetConnectionId"
                     @change="selectChangedTargetConnection"
                     placeholder="请选择">
            <el-option v-for="(item,index) in connectionNameList"
                       :key="index"
                       :label="`[${item.id}]${item.name}`"
                       :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="目的端模式名"
                      label-width="160px"
                      :required=true
                      prop="targetSchema"
                      style="width:80%">
          <el-select v-model="dataform.targetSchema"
                     filterable
                     placeholder="请选择">
            <el-option v-for="(item,index) in targetConnectionSchemas"
                       :key="index"
                       :label="item"
                       :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="自动同步模式"
                      label-width="160px"
                      :required=true
                      prop="autoSyncMode"
                      style="width:80%">
          <span slot="label">
            <span style="color: red"><strong>自动同步模式</strong> </span>
          </span>
          <el-tooltip placement="top">
            <div slot="content">
              <p>目标端建表并同步数据：首次在目标的自动建表(存在重命表时会删除重建)并执行数据加载同步操作，再次执行时会根据是否有主键进行变化量同步；</p>
              <p>目标端只创建物理表: 每次执行时，只在目标端自动建表，存在重名表时会删除后重建；</p>
              <p>目标端只同步表里数据：每次执行时，目标端需要存在符合映射规则的物理表，最迟需要在执行任务前已经存在目标表；<br />该选项通
                常适用于两端表结构一致时(或目标端字段包含源端所有的字段且字段数据类型一致)的数据同步场景</p>
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.autoSyncMode">
            <el-option label='目标端建表并同步数据'
                       :value=2></el-option>
            <el-option label='目标端只创建物理表'
                       :value=1></el-option>
            <el-option label='目标端只同步表里数据'
                       :value=0></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="建表字段自增"
                      label-width="160px"
                      :required=true
                      v-if=" dataform.autoSyncMode!==0 "
                      prop="targetAutoIncrement"
                      style="width:80%">
          <el-tooltip placement="top">
            <div slot="content">
              创建表时是否自动支持字段的自增；只有使用自动建表才会生效，不过前提需要两端的数据库表建表时支持指定自增字段，默认为false。
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.targetAutoIncrement">
            <el-option label='是'
                       :value=true></el-option>
            <el-option label='否'
                       :value=false></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表名转换方法"
                      label-width="160px"
                      :required=true
                      prop="tableNameCase"
                      style="width:80%">
          <el-tooltip placement="top">
            <div slot="content">
              转换说明：先使用下面的表名映射，然后再使用这里的表名转换方法转换，对支持大小写敏感的数据库类型有效。
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.tableNameCase">
            <el-option v-for="(item,index) in nameConvertList"
                       :key="index"
                       :label="item.name"
                       :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="列名转换方法"
                      label-width="160px"
                      :required=true
                      prop="columnNameCase"
                      style="width:80%">
          <el-tooltip placement="top">
            <div slot="content">
              转换说明：先使用下面的列名映射，然后再使用这里的转换方法转换，对支持大小写敏感的数据库类型有效。
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.columnNameCase">
            <el-option v-for="(item,index) in nameConvertList"
                       :key="index"
                       :label="item.name"
                       :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据批次大小"
                      label-width="160px"
                      :required=true
                      v-if=" dataform.autoSyncMode!==1 "
                      prop="batchSize"
                      style="width:80%">
          <el-tooltip placement="top">
            <div slot="content">
              数据同步时单个批次处理的行记录总数，该值越大越占用内存空间。建议：小字段表设置为10000或20000，大字段表设置为100或500
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.batchSize">
            <el-option v-for="(item,index) in batchSizeList"
                       :key="index"
                       :label="item.toString()"
                       :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通道队列大小"
                      label-width="160px"
                      :required=true
                      v-if=" dataform.autoSyncMode!==1 "
                      prop="channelSize"
                      style="width:80%">
          <el-tooltip placement="top">
            <div slot="content">
              数据同步时缓存数据的通道队列大小，该值越大越占用内存空间。当源库读取快目标库写入慢时，缓存在内存中的数据最大占用空间 = 行记录大小 × 数据批次大小 × 通道队列大小 。
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.channelSize">
            <el-option v-for="(item,index) in channelSizeList"
                       :key="index"
                       :label="item.toString()"
                       :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="同步操作方法"
                      label-width="160px"
                      :required=true
                      v-if=" dataform.autoSyncMode!==1 "
                      prop="targetSyncOption"
                      style="width:80%">
          <el-tooltip placement="top">
            <div slot="content">
              <p>数据同步时包括增删改操作，这里选择配置执行INSERT、UPDATE、DELETE操作类型的方法;</p>
              <p>对首次数据加载无效，只对数据同步有效;</p>
              <p>只对有主键表有效，如果源表为视图表或无主键表则并不生效.</p>
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-select v-model="dataform.targetSyncOption">
            <el-option v-for="(item,index) in targetSyncOptionList"
                       :key="index"
                       :label="item.name"
                       :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="同步前置执行SQL脚本"
                      label-width="160px"
                      v-if=" dataform.autoSyncMode!==1 "
                      prop="targetBeforeSqlScripts"
                      style="width:80%">
          <el-input v-model="dataform.targetBeforeSqlScripts"
                    type="textarea"
                    :rows="3"
                    auto-complete="off"
                    style="width: 80%"></el-input>
          <label class="tips-style block">数据同步写入目标端数据库前执行的SQL，多个SQL间以英文逗号分隔。使用场景如：MySQL数据库关闭外键约束 SET FOREIGN_KEY_CHECKS
            = 0</label>
        </el-form-item>
        <el-form-item label="同步后置执行SQL脚本"
                      label-width="160px"
                      v-if=" dataform.autoSyncMode!==1 "
                      prop="targetAfterSqlScripts"
                      style="width:80%">
          <el-input v-model="dataform.targetAfterSqlScripts"
                    type="textarea"
                    :rows="3"
                    auto-complete="off"
                    style="width: 80%"></el-input>
          <label class="tips-style block">数据同步写入目标端数据库后执行的SQL，多个SQL间以英文逗号分隔。使用场景如：MySQL数据库恢复外键约束 SET FOREIGN_KEY_CHECKS = 1</label>
        </el-form-item>
      </div>
      <div v-show="active == 4"
           class="common-top">
        <el-alert title="说明信息"
                  type="success">
          <p>(1) 当表名映射规则记录为空时，代表目标表名与源表名的名称相同;</p>
          <p>(2) 当字段名映射规则记录为空时，代表目标表的字段名与源表名的字段名相同</p>
          <p>(3) 在字段名映射规则中，如果目标字段名为空（未填写），则代表剔除该字段（不能是主键）的同步</p>
        </el-alert>
        <el-button type="primary"
                   @click="addTableNameMapperListRow()"
                   round>添加表名映射</el-button>
        <el-button type="warning"
                   @click="previewTableNameMapList()"
                   round>预览表名映射</el-button>
        <el-table :data="dataform.tableNameMapper"
                  size="mini"
                  border
                  height="200"
                  style="width:90%;margin-top: 15px;">
          <template slot="empty">
            <span>请点击"添加表名映射"按钮添加表名映射关系记录</span>
          </template>
          <el-table-column label="表名匹配的正则名"
                           width="320">
            <template slot-scope="scope">
              <el-input v-model="scope.row.fromPattern"
                        type="string"> </el-input>
            </template>
          </el-table-column>
          <el-table-column label="替换的目标值"
                           width="320">
            <template slot-scope="scope">
              <el-input v-model="scope.row.toValue"
                        type="string"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="操作"
                           width="220">
            <template slot-scope="scope">
              <el-button size="mini"
                         type="danger"
                         @click="deleteTableNameMapperListItem(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary"
                   @click="addColumnNameMapperListRow()"
                   round>添加字段名映射</el-button>
        <el-button type="warning"
                   @click="previewColumnNameMapList()"
                   round>预览字段名映射</el-button>
        <el-table :data="dataform.columnNameMapper"
                  size="mini"
                  border
                  height="200"
                  style="width:90%;margin-top: 15px;">
          <template slot="empty">
            <span>请点击"添加字段名映射"按钮添加字段名映射关系记录</span>
          </template>
          <el-table-column label="字段名匹配的正则名"
                           width="320">
            <template slot-scope="scope">
              <el-input v-model="scope.row.fromPattern"
                        type="string"> </el-input>
            </template>
          </el-table-column>
          <el-table-column label="替换的目标值"
                           width="320">
            <template slot-scope="scope">
              <el-input v-model="scope.row.toValue"
                        type="string"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="操作"
                           width="220">
            <template slot-scope="scope">
              <el-button size="mini"
                         type="danger"
                         @click="deleteColumnNameMapperListItem(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-show="active == 5"
           class="common-top">
        <commonInfo :infoform="dataform"></commonInfo>
      </div>
    </el-form>

    <el-button round
               v-if="active > 1"
               style="margin-top: 12px;margin-left: 20px"
               @click="pre">
      上一步
    </el-button>
    <el-button round
               @click="next"
               v-if="active > 0 && active < 5"
               style="margin-left: 20px">
      下一步
    </el-button>
    <el-button round
               @click="handleSave"
               v-if="active == 5">
      提交
    </el-button>

    <el-dialog v-if="active == 2"
               title="选择增量同步表的增量标识字段"
               :visible.sync="columnNameIncrementDialogVisible"
               :showClose="false"
               :before-close="handleClose">
      <el-select @change="queryPreviewColumnNameMapperList"
                 v-model="preiveTableName"
                 placeholder="请选择">
        <el-option v-for="(item,index) in preiveSeeTableNameList"
                   :key="index"
                   :label="item"
                   :value="item"></el-option>
      </el-select>
      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="columnNamesMapperData"
                @row-click="singleRowClick"
                highlight-current-row
                size="mini"
                border>
        <el-table-column label="#"
                         min-width="10%">
          <template slot-scope="scope">
            {{scope.$index}}
          </template>
        </el-table-column>
        <el-table-column prop="originalName"
                         label="字段名"
                         min-width="30%"></el-table-column>
        <el-table-column prop="typeName"
                         label="字段类型"
                         min-width="30%"></el-table-column>
        <el-table-column prop="canIncrement"
                         label="可标识增量"
                         min-width="20%">
          <template slot-scope="scope">
            <el-tag size="medium">{{ boolValueFormat(scope.row.canIncrement) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="选择"
                         min-width="10%">
          <template slot-scope="scope">
            <el-radio :label="scope.row.originalName"
                      v-model="radio"
                      :disabled="!scope.row.canIncrement"
                      @change.native="singleRowClick(scope.row)">{{""}}</el-radio>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="handleConfirmSelectIncrTableColumn">确定</el-button>
        <el-button @click="handleCancelSelectIncrTableColumn">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog v-if="active == 2"
               title="提示信息"
               :visible.sync="showDataSyncMessageDialogVisible"
               :showClose="false"
               :before-close="handleClose">
      <el-alert title="1、数据同步概念"
                type="warning"
                :closable="false"
                show-icon>
        <ul>
          <li><b>全量同步:</b> 先truncate清空目标表后，然后将源端表数据全部插入目标表的过程</li>
          <li><b>增量同步:</b> 根据增量表指定的增量字段，使用带有WHERE field > value的条件SQL查询源端表数据，然后插入目标表的过程</li>
          <li><b>变化量同步:</b> 在源端表和目标表都有主键且映射一致的条件下，通过两边数据比对计算出差异，然后目标表执行插入/更新/删除数据的过程</li>
        </ul>
      </el-alert>
      <el-alert title="2、dbswitch同步逻辑"
                type="info"
                :closable="false"
                show-icon>
        <ul>
          <li><b>步骤1:</b> 如果是首次同步，则会自动创建目标表，并执行全量数据同步;</li>
          <li><b>步骤2:</b> 非首次同步时，如果表配置了增量同步标识字段，则会执行增量数据同步;</li>
          <li><b>步骤3:</b> 非首次同步时，且没有配置增量同步标识字段，如果两端都有主键且映射一致，则会执行变化量数据同步;</li>
          <li><b>步骤3:</b> 非首次同步时，且没有配置增量同步标识字段，如果两端没有主键或主键不一致，则会执行全量数据同步;</li>
        </ul>
      </el-alert>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="showDataSyncMessageDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog v-if="active == 4"
               title="查看表名映射关系"
               :visible.sync="tableNameMapperDialogVisible"
               :showClose="false"
               :before-close="handleClose">
      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="tableNamesMapperData"
                size="mini"
                border>
        <el-table-column prop="originalName"
                         label="源端表名"
                         min-width="20%"></el-table-column>
        <el-table-column prop="targetName"
                         label="目标表名"
                         min-width="20%"></el-table-column>
      </el-table>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="tableNameMapperDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog v-if="active == 4"
               title="查看字段映射关系"
               :visible.sync="columnNameMapperDialogVisible"
               :showClose="false"
               :before-close="handleClose">
      <el-select @change="queryPreviewColumnNameMapperList"
                 v-model="preiveTableName"
                 placeholder="请选择">
        <el-option v-for="(item,index) in preiveSeeTableNameList"
                   :key="index"
                   :label="item"
                   :value="item"></el-option>
      </el-select>
      <el-table :header-cell-style="{background:'#eef1f6',color:'#606266'}"
                :data="columnNamesMapperData"
                size="mini"
                border>
        <el-table-column prop="originalName"
                         label="原始字段名"
                         min-width="20%"></el-table-column>
        <el-table-column prop="targetName"
                         label="目标表字段名"
                         min-width="20%"></el-table-column>
      </el-table>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="columnNameMapperDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

  </el-card>
</template>

<script>
import commonInfo from '@/views/task/common/info'

export default {
  components: { commonInfo },
  data () {
    return {
      cronExprOptionList: [
        {
          name: "每5分钟执行1次",
          value: "0 0/5 * * * ? *"
        },
        {
          name: "每30分钟执行1次",
          value: "0 0/30 * * * ? *"
        },
        {
          name: "每1小时执行1次",
          value: "0 0 0/1 * * ? *"
        },
        {
          name: "每2小时执行1次",
          value: "0 0 0/2 * * ? *"
        },
        {
          name: "每8小时执行1次",
          value: "0 0 0/8 * * ? *"
        },
        {
          name: "每12小时执行1次",
          value: "0 0 0/12 * * ? *"
        },
        {
          name: "每日0时执行1次",
          value: "0 0 0 1/1 * ? *"
        }
      ],
      nameConvertList: [
        {
          name: "无转换",
          value: "NONE"
        },
        {
          name: "转大写",
          value: "UPPER"
        },
        {
          name: "转小写",
          value: "LOWER"
        },
        {
          name: "下划线转驼峰",
          value: "CAMEL"
        },
        {
          name: "驼峰转下划线",
          value: "SNAKE"
        }
      ],
      batchSizeList: [100, 500, 1000, 5000, 10000, 20000],
      channelSizeList: [10, 20, 40, 60, 80, 100, 200, 500, 1000],
      targetSyncOptionList: [
        {
          name: "只同步INSERT操作",
          value: "ONLY_INSERT"
        },
        {
          name: "只同步UPDATE操作",
          value: "ONLY_UPDATE"
        },
        {
          name: "只同步INSERT和UPDATE",
          value: "INSERT_UPDATE"
        },
        {
          name: "只同步DELETE操作",
          value: "ONLY_DELETE"
        },
        {
          name: "只同步UPDATE和DELETE",
          value: "UPDATE_DELETE"
        },
        {
          name: "执行所有的同步操作",
          value: "INSERT_UPDATE_DELETE"
        }
      ],
      dataform: {
        id: 0,
        name: "",
        description: "",
        scheduleMode: "MANUAL",
        scheduleModeName: "手动调度",
        cronExpression: "",
        sourceConnectionId: '请选择',
        sourceTypeName: 'MySQL',
        sourceSchema: "",
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
        targetConnectionId: '请选择',
        targetTypeName: 'MySQL',
        targetDropTable: true,
        targetOnlyCreate: false,
        targetAutoIncrement: false,
        autoSyncMode: 2,
        targetSchema: "",
        batchSize: 5000,
        channelSize: 100,
        targetSyncOption: 'INSERT_UPDATE_DELETE',
        targetBeforeSqlScripts: '',
        targetAfterSqlScripts: '',
      },
      rules: {
        name: [
          {
            required: true,
            message: "任务名称不能为空",
            trigger: "blur"
          }
        ],
        scheduleMode: [
          {
            required: true,
            type: 'string',
            message: "调度方式必须选择",
            trigger: "change"
          }
        ],
        sourceConnectionId: [
          {
            required: true,
            type: 'integer',
            message: "必选选择一个来源端",
            trigger: "change"
          }
        ],
        sourceSchema: [
          {
            required: true,
            type: 'string',
            message: "必选选择一个Schema名",
            trigger: "change"
          }
        ],
        tableType: [
          {
            required: true,
            type: 'string',
            message: "表类型必须选择",
            trigger: "change"
          }
        ],
        includeOrExclude: [
          {
            required: true,
            type: 'string',
            message: "配置方式必须选择",
            trigger: "change"
          }
        ],
        sourceTables: [
          {
            required: false,
            type: 'array',
            message: "必选选择一个Table名",
            trigger: "change"
          }
        ],
        targetConnectionId: [
          {
            required: true,
            type: 'integer',
            message: "必选选择一个目的端",
            trigger: "change"
          }
        ],
        targetSchema: [
          {
            required: true,
            type: 'string',
            message: "必选选择一个Schema名",
            trigger: "change"
          }
        ],
        batchSize: [
          {
            required: true,
            type: 'integer',
            message: "必选选择一个数据批次大小",
            trigger: "change"
          }
        ],
        channelSize: [
          {
            required: true,
            type: 'integer',
            message: "必选选择一个通道队列大小",
            trigger: "change"
          }
        ],
        targetSyncOption: [
          {
            required: true,
            type: 'string',
            message: "必选选择一个同步方法来 ",
            trigger: "change"
          }
        ]
      },
      active: 1,
      radio: '0',
      sourceConnection: {},
      targetConnection: {},
      sourceConnectionSchemas: [],
      sourceSchemaTables: [],
      targetConnectionSchemas: [],
      columnNameIncrementDialogVisible: false,
      showDataSyncMessageDialogVisible: false,
      tableNameMapperDialogVisible: false,
      columnNameMapperDialogVisible: false,
      tableNamesMapperData: [],
      columnNamesMapperData: [],
      preiveSeeTableNameList: [],
      preiveTableName: "",
      tempIncrTableName: "",
      tempIncrColumnName: "",
    }
  },
  methods: {
    initScheduleModeTemp (val) {
      if (val === 'SYSTEM_SCHEDULED') {
        return "系统调度"
      }
      if (val === 'MANUAL') {
        return "手动调度"
      }
    },
    scheduleModeUpdate (val) {
      if (val === '系统调度') {
        this.dataform.scheduleMode = "SYSTEM_SCHEDULED"
      }
      if (val === '手动调度') {
        this.dataform.scheduleMode = "MANUAL"
      }
    },
    boolValueFormat (value) {
      if (value === true) {
        return "是";
      } else {
        return "否";
      }
    },
    handleClose (done) {
    },
    next () {
      if (this.active++ > 4) this.active = 5
    },
    pre () {
      if (this.active-- < 2) this.active = 1
    },
    loadConnections: function () {
      this.connectionNameList = [];
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/connection/list/name"
      }).then(
        res => {
          if (0 === res.data.code) {
            this.connectionNameList = res.data.data;
          } else {
            this.connectionNameList = [];
            if (res.data.message) {
              alert("加载任务列表失败:" + res.data.message);
            }
          }
        }
      );
    },
    loadAssignmentDetail: function () {
      if (this.$route.query.id && this.$route.query.id > 0) {
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
            this.dataform = {
              id: detail.id,
              name: detail.name,
              description: detail.description,
              scheduleMode: detail.scheduleMode,
              scheduleModeName: this.initScheduleModeTemp(detail.scheduleMode),
              cronExpression: detail.cronExpression,
              sourceConnectionId: detail.configuration.sourceConnectionId,
              sourceTypeName: detail.configuration.sourceTypeName,
              sourceConnectionName: detail.configuration.sourceConnectionName,
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
              targetTypeName: detail.configuration.targetTypeName,
              targetConnectionName: detail.configuration.targetConnectionName,
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
            };
            this.selectChangedSourceConnection(this.dataform.sourceConnectionId)
            this.selectCreateChangedSourceSchema(this.dataform.sourceSchema)
            this.selectChangedTargetConnection(this.dataform.targetConnectionId)
          } else {
            if (res.data.message) {
              alert("查询任务失败," + res.data.message);
            }
          }
        });
      }
    },
    changeCreateCronExpression: function (value) {
      this.dataform.cronExpression = value;
    },
    selectChangedSourceConnection: function (value) {
      this.sourceConnection = this.connectionNameList.find(
        (item) => {
          return item.id === value;
        });
      if (this.sourceConnection) {
        this.dataform.sourceTypeName = this.sourceConnection.typeName;
      }

      this.sourceConnectionSchemas = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/schemas/get/" + value
      ).then(res => {
        if (0 === res.data.code) {
          this.sourceConnectionSchemas = res.data.data;
        } else {
          this.$message.error("查询来源端数据库的Schema失败," + res.data.message);
          this.sourceConnectionSchemas = [];
        }
      });
    },
    selectCreateChangedSourceSchema: function (value) {
      this.sourceSchemaTables = [];
      if ('TABLE' === this.dataform.tableType) {
        this.$http.get(
          "/dbswitch/admin/api/v1/connection/tables/get/" + this.dataform.sourceConnectionId + "?schema=" + value
        ).then(res => {
          if (0 === res.data.code) {
            this.sourceSchemaTables = res.data.data;
          } else {
            this.$message.error("查询来源端数据库在指定Schema下的物理表列表失败," + res.data.message);
            this.sourceSchemaTables = [];
          }
        });
      } else {
        this.$http.get(
          "/dbswitch/admin/api/v1/connection/views/get/" + this.dataform.sourceConnectionId + "?schema=" + value
        ).then(res => {
          if (0 === res.data.code) {
            this.sourceSchemaTables = res.data.data;
          } else {
            this.$message.error("查询来源端数据库在指定Schema下的视图表列表失败," + res.data.message);
            this.sourceSchemaTables = [];
          }
        });
      }
    },
    selectCreateChangedTableType: function (value) {
      this.sourceSchemaTables = [];
      if ('TABLE' === value) {
        this.$http.get(
          "/dbswitch/admin/api/v1/connection/tables/get/" + this.dataform.sourceConnectionId + "?schema=" + this.dataform.sourceSchema
        ).then(res => {
          if (0 === res.data.code) {
            this.sourceSchemaTables = res.data.data;
          } else {
            this.$message.error("查询来源端数据库在指定Schema下的物理表列表失败," + res.data.message);
            this.sourceSchemaTables = [];
          }
        });
      } else {
        this.$http.get(
          "/dbswitch/admin/api/v1/connection/views/get/" + this.dataform.sourceConnectionId + "?schema=" + this.dataform.sourceSchema
        ).then(res => {
          if (0 === res.data.code) {
            this.sourceSchemaTables = res.data.data;
          } else {
            this.$message.error("查询来源端数据库在指定Schema下的视图表列表失败," + res.data.message);
            this.sourceSchemaTables = [];
          }
        });
      }
    },
    handleAddInputIncrTable: function () {
      if (!this.dataform.sourceConnectionId || this.dataform.sourceConnectionId < 0
        || !this.dataform.sourceSchema || this.dataform.sourceSchema.length == 0) {
        alert("请选择【源端数据源】和【源端模式名】！");
        return;
      }

      if (!this.dataform.includeOrExclude) {
        alert("请选择源端表选择的【配置方式】！");
        return;
      }

      if (this.dataform.includeOrExclude == "INCLUDE") {
        if (this.dataform.sourceTables.length == 0) {
          this.preiveSeeTableNameList = this.sourceSchemaTables;
        } else {
          this.preiveSeeTableNameList = this.dataform.sourceTables;
        }
      } else {
        if (this.dataform.sourceTables.length == 0) {
          alert("请选择排除表的【表名配置】！");
          return;
        }

        this.preiveSeeTableNameList = JSON.parse(JSON.stringify(this.sourceSchemaTables));
        for (var i = 0; i < this.dataform.sourceTables.length; ++i) {
          var one = this.dataform.sourceTables[i];
          this.preiveSeeTableNameList.some((item, index) => {
            if (item == one) {
              this.preiveSeeTableNameList.splice(index, 1)
              return true;
            }
          })
        }
      }
      this.columnNameIncrementDialogVisible = true;
    },
    handleDeleteIncrTable: function (index) {
      this.dataform.incrTableColumns.splice(index, 1);
    },
    selectChangedTargetConnection: function (value) {
      this.targetConnection = this.connectionNameList.find(
        (item) => {
          return item.id === value;
        });
      if (this.targetConnection) {
        this.dataform.targetTypeName = this.targetConnection.typeName;
      }

      this.targetConnectionSchemas = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/schemas/get/" + value
      ).then(res => {
        if (0 === res.data.code) {
          this.targetConnectionSchemas = res.data.data;
        } else {
          this.$message.error("查询目的端数据库的Schema失败," + res.data.message);
          this.targetConnectionSchemas = [];
        }
      });
    },
    addTableNameMapperListRow: function () {
      this.dataform.tableNameMapper.push({ "fromPattern": "", "toValue": "" });
    },
    deleteTableNameMapperListItem: function (index) {
      this.dataform.tableNameMapper.splice(index, 1);
    },
    previewTableNameMapList: function () {
      if (!this.dataform.sourceConnectionId || this.dataform.sourceConnectionId < 0
        || !this.dataform.sourceSchema || this.dataform.sourceSchema.length == 0) {
        alert("请选择【源端数据源】和【源端模式名】！");
        return;
      }

      if (!this.dataform.includeOrExclude) {
        alert("请选择源端表选择的【配置方式】！");
        return;
      }

      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/mapper/preview/table",
        data: JSON.stringify({
          id: this.dataform.sourceConnectionId,
          schemaName: this.dataform.sourceSchema,
          isInclude: this.dataform.includeOrExclude == 'INCLUDE',
          tableNames: this.dataform.sourceTables,
          nameMapper: this.dataform.tableNameMapper,
          tableNameCase: this.dataform.tableNameCase
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.tableNamesMapperData = res.data.data;
          this.tableNameMapperDialogVisible = true;
        } else {
          this.tableNamesMapperData = [];
          if (res.data.message) {
            alert(res.data.message);
          }
        }
      });

    },
    addColumnNameMapperListRow: function () {
      this.dataform.columnNameMapper.push({ "fromPattern": "", "toValue": "" });
    },
    deleteColumnNameMapperListItem: function (index) {
      this.dataform.columnNameMapper.splice(index, 1);
    },
    previewColumnNameMapList: function () {
      if (!this.dataform.sourceConnectionId || this.dataform.sourceConnectionId <= 0
        || !this.dataform.sourceSchema || this.dataform.sourceSchema.length == 0) {
        alert("请选择【源端数据源】和【源端模式名】！");
        return;
      }

      if (!this.dataform.includeOrExclude) {
        alert("请选择源端表选择的【配置方式】！");
        return;
      }


      if (this.dataform.includeOrExclude == "INCLUDE") {
        if (this.dataform.sourceTables.length == 0) {
          this.preiveSeeTableNameList = this.sourceSchemaTables;
        } else {
          this.preiveSeeTableNameList = this.dataform.sourceTables;
        }
      } else {
        if (this.dataform.sourceTables.length == 0) {
          alert("请选择排除表的【表名配置】！");
          return;
        }

        this.preiveSeeTableNameList = JSON.parse(JSON.stringify(this.sourceSchemaTables));
        for (var i = 0; i < this.dataform.sourceTables.length; ++i) {
          var one = this.dataform.sourceTables[i];
          this.preiveSeeTableNameList.some((item, index) => {
            if (item == one) {
              this.preiveSeeTableNameList.splice(index, 1)
              return true;
            }
          })
        }
      }
      this.preiveTableName = "";
      this.columnNamesMapperData = [];
      this.columnNameMapperDialogVisible = true;
    },
    queryPreviewColumnNameMapperList: function () {
      if (!this.preiveSeeTableNameList || this.preiveSeeTableNameList.length == 0) {
        alert("请在源端配置【表名配置】！");
        return;
      }

      if (!this.preiveTableName || this.preiveTableName.length == 0) {
        alert("请选择一个表名！");
        return;
      }

      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/mapper/preview/column",
        data: JSON.stringify({
          id: this.dataform.sourceConnectionId,
          schemaName: this.dataform.sourceSchema,
          isInclude: this.dataform.includeOrExclude == 'INCLUDE',
          tableName: this.preiveTableName,
          nameMapper: this.dataform.columnNameMapper,
          columnNameCase: this.dataform.columnNameCase
        })
      }).then(res => {
        if (0 === res.data.code) {
          this.columnNamesMapperData = res.data.data;
        } else {
          if (res.data.message) {
            alert(res.data.message);
          }
        }
      });
    },
    singleRowClick (row) {
      if (row.canIncrement) {
        this.tempIncrTableName = this.preiveTableName;
        this.tempIncrColumnName = row.originalName;
        this.radio = row.originalName;
        console.log("table=" + this.tempIncrTableName + ";column=" + this.tempIncrColumnName)
      } else {
        this.$alert("非整型或日期时间类型不能被选中", "提示信息",
          {
            confirmButtonText: "确定",
            type: "warn"
          }
        );
      }
    },
    handleConfirmSelectIncrTableColumn: function () {
      if (!this.tempIncrTableName || !this.tempIncrColumnName) {
        this.$alert("请选择一个标识增量字段来", "错误信息",
          {
            confirmButtonText: "确定",
            type: "error"
          }
        );
        return;
      }
      if (!this.dataform.incrTableColumns.find(item => item.tableName === this.tempIncrTableName)) {
        this.dataform.incrTableColumns.push(
          {
            tableName: this.tempIncrTableName,
            columnName: this.tempIncrColumnName
          }
        );
        this.handleCancelSelectIncrTableColumn();
      } else {
        this.$alert("已经存在增量同步表[" + this.tempIncrTableName + "]的配置了", "提示信息",
          {
            confirmButtonText: "确定",
            type: "info"
          }
        );
      }
    },
    handleCancelSelectIncrTableColumn: function () {
      this.columnNameIncrementDialogVisible = false;
      this.preiveTableName = "";
      this.columnNamesMapperData = [];
      this.tempIncrTableName = "";
      this.tempIncrColumnName = "";
      this.radio = "";
    },
    handleSave: function () {
      if (0 === this.dataform.autoSyncMode) {
        this.dataform.targetDropTable = false;
        this.dataform.targetOnlyCreate = false;
      } else if (1 === this.dataform.autoSyncMode) {
        this.dataform.targetDropTable = true;
        this.dataform.targetOnlyCreate = true;
      } else {
        this.dataform.targetDropTable = true;
        this.dataform.targetOnlyCreate = false;
      }
      this.$refs['dataform'].validate(valid => {
        if (valid) {
          if (this.$route.query.id && this.$route.query.id > 0) {
            this.$http({
              method: "POST",
              headers: {
                'Content-Type': 'application/json'
              },
              url: "/dbswitch/admin/api/v1/assignment/update",
              data: JSON.stringify({
                id: this.$route.query.id,
                name: this.dataform.name,
                description: this.dataform.description,
                scheduleMode: this.dataform.scheduleMode,
                cronExpression: this.dataform.cronExpression,
                config: {
                  sourceConnectionId: this.dataform.sourceConnectionId,
                  sourceSchema: this.dataform.sourceSchema,
                  tableType: this.dataform.tableType,
                  includeOrExclude: this.dataform.includeOrExclude,
                  sourceTables: this.dataform.sourceTables,
                  incrTableColumns: this.dataform.incrTableColumns,
                  sourceBeforeSqlScripts: this.dataform.sourceBeforeSqlScripts,
                  sourceAfterSqlScripts: this.dataform.sourceAfterSqlScripts,
                  targetConnectionId: this.dataform.targetConnectionId,
                  targetSchema: this.dataform.targetSchema,
                  tableNameMapper: this.dataform.tableNameMapper,
                  columnNameMapper: this.dataform.columnNameMapper,
                  tableNameCase: this.dataform.tableNameCase,
                  columnNameCase: this.dataform.columnNameCase,
                  targetDropTable: this.dataform.targetDropTable,
                  targetOnlyCreate: this.dataform.targetOnlyCreate,
                  targetAutoIncrement: this.dataform.targetAutoIncrement,
                  batchSize: this.dataform.batchSize,
                  channelSize: this.dataform.channelSize,
                  targetSyncOption: this.dataform.targetSyncOption,
                  targetBeforeSqlScripts: this.dataform.targetBeforeSqlScripts,
                  targetAfterSqlScripts: this.dataform.targetAfterSqlScripts,
                }
              })
            }).then(res => {
              if (0 === res.data.code) {
                this.$message({
                  message: '修改任务成功!',
                  type: 'success'
                });
                this.$router.push('/task/list')
              } else {
                if (res.data.message) {
                  alert(res.data.message);
                }
              }
            });
          } else {
            this.$http({
              method: "POST",
              headers: {
                'Content-Type': 'application/json'
              },
              url: "/dbswitch/admin/api/v1/assignment/create",
              data: JSON.stringify({
                name: this.dataform.name,
                description: this.dataform.description,
                scheduleMode: this.dataform.scheduleMode,
                cronExpression: this.dataform.cronExpression,
                config: {
                  sourceConnectionId: this.dataform.sourceConnectionId,
                  sourceSchema: this.dataform.sourceSchema,
                  tableType: this.dataform.tableType,
                  includeOrExclude: this.dataform.includeOrExclude,
                  sourceTables: this.dataform.sourceTables,
                  incrTableColumns: this.dataform.incrTableColumns,
                  sourceBeforeSqlScripts: this.dataform.sourceBeforeSqlScripts,
                  sourceAfterSqlScripts: this.dataform.sourceAfterSqlScripts,
                  targetConnectionId: this.dataform.targetConnectionId,
                  targetSchema: this.dataform.targetSchema,
                  tableNameMapper: this.dataform.tableNameMapper,
                  columnNameMapper: this.dataform.columnNameMapper,
                  tableNameCase: this.dataform.tableNameCase,
                  columnNameCase: this.dataform.columnNameCase,
                  targetDropTable: this.dataform.targetDropTable,
                  targetOnlyCreate: this.dataform.targetOnlyCreate,
                  targetAutoIncrement: this.dataform.targetAutoIncrement,
                  batchSize: this.dataform.batchSize,
                  channelSize: this.dataform.channelSize,
                  targetSyncOption: this.dataform.targetSyncOption,
                  targetBeforeSqlScripts: this.dataform.targetBeforeSqlScripts,
                  targetAfterSqlScripts: this.dataform.targetAfterSqlScripts,
                }
              })
            }).then(res => {
              if (0 === res.data.code) {
                this.$message({
                  message: '添加任务成功!',
                  type: 'success'
                });
                this.$router.push('/task/list')
              } else {
                if (res.data.message) {
                  alert(res.data.message);
                }
              }
            });
          }
        } else {
          alert("请点击【上一步】检查输入");
        }
      });
    }
  },
  created () {
    this.loadConnections();
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

.tip-content {
  font-size: 12px;
}

.name-mapper-table,
.name-mapper-table table tr th,
.name-mapper-table table tr td {
  border-collapse: collapse;
  border: 1px solid #e0dddd;
  width: 100%;
}

.el-descriptions__body
  .el-descriptions__table
  .el-descriptions-row
  .el-descriptions-item__label {
  min-width: 20px;
  max-width: 60px;
}

.tips-style {
  font-size: 12px;
  color: #a0a6b8;
}

.block {
  padding-top: 6px;
  display: block;
}

.common-top {
  margin-top: 40px;
}
</style>