package org.dromara.dbswitch.admin.model.ops;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpsAssignmentTaskJob {

  private Long assignmentId;

  private List<Long> jobIds;
}
