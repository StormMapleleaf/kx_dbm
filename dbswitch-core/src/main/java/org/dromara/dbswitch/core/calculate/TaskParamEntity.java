package org.dromara.dbswitch.core.calculate;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.provider.transform.RecordTransformProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


@Data
@Builder
@AllArgsConstructor
public class TaskParamEntity {


  @NonNull
  private DataSource oldDataSource;


  @NonNull
  private String oldSchemaName;


  @NonNull
  private String oldTableName;


  @NonNull
  private ProductTypeEnum oldProductType;
  

  @NonNull
  private DataSource newDataSource;


  @NonNull
  private String newSchemaName;


  @NonNull
  private String newTableName;

  @NonNull
  private ProductTypeEnum newProductType;

  private List<String> fieldColumns;

  @NonNull
  @Builder.Default
  private Map<String, String> columnsMap = Collections.emptyMap();

  @NonNull
  private RecordTransformProvider transformer;
}
