package org.dromara.dbswitch.common.util;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class UuidUtils {

  public static String generateUuid() {
    return UUID.randomUUID().toString().replace("-", "");
  }

}
