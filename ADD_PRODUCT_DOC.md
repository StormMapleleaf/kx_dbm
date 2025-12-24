# 接入新的自定义关系型数据库的开发说明文档

### 1、背景说明

虽然dbswitch已经集成支持了MySQL、Oracle、SQLServer、PostgreSQL、DB2、SBase、MariaDB、SQLite、Hive及
DM、Kingbase、OSCar、GBase等部分国产数据库。但近些年关系型数据库不断涌现，不过基本上都是类似或兼容MySQL、Oracle、
PostgreSQL等数据库的，只是存在某些特殊的语法情况而已。为此，为方便适配这些新的数据库接入到dbswitch中来，编写此说明 
文档，方便开发者快速的开发新的适配数据库的模块。

### 2、开发步骤说明

> 这里以新增openguass为例：

**(1) dbswitch-product下新建子模块：**

在模块dbswitch-product下新建子模块，例如：dbswitch-product-openguass,并在pom.xml中配置如下依赖：

```
    <dependency>
      <groupId>org.dromara.dbswitch</groupId>
      <artifactId>dbswitch-common</artifactId>
      <version>${project.version}</version>
    </dependency>

    <dependency>
      <groupId>org.dromara.dbswitch</groupId>
      <artifactId>dbswitch-core</artifactId>
      <version>${project.version}</version>
    </dependency>
```

**(2) 添加新的数据库产品类型枚举值：**

在dbswitch-common模块里，找到org.dromara.dbswitch.common.type.ProductTypeEnum的定义，并在最后追加以数据库产品 命名的枚举字符串，例如：

```
  /**
   * OpenGauss数据库类型
   */
  OPENGAUSS(14, "\"", "opengauss", "org.opengauss.Driver", 15432,
      "SELECT 1",
      "jdbc:opengauss://",
      new String[]{"jdbc:opengauss://{host}[:{port}]/[{database}][\\?{params}]"}),
```

枚举值的附带信息对应如下：

- 编号: 14 （注意，不能与已有的重复，通常递增排序即可）
- 限定符: " (单双引号)
- 产品名字符串: OpenGauss (后续需要在前段项目里增加OpenGauss.png数据库图标文件) 
- 驱动类名: org.opengauss.Driver
- 默认端口号: 15432
- 测试连接使用的SQL: SELECT 1
- JDBC连接串的前缀: jdbc:opengauss://
- JDBC连接串的模板: jdbc:opengauss://{host}[:{port}]/[{database}][\\?{params}]

**(3) 编写对接实现模块dbswitch-product-openguass的java代码：**

```
[root@localhost dbswitch-product-opengauss]# tree .
.
├── pom.xml
└── src
    └── main
        ├── java
        │   └── org
        │       └── dromara
        │           └── dbswitch
        │               └── product
        │                   └── openguass
        │                       ├── OpenGaussFactoryProvider.java
        │                       ├── OpenGaussFeatures.java
        │                       ├── OpenGaussMetadataQueryProvider.java
        │                       └── OpenGaussTableOperateProvider.java
        └── resources
            └── META-INF
                └── services
                    └── dbswitch.providers
```

其中OpenGaussFactoryProvider.java必须继承自org.dromara.dbswitch.provider.AbstractFactoryProvider，并带有@Product注解：

```
@Product(ProductTypeEnum.OPENGAUSS)
public class OpenGaussFactoryProvider extends AbstractFactoryProvider {

  // 构造函数必须以DataSource作为参数类型，并传递给父类
  public OpenGaussFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  // 这里提供数据库特征配置实现对象（可参见OpenGaussFeatures的定义，目前基本上为空类）
  @Override
  public ProductFeatures getProductFeatures() {
    return new OpenGaussFeatures();
  }

  // 这里提供数据库元数据查询的实现对象，继承自org.dromara.dbswitch.provider.meta.AbstractMetadataProvider
  // 这里需编写类OpenGaussMetadataQueryProvider查询元数据信息的实现代码
  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new OpenGaussMetadataQueryProvider(this);
  }

  // 这里提供对数据库表的drop和truncate操作的实现对象,继承自org.dromara.dbswitch.provider.operate.DefaultTableOperateProvider
  @Override
  public TableOperateProvider createTableOperateProvider() {
    return new OpenGaussTableOperateProvider(this);
  }

  // 这里提供对数据库表的批量插入数据操作的实现对象，继承自org.dromara.dbswitch.provider.write.DefaultTableDataWriteProvider
  // 也可配置为 new AutoCastTableDataWriteProvider(this);
  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new AutoCastTableDataWriteProvider(this);
  }

  // 这里提供对数据库表数据的Insert、Update、Delete同步操作的实现对象，继承自org.dromara.dbswitch.provider.sync.DefaultTableDataSynchronizer
  // 也可配置为 new AutoCastTableDataSynchronizer(this);
  @Override
  public TableDataSynchronizer createTableDataSynchronizer() {
    return new AutoCastTableDataSynchronizer(this);
  }
}

```

**(4) 添加dbswitch.providers实现类配置:**
  在文件（需要创建）resources/META-INF/services/dbswitch.providers中，添加上述实现类的全类名：

```
[root@localhost dbswitch-product-opengauss]# cat src/main/resources/META-INF/services/dbswitch.providers 
org.dromara.dbswitch.product.openguass.OpenGaussFactoryProvider
```

**(5) 为dbswitch-register-product增加依赖：**

在dbswitch-product/dbswitch-register-product/pom.xml中增加依赖：

```
    <!-- 新增加的数据库产品需要在这里追加依赖-->
    <dependency>
      <groupId>org.dromara.dbswitch</groupId>
      <artifactId>dbswitch-product-openguass</artifactId>
      <version>${project.version}</version>
    </dependency>
```

**(6) 根目录下的drivers下添加驱动jar文件**

在根目录下的drivers/opengauss/opengauss-3.0.0/下增加对应的版本的驱动jar文件。

**(7) 前端项目中增加PGN数据库图标文件**

对于前段项目，还需要在dbswitch-admin-ui/src/icons目录下增加对应数据库的PNG图标文件，文件命名需为
org.dromara.dbswitch.common.type.ProductTypeEnum#name做文件名，.png做文件后缀。
例如，这里的openguass的数据库文件名为：OpenGauss.png



完成上述的开发内容后，即可使用根目录下的build.sh(linux/MacOS下)或build.cmd(windows下)进行打包后部署了。如果成功会在日志中
有如下内容的输出，包含有新添加的OPENGAUSS注册的信息：
```
2023-01-28 21:35:13.768 [main] INFO  c.g.d.p.r.ProductRegisterAutoConfiguration - Register database product now ...
...
2023-01-28 21:35:13.787 [main] INFO  org.dromara.dbswitch.provider.ProductProviderFactory - Register product OPENGAUSS by subclass :org.dromara.dbswitch.product.openguass.OpenGaussFactoryProvider 
....
2023-01-28 21:35:13.805 [main] INFO  c.g.d.p.r.ProductRegisterAutoConfiguration - Finish to register total 14 database product !
```
