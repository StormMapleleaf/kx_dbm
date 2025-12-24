package org.dromara.dbswitch.common.util;

import org.dromara.dbswitch.common.entity.PatternMapper;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;


@UtilityClass
public final class PatterNameUtils {

  public static String getFinalName(String originalName, List<PatternMapper> patternMappers) {
    if (null == originalName) {
      return null;
    }

    String targetName = originalName;
    if (null != patternMappers && !patternMappers.isEmpty()) {
      for (PatternMapper mapper : patternMappers) {
        String fromPattern = mapper.getFromPattern();
        String toValue = mapper.getToValue();
        if (null == fromPattern) {
          continue;
        }
        if (null == toValue) {
          toValue = "";
        }
        targetName = targetName.replaceAll(fromPattern, toValue);
      }
    }
    return targetName;
  }


  public static void main(String[] args) {
    System.out.println(getFinalName(
        "hello",
        Arrays.asList(new PatternMapper("^", "T_"), new PatternMapper("$", "_Z")))
    );

    System.out.println(getFinalName(
        "hello",
        Arrays.asList(new PatternMapper("hello", "new_hello")))
    );

    System.out.println(getFinalName(
        "test",
        Arrays.asList(new PatternMapper("hello", "new_hello")))
    );
  }

}
