// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.mongodb;

import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.TableDescription;
import com.google.common.collect.Sets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongodbMetadataQueryProvider extends AbstractMetadataProvider {

  private static final Set<String> systemSchemas = Sets.newHashSet("admin", "config", "local");

  public MongodbMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    List<String> catalogs = new ArrayList<String>();
    try (ResultSet rs = connection.getMetaData().getCatalogs()) {
      while (rs.next()) {
        String name = rs.getString("TABLE_CAT");
        if (!systemSchemas.contains(name)) {
          catalogs.add(name);
        }
      }
      return catalogs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> ret = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData().getTables(schemaName, null, null, null)) {
      while (rs.next()) {
        String tableName = rs.getString("TABLE_NAME");
        TableDescription td = new TableDescription();
        td.setSchemaName(schemaName);
        td.setTableName(tableName);
        td.setRemarks(rs.getString("REMARKS"));
        td.setTableType(rs.getString("TABLE_TYPE").toUpperCase());
        ret.add(td);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  public TableDescription queryTableMeta(Connection connection, String schemaName, String tableName) {
    try (ResultSet rs = connection.getMetaData()
        .getTables(schemaName, null, tableName, new String[]{"TABLE"})) {
      if (rs.next()) {
        TableDescription td = new TableDescription();
        td.setSchemaName(schemaName);
        td.setTableName(tableName);
        td.setRemarks(rs.getString("REMARKS"));
        td.setTableType(rs.getString("TABLE_TYPE").toUpperCase());
        return td;
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    return null;
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    return null;
  }

  @Override
  public List<String> queryTableColumnName(Connection connection, String schemaName, String tableName) {
    List<String> ret = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData().getColumns(schemaName, null, tableName, null)) {
      while (rs.next()) {
        ret.add(rs.getString("COLUMN_NAME"));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName) {
    List<ColumnDescription> ret = new ArrayList<>();
    String sql = String.format("%s.getCollection('%s').find().limit(1);", schemaName, tableName);
    try (Statement stmt = connection.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(sql)) {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          String name = metaData.getColumnName(i);
          int jdbcType = metaData.getColumnType(i);
          int displaySize = ("_id".equals(name)) ? 128 : 0;
          if (Types.JAVA_OBJECT == jdbcType) {
            jdbcType = Types.LONGVARCHAR;
            displaySize = Constants.CLOB_LENGTH;
          }

          ColumnDescription cd = new ColumnDescription();
          cd.setFieldName(name);
          cd.setLabelName(name);
          cd.setFieldType(jdbcType);
          cd.setFieldTypeName(metaData.getColumnTypeName(i));
          cd.setFiledTypeClassName(metaData.getColumnTypeName(i));
          cd.setDisplaySize(displaySize);
          cd.setPrecisionSize(0);
          cd.setScaleSize(0);
          cd.setAutoIncrement(false);
          cd.setNullable(!"_id".equals(cd.getFieldName()));
          cd.setProductType(getProductType());
          ret.add(cd);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    return Collections.emptyList();
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName, String tableName) {
    List<String> ret = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData().getPrimaryKeys(schemaName, null, tableName)) {
      while (rs.next()) {
        ret.add(rs.getString("COLUMN_NAME"));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  public List<IndexDescription> queryTableIndexes(Connection connection, String schemaName, String tableName) {
    return Collections.emptyList();
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {

  }

  @Override
  public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
    return String.format("%s.%s", schemaName, tableName);
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr,
      boolean withRemarks) {
    return null;
  }

  @Override
  public String getPrimaryKeyAsString(List<String> pks) {
    return null;
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td, List<ColumnDescription> cds) {
    return Collections.emptyList();
  }

}
