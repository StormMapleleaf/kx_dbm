package org.dromara.dbswitch.common.type;

import cn.hutool.core.util.StrUtil;

public enum CaseConvertEnum {

  NONE(s -> s),

  UPPER(String::toUpperCase),

  LOWER(String::toLowerCase),

  SNAKE(StrUtil::toUnderlineCase),

  CAMEL(StrUtil::toCamelCase);

  private Converter function;

  CaseConvertEnum(Converter function) {
    this.function = function;
  }

  public String convert(String name) {
    return function.convert(name);
  }

  interface Converter {

    String convert(String s);
  }

}
