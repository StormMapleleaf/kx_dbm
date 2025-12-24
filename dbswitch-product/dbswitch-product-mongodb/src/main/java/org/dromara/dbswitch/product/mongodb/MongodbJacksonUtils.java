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

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class MongodbJacksonUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
    objectMapper.registerModule(createSimpleModule());
  }

  private static SimpleModule createSimpleModule() {
    SimpleModule module = new SimpleModule();

    module.addSerializer(Date.class, new StdSerializer<Date>(Date.class) {

      @Override
      public void serialize(Date value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
          throws IOException {
        if (value != null) {
          SimpleDateFormat sdf = new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN);
          jsonGenerator.writeString(sdf.format(value));
        }
      }
    });

    module.addSerializer(LocalDate.class, new StdSerializer<LocalDate>(LocalDate.class) {

      @Override
      public void serialize(LocalDate value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
          throws IOException {
        if (value != null) {
          SimpleDateFormat sdf = new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN);
          jsonGenerator.writeString(sdf.format(value));
        }
      }
    });

    module.addSerializer(Time.class, new StdSerializer<Time>(Time.class) {

      @Override
      public void serialize(Time value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
          throws IOException {
        if (value != null) {
          SimpleDateFormat sdf = new SimpleDateFormat(DatePattern.NORM_TIME_PATTERN);
          jsonGenerator.writeString(sdf.format(value));
        }
      }
    });

    module.addSerializer(Timestamp.class, new StdSerializer<Timestamp>(Timestamp.class) {

      @Override
      public void serialize(Timestamp value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
          throws IOException {
        if (value != null) {
          SimpleDateFormat sdf = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
          jsonGenerator.writeString(sdf.format(value));
        }
      }
    });

    module.addSerializer(LocalDateTime.class, new StdSerializer<LocalDateTime>(LocalDateTime.class) {

      @Override
      public void serialize(LocalDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
          throws IOException {
        if (value != null) {
          SimpleDateFormat sdf = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
          jsonGenerator.writeString(sdf.format(value));
        }
      }
    });

    return module;
  }

  public static String toJsonStr(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
