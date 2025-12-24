// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.common.event;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import lombok.Getter;

@Getter
public class ListenedEvent extends java.util.EventObject {

  private String identifier;
  private Object[] args;

  public ListenedEvent(Object source, String identifier, Object... args) {
    super(source);
    this.identifier = identifier;
    this.args = args;
  }

  public void checkArgs(Class<?>... classes) throws IllegalArgumentException {
    Preconditions.checkArgument(this.args.length == classes.length,
        "The args count of event '%s' should be %s(actual %s)",
        this.identifier, classes.length, this.args.length);
    int i = 0;
    for (Class<?> c : classes) {
      Object arg = this.args[i++];
      if (arg == null) {
        continue;
      }
      Preconditions.checkArgument(c.isAssignableFrom(arg.getClass()),
          "The arg '%s'(%s) can't match %s",
          arg, arg.getClass(), c);
    }
  }

  @Override
  public String toString() {
    return String.format("Event{identifier='%s', args=%s}",
        this.identifier, Arrays.asList(this.args));
  }
}