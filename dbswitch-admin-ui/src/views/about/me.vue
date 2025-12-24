<template>
  <div>
    <h3>关于dbswitch工具</h3>
    <el-tabs v-model="activeName"
             @tab-click="handleClick()">
      <el-tab-pane label="系统功能"
                   name="first">
        <div class="content_tag">
          <el-tag type="danger">迁移</el-tag>
          <el-tag type="success">同步</el-tag>
        </div>
        <div class="content_card">
          <el-card class="box-card">
            <div class="text item">
              <p>
                一句话，dbswitch工具提供源端数据库向目的端数据的<B>迁移同步</B>功能，包括全量和变化量方式。迁移包括：
              </p>
              <ul>
                <li>项目托管</li>
                <p>
                  Gitee地址：<a href="https://gitee.com/inrgihc/dbswitch">https://gitee.com/inrgihc/dbswitch</a>
                </p>
                <li>结构迁移</li>
                <p>
                  字段类型、主键信息、建表语句等的转换，并生成建表SQL语句。
                </p>
                <p>
                  支持基于正则表达式转换的表名与字段名映射转换。
                </p>
                <li>数据迁移</li>
                <p>
                  基于JDBC的分批次读取源端数据库数据，并基于insert/copy方式将数据分批写入目的数据库。
                </p>
                <p>
                  支持有主键表的"变化量"同步 （变化数据计算Change Data Calculate）功能。
                </p>
              </ul>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="异构数据库"
                   name="second">
        <div class="content_tag">
          <el-tag type="success">JDBC</el-tag>
          <el-tag type="danger">dbswitch</el-tag>
        </div>
        <div class="content_card">
          <el-card class="box-card">
            <div class="text item">
              <p>
                dbswitch提供异构关系数据库间的数据迁移同步，支持多种数据库，包括：
              </p>
              <ul>
                <li>甲骨文的Oracle
                </li>
                <li>微软的Microsoft SQLServer
                </li>
                <li>MySQL
                </li>
                <li>MariaDB
                </li>
                <li>PostgreSQL
                </li>
                <li>Greenplum
                </li>
                <li>IBM的DB2
                </li>
                <li>Sybase数据库
                </li>
                <li>国产达梦数据库DMDB
                </li>
                <li>国产人大金仓数据库Kingbase8
                </li>
                <li>国产翰高数据库HighGo
                </li>
                <li>国产神通数据库Oscar
                </li>
                <li>国产南大通用数据库GBase8a
                </li>
                <li>Apache Hive(基于JdbcStorageHandler)
                </li>
                <li>SQLite3
                </li>
                <li>OpenGuass
                </li>
                <li>ClickHouse
                </li>
                <li>StarRocks
                </li>
                <li>Apache Doris
                </li>
                <li>MongoDB(只支持数据加载写入，不支持变化量同步)
                </li>
                <li>ElasticSearch(只支持数据加载写入，不支持变化量同步)
                </li>
              </ul>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="开发技术栈"
                   name="third">
        <div class="content_tag">
          <el-tag type="success">SpringBoot</el-tag>
          <el-tag type="danger">Quartz</el-tag>
          <el-tag type="success">Vue/ElementUI</el-tag>
        </div>
        <div class="content_card">
          <el-card class="box-card">
            <div class="text item">
              <p>
                dbswitch基于Springboot脚手架进行的后端模块开发，模块组成结构如下：
              <pre>
            └── dbswitch
                ├── dbswitch-common                     // dbswitch通用定义模块
                ├── dbswitch-core                       // dbswitch迁移同步实现类
                ├── dbswitch-product                    // dbswitch数据库方言
                    ├── dbswitch-product-mysql          //  -> mysql方言实现类
                    ├── dbswitch-product-oracle         //  -> oracle方言实现类
                    ├── dbswitch-product-sqlserver      //  -> sqlserver方言实现类
                    ├── dbswitch-product-postgres       //  -> postgres方言实现类
                    ├── dbswitch-product-dm             //  -> dm方言实现类
                    ├── dbswitch-product-kingbase       //  -> kingbase方言实现类
                    ├── dbswitch-product-highgo         //  -> highgo方言实现类
                    ├── dbswitch-product-oscar          //  -> oscar方言实现类
                    ├── dbswitch-product-gbase          //  -> gbase方言实现类
                    ├── dbswitch-product-mariadb        //  -> mariadb方言实现类
                    ├── dbswitch-product-openguass      //  -> openguass方言实现类
                    ├── dbswitch-product-db2            //  -> db2方言实现类
                    ├── dbswitch-product-sybase         //  -> sybase方言实现类
                    ├── dbswitch-product-hive           //  -> hive方言实现类
                    ├── dbswitch-product-sqlite         //  -> sqlite方言实现类
                    ├── dbswitch-product-clickhouse     //  -> clickhouse方言实现类
                    ├── dbswitch-product-greenplum      //  -> greenplum方言实现类
                    ├── dbswitch-product-starrocks      //  -> starrocks方言实现类
                    ├── dbswitch-product-doris          //  -> doris方言实现类
                    ├── dbswitch-product-oceanbase 	    //  -> oceanbase方言实现类
                    ├── dbswitch-product-mongodb        //  -> mongodb方言实现类
                    ├── dbswitch-product-elasticsearch  //  -> elasticsearch方言实现类
                ├── dbswitch-data                       // 工具入口模块，读取配置文件中的参数执行异构迁移同步
                ├── dbswitch-admin                      // 在以上模块的基础上引入Quartz的调度服务与接口
                ├── dbswitch-admin-ui                   // 基于Vue2的前段WEB交互页面
                ├── dbswitch-dist                       // 基于maven-assembly-plugin插件的项目打包模块
                </pre>
              </p>
              <ul>
                <li>SpringBoot/Mybatis</li>
                <p>
                  dbwitch基于SpringBoot作为项目的基础框架，利用JdbcTemplate提供常规的动态SQL读写操作，实现异构数据库数据的导出与导入功能。
                </p>
                <p>
                  dbwitch-admin模块为用户交互提供了服务接口，基于Mybatis/Mybatis-plus提供配置数据的持久化。
                </p>
                <li>Quartz</li>
                <p>
                  Quartz是一个开源的作业调度框架，它完全由Java写成。dbswitch-admin基于Quartz提供了支持集群模式迁移同步任务调度功能。
                </p>
                <li>Vue/ElementUI</li>
                <p>
                  Vue是一套用于构建用户界面的渐进式JavaScript框架。 Element是饿了么团队基于MVVM框架Vue开源出来的一套前端基于Vue 2.0的桌面端组件库。
                </p>
                <p>
                  dbswitch-admin-ui模块基于Vue和ElementUI提供可视化的操作WEB界面。
                </p>
              </ul>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script>
import layout from '../layout.vue';
export default {
  components: { layout },
  data () {
    return {
      activeName: 'first'
    };
  },
  methods: {
    handleClick (tab, event) {
      //console.log(tab, event);
    }
  }
}
</script>

<style scoped>
div h3 {
  text-align: center;
}

.content_tag {
  text-align: center;
}

.content_card {
  padding-top: 20px;
}
</style>
