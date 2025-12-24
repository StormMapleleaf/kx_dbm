// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.sqlserver;

public final class SQLServerConst {

  public static final String GET_CURRENT_CATALOG_SQL =
      "Select Name From Master..SysDataBases Where DbId=(Select Dbid From Master..SysProcesses Where Spid = @@spid)";

  /**
   * 删除临时表
   */
  public static final String DROP_TEMPTABLE_SQL = "IF (OBJECT_ID('tempdb.dbo.#t') IS NOT NULL) DROP TABLE #t";

  /**
   * 创建临时表
   */
  public static final String CREATE_TEMPTABLE_SQL = "DECLARE @schemaname VARCHAR(1024)\n" +
          "DECLARE @tabname VARCHAR(1024)\n" +
          "SET @schemaname = '%s'\n" +
          "SET @tabname = '%s'\n" +
          "SELECT  'CREATE TABLE [' + DB_NAME() + '].[' + @schemaname + '].[' + so.name + '] (' + o.list + ')' \n" +
          " + CASE WHEN tc.Constraint_Name IS NULL THEN '' ELSE 'ALTER TABLE [' + DB_NAME() + '].[' + @schemaname + '].[' + so.Name + '] ADD CONSTRAINT ' + tc.Constraint_Name  + ' PRIMARY KEY ' + ' (' + LEFT(j.List, LEN(j.List)-1) + ')' END \n" +
          " AS TABLE_DDL \n" +
          " INTO #t \n" +
          " FROM sysobjects so \n" +
          " CROSS APPLY\n" +
          " (SELECT \n" +
          "    '  [' + column_name + '] ' + \n" +
          "    data_type + CASE data_type\n" +
          "        WHEN 'sql_variant' THEN ''\n" +
          "        WHEN 'text' THEN ''\n" +
          "        WHEN 'ntext' THEN ''\n" +
          "        WHEN 'xml' THEN ''\n" +
          "        WHEN 'decimal' THEN '(' + CAST(numeric_precision AS VARCHAR) + ', ' + CAST(numeric_scale AS VARCHAR) + ')'\n" +
          "        ELSE COALESCE('(' + CASE WHEN character_maximum_length = -1 THEN 'MAX' ELSE CAST(character_maximum_length AS VARCHAR) END + ')', '') END + ' ' +\n" +
          "    CASE WHEN EXISTS ( \n" +
          "        SELECT id FROM syscolumns\n" +
          "        WHERE OBJECT_NAME(id) = so.name\n" +
          "        AND name = column_name\n" +
          "        AND COLUMNPROPERTY(id, name, 'IsIdentity') = 1 \n" +
          "    ) THEN\n" +
          "        'IDENTITY(' + \n" +
          "        CAST(ident_seed(so.name) AS VARCHAR) + ',' + \n" +
          "        CAST(ident_incr(so.name) AS VARCHAR) + ')'\n" +
          "    ELSE ''\n" +
          "    END + ' ' +\n" +
          "    (CASE WHEN IS_NULLABLE = 'No' THEN 'NOT ' ELSE '' END) + 'NULL ' + \n" +
          "    CASE WHEN information_schema.columns.COLUMN_DEFAULT IS NOT NULL THEN 'DEFAULT ' + information_schema.columns.COLUMN_DEFAULT ELSE '' END + ', ' \n" +
          " FROM information_schema.columns WHERE table_schema = @schemaname AND table_name = so.name\n" +
          " ORDER BY ordinal_position\n" +
          " FOR XML PATH('')) o (list)\n" +
          " LEFT JOIN\n" +
          "     information_schema.table_constraints tc\n" +
          " ON  tc.Table_name       = so.Name\n" +
          " AND tc.Constraint_Type  = 'PRIMARY KEY'\n" +
          " CROSS APPLY\n" +
          " (SELECT '[' + Column_Name + '], '\n" +
          " FROM   information_schema.key_column_usage kcu\n" +
          " WHERE  kcu.Constraint_Name = tc.Constraint_Name\n" +
          " ORDER BY\n" +
          "    ORDINAL_POSITION\n" +
          " FOR XML PATH('')) j (list)\n" +
          " WHERE   xtype = 'U'\n" +
          " AND name = @tabname;";

  /**
   * 获取ddl
   */
  public static final String SELECT_DDL_SQL = "SELECT (\n"
          + " CASE WHEN (\n"
          + "     SELECT COUNT(a.constraint_type)\n"
          + "     FROM information_schema.table_constraints a \n"
          + "     INNER JOIN information_schema.constraint_column_usage b\n"
          + "     ON a.constraint_name = b.constraint_name\n"
          + "     WHERE a.constraint_type = 'PRIMARY KEY' \n"
          + "     AND a.CONSTRAINT_SCHEMA = '%s'\n"
          + "     AND a.table_name = '%s'\n"
          + " ) = 1 THEN\n"
          + "     REPLACE(table_ddl, ', )ALTER TABLE', ') ALTER TABLE')\n"
          + " ELSE \n"
          + "     SUBSTRING(table_ddl, 1, LEN(table_ddl) - 3) + ')'\n"
          + " END\n"
          + ") AS createTableStatement\n"
          + " FROM #t;";

  private SQLServerConst() {

  }
}
