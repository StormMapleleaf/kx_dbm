package org.dromara.dbswitch.core.provider.transform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SchemaTableColumnTuple {

  private String schema;
  private String table;
  private String column;
}
