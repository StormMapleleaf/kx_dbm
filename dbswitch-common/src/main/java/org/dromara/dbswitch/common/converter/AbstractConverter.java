package org.dromara.dbswitch.common.converter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractConverter<U, V> implements Converter<U, V> {

  public List<V> convert(List<U> inputList) {
    if (null == inputList) {
      return null;
    }

    return inputList.stream()
        .map(u -> convert(u))
        .collect(Collectors.toList());
  }

  public Set<V> convert(Set<U> inputSet) {
    if (null == inputSet) {
      return null;
    }

    return inputSet.stream()
        .map(u -> convert(u))
        .collect(Collectors.toSet());
  }

}
