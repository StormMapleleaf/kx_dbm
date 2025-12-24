package org.dromara.dbswitch.core.util;

import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.type.ProductTableEnum;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.common.util.DDLFormatterUtils;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.TableDescription;
import org.dromara.dbswitch.core.schema.SourceProperties;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

@UtilityClass
public final class GenerateSqlUtils {

  public static String getDDLCreateTableSQL(
      MetadataProvider provider,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      boolean autoIncr) {
    return getDDLCreateTableSQL(
        provider,
        fieldNames,
        primaryKeys,
        schemaName,
        tableName,
        false,
        null,
        autoIncr,
        null);
  }

  public static String getDDLCreateTableSQL(
      MetadataProvider provider,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      boolean withRemarks,
      String tableRemarks,
      boolean autoIncr,
      SourceProperties tblProperties) {
    ProductTypeEnum type = provider.getProductType();
    StringBuilder sb = new StringBuilder();
    Set<String> fieldNameSets = fieldNames.stream()
        .map(ColumnDescription::getFieldName)
        .collect(Collectors.toSet());
    List<String> pks = primaryKeys.stream()
        .filter(fieldNameSets::contains)
        .collect(Collectors.toList());

    sb.append(Constants.CREATE_TABLE);
    provider.preAppendCreateTableSql(sb);
    sb.append(provider.getQuotedSchemaTableCombination(schemaName, tableName));
    sb.append("(");

    if (type.isPrimaryKeyShouldAtFirst()) {
      List<ColumnDescription> copyFieldNames = new ArrayList<>();
      List<String> copyPrimaryKeys = new ArrayList<>();
      Integer fieldIndex = 0;
      for (int i = 0; i < fieldNames.size(); i++) {
        ColumnDescription cd = fieldNames.get(i);
        if (primaryKeys.contains(cd.getFieldName())) {
          copyFieldNames.add(fieldIndex++, cd);
          copyPrimaryKeys.add(cd.getFieldName());
        } else {
          copyFieldNames.add(cd);
        }
      }
      fieldNames = copyFieldNames;
      pks = copyPrimaryKeys;
    }

    for (int i = 0; i < fieldNames.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      } else {
        sb.append("  ");
      }

      ColumnMetaData v = fieldNames.get(i).getMetaData();
      sb.append(provider.getFieldDefinition(v, pks, autoIncr, false, withRemarks));
    }

    if (type.isParenthesisBefore()) {
      sb.append(")");
      provider.appendPrimaryKeyForCreateTableSql(sb, pks);
    } else {
      provider.appendPrimaryKeyForCreateTableSql(sb, pks);
      sb.append(")");
    }

    provider.postAppendCreateTableSql(sb, tableRemarks, pks, tblProperties);

    return DDLFormatterUtils.format(sb.toString());
  }

  public static List<String> getDDLCreateTableSQL(
      MetadataProvider provider,
      List<ColumnDescription> fieldNames,
      List<String> primaryKeys,
      String schemaName,
      String tableName,
      String tableRemarks,
      boolean autoIncr,
      SourceProperties tblProperties) {
    ProductTypeEnum productType = provider.getProductType();
    if (productType.isLikeHive()) {
      return provider.getCreateTableSqlList(
          fieldNames, primaryKeys, schemaName, tableName, tableRemarks, autoIncr, tblProperties);
    } else if (productType.noCommentStatement()) {
      String createTableSql = getDDLCreateTableSQL(provider, fieldNames, primaryKeys, schemaName,
          tableName, true, tableRemarks, autoIncr, tblProperties);
      return Arrays.asList(createTableSql);
    } else {
      String createTableSql = getDDLCreateTableSQL(provider, fieldNames, primaryKeys, schemaName,
          tableName, true, tableRemarks, autoIncr, tblProperties);
      TableDescription td = new TableDescription();
      td.setSchemaName(schemaName);
      td.setTableName(tableName);
      td.setRemarks(tableRemarks);
      td.setTableType(ProductTableEnum.TABLE.name());
      List<String> results = provider.getTableColumnCommentDefinition(td, fieldNames);
      if (CollectionUtils.isEmpty(results)) {
        results = Lists.newArrayList(createTableSql);
      } else {
        results.add(0, createTableSql);
      }
      return results;
    }
  }

}
