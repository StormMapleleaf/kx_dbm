# flyway适配高斯数据库

由于高斯和postgresql使用的驱动都是一样的，所以基于flyway对已有的postgresql数据库来改造适配，本模块源代码来自于flyway项目的开
源的flyway-core模块代码（版本为6.4.4）, flyway开源项目地址：```https://github.com/flyway/flyway```。

- 1、PostgreSQLConnection 类中的 doRestoreOriginalState 方法

方法作用：将数据库连接的角色（role）重置为其原始值，确保在迁移或回调过程中更改的角色被还原回初始状态，在Flyway的设计中，可能会在迁
移或回调期间更改数据库连接的角色，以满足特定需求。为了保证不同迁移之间的一致性，当完成迁移或回调时，需要将角色重置为初始状态，以免对后
续迁移或操作产生影响。

把***这个方法体注释掉***，经测试高斯和postgresql均无影响。由于 postgresql 和 高斯 之间对设置 role 语法之间的差异，高斯数据库 set 角色
时还需要带上密码，而postgresql则不用。

- 2、PostgreSQLDatabase 类中的 ensureSupported 方法

方法作用：确保数据库与当前使用的Flyway版本兼容，并提供相应的建议或推荐操作。
```
@Override
public final void ensureSupported() {
    // 检查数据库版本不低于 9.0
    ensureDatabaseIsRecentEnough("9.0");
    // 检查数据库是否高于指定版本，并且推荐升级到某个特定的Flyway版本（在6.2.2源码中为9.4版本）。如果数据库版本较旧，并且与所需的Flyway版本不兼容，将给出相应的建议
    ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("9.4", org.flywaydb.core.internal.license.Edition.ENTERPRISE);
    // 检查数据库是否需要升级到指定的主要版本，如果数据库的版本低于指定版本，给出升级Flyway的建议
    recommendFlywayUpgradeIfNecessaryForMajorVersion("12");
}
```
高斯数据库是基于 postgresql 9.2 改造的，在 flyway 中是不支持的，所以降低源码中给出的版本，否则就会报推荐升级数据库的版本或者使用
Flyway Teams Edition，Flyway Teams Edition 可以支持 postgresql 9.2，这个是企业版要收费的。

```
ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("9.0", org.flywaydb.core.internal.license.Edition.ENTERPRISE);
```

- 3、PostgreSQLDatabase 类中的 getRawCreateScript 方法

方法作用：生成创建数据库表的原始SQL脚本字符串，用于存储迁移历史记录，就是生成 flyway_scheme_history 表。

高斯数据库执行时创建 flyway_scheme_history 表会丢失 checksum 的值，导致最后执行不了指定文件夹的脚本语句，把这个方法执行的逻辑调整下:

```
@Override
public String getRawCreateScript(Table table, boolean baseline) {

    //todo 这里高斯会创建一条空的baseline记录，调整下执行逻辑
    String tablespace = configuration.getTablespace() == null
        ? ""
        : " TABLESPACE \"" + configuration.getTablespace() + "\"";

    String createTableScript = "CREATE TABLE " + table + " (\n" +
        "    \"installed_rank\" INT NOT NULL,\n" +
        "    \"version\" VARCHAR(50),\n" +
        "    \"description\" VARCHAR(200) NOT NULL,\n" +
        "    \"type\" VARCHAR(20) NOT NULL,\n" +
        "    \"script\" VARCHAR(1000) NOT NULL,\n" +
        "    \"checksum\" INTEGER,\n" +
        "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
        "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n" +
        "    \"execution_time\" INTEGER NOT NULL,\n" +
        "    \"success\" BOOLEAN NOT NULL\n" +
        ")" + tablespace + ";\n";

    if (baseline) {
        return createTableScript +
            "ALTER TABLE " + table + " ADD CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\");\n" +
            "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
    } else {
        return createTableScript;
    }
}
```

最后，为了不跟本地仓库原有的artifactId坐标有冲突,故将flyway-core的 groupId这里改为org.dromara.dbswitch。
