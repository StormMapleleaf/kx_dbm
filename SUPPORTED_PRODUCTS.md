## DBSWITCH支持的数据库产品列表

支持的数据库产品及其JDBC驱动连接示例如下：

**MySQL数据库**

```
jdbc连接地址：jdbc:mysql://172.17.2.10:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&rewriteBatchedStatements=true&useCompression=true
jdbc驱动名称： com.mysql.jdbc.Driver
```

**MariaDB数据库**

```
jdbc连接地址：jdbc:mariadb://172.17.2.10:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&rewriteBatchedStatements=true&useCompression=true
jdbc驱动名称： org.mariadb.jdbc.Driver
```

**Oracle数据库**

```
jdbc连接地址：jdbc:oracle:thin:@172.17.2.10:1521:ORCL  或   jdbc:oracle:thin:@//172.17.2.10:1521/ORCL
jdbc驱动名称：oracle.jdbc.driver.OracleDriver
```

**SQLServer(>=2005)数据库**

```
jdbc连接地址：jdbc:sqlserver://172.17.2.10:1433;DatabaseName=test
jdbc驱动名称：com.microsoft.sqlserver.jdbc.SQLServerDriver
```

**Sybase数据库**

```
jdbc连接地址：jdbc:sybase:Tds:172.17.2.10:5000/test?charset=cp936
jdbc驱动名称：com.sybase.jdbc4.jdbc.SybDriver
```
> JDBC连接Sybase数据库使用中文时只能使用CP936这个字符集

**PostgreSQL数据库**

```
jdbc连接地址：jdbc:postgresql://172.17.2.10:5432/test
jdbc驱动名称：org.postgresql.Driver
```

**Greenplum数据库**

```
jdbc连接地址：jdbc:postgresql://172.17.2.10:5432/test
jdbc驱动名称：org.postgresql.Driver
```

**DB2数据库**

```
jdbc连接地址：jdbc:db2://172.17.2.10:50000/testdb:driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;
jdbc驱动名称：com.ibm.db2.jcc.DB2Driver
```

**达梦DM数据库**

```
jdbc连接地址：jdbc:dm://172.17.2.10:5236
jdbc驱动名称：dm.jdbc.driver.DmDriver
```

**金仓Kingbase8数据库**

```
jdbc连接地址：jdbc:kingbase8://172.17.2.10:54321/MYTEST
jdbc驱动名称：com.kingbase8.Driver
```

**神通Oscar数据库**

```
jdbc连接地址：jdbc:oscar://172.17.2.10:2003/OSRDB
jdbc驱动名称：com.oscar.Driver
```

**南大通用GBase8a数据库**

```
jdbc连接地址：jdbc:gbase://172.17.2.10:5258/gbase
jdbc驱动名称：com.gbase.jdbc.Driver
```

**翰高HighGo数据库**

```
jdbc连接地址：jdbc:highgo://172.17.2.10:5866/highgo
jdbc驱动名称：com.highgo.jdbc.Driver
```

**Apache Hive数据库**

```
jdbc连接地址：jdbc:hive2://172.17.2.12:10000/default
jdbc驱动名称：org.apache.hive.jdbc.HiveDriver
```

注意：当前只支持hive version 3.x的账号密码认证方式。

**OpenGauss数据库**

```
jdbc连接地址：jdbc:opengauss://172.17.2.10:5866/test
jdbc驱动名称：org.opengauss.Driver
```

**StarRocks数据库**

```
jdbc连接地址：jdbc:mysql://127.0.0.1:9030/test?useUnicode=true&characterEncoding=utf-8&useSSL=false
jdbc驱动名称：com.mysql.jdbc.Driver
```

**Apache Doris数据库(>=1.2.2)**

```
jdbc连接地址：jdbc:mysql://127.0.0.1:9030/test?useUnicode=true&characterEncoding=utf-8&useSSL=false
jdbc驱动名称：com.mysql.jdbc.Driver
```

**OceanBase数据库**

```
jdbc连接地址：jdbc:oceanbase://127.0.0.1:2881/test?pool=false
jdbc驱动名称：com.oceanbase.jdbc.Driver
```

**ClickHouse(>=21.0.0.0)数据库**

```
jdbc连接地址：jdbc:clickhouse://172.17.2.10:8123/default
jdbc驱动名称：com.clickhouse.jdbc.ClickHouseDriver
```

**SQLite3数据库**

```
jdbc连接地址：jdbc:sqlite:/tmp/test.db   或者  jdbc:sqlite::resource:http://172.17.2.12:8080/test.db
jdbc驱动名称：org.sqlite.JDBC
```
注意:

> (a) 本地文件方式：jdbc:sqlite:/tmp/test.db , 该方式适用于dbswitch为实体机器部署的场景。
>
> (b) 远程文件方式: jdbc:sqlite::resource:http://172.17.2.12:8080/test.db ,该方式适用于容器方式部署的场景,  搭建文件服务器的方法可使
> 用如下docker方式快速部署(/home/files为服务器上存放sqlite数据库文件的目录)：
>
> ```docker run -d --name http_file_server -p 8080:8080 -v /home/files:/data inrgihc/http_file_server:latest```
>
> 说明：远程服务器文件将会被下载到本地System.getProperty("java.io.tmpdir")所指定的目录下(linux为/tmp/，Windows为C:/temp/)，并以
> sqlite-jdbc-tmp-{XXX}.db的方式进行文件命名，其中{XXX}为文件网络地址(例如上述为http://192.168.31.57:8080/test.db) 的字符串哈希值，
> 如果本地文件已经存在则不会再次进行下载而是直接使用该文件(当已经下载过文件后，远程服务器即使关闭了，该sqlite的jdbc-url任然可
> 用，直至本地的sqlite-jdbc-tmp-XXX.db文件被人为手动删除)
>
> (c) 不支持内存及其他方式;本地文件方式可以作为源端和目的端，而远程服务器方式只能作为源端。
>
> (d) SQLite为单写多读方式，禁止人为方式造成多写导致锁表。

**MongoDB数据库**

```
jdbc连接地址：jdbc:mongodb://172.17.2.12:27017/admin?authSource=admin&authMechanism=SCRAM-SHA-1
jdbc驱动名称：com.gitee.jdbc.mongodb.JdbcDriver
```

> 项目地址：https://gitee.com/inrgihc/jdbc-mongodb-driver

**ElasticSearch(7.x版本)数据库**

```
jdbc连接地址：jdbc:jest://172.17.2.12:9200?useHttps=false
jdbc驱动名称：com.gitee.jdbc.elasticsearch.JdbcDriver
```

> 项目地址：https://gitee.com/inrgihc/jdbc-jest-driver
