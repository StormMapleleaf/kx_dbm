package org.dromara.dbswitch.product.sr;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dromara.dbswitch.common.entity.CloseableDataSource;

@Slf4j
public final class StarRocksUtils {

  private String dbName;
  private String tbName;
  private String host;
  private String username;
  private String password;
  private CloseableDataSource dataSource;
  private String httpPort;

  public void init(String schemaName, String tableName, DataSource dataSource) {
    this.getHttpPort(dataSource);
    this.dataSource = (CloseableDataSource) dataSource;
    this.host = ReUtil.extractMulti("jdbc:mysql://(.*):[0-9]{2,8}/", this.dataSource.getJdbcUrl(), "$1");
    this.username = this.dataSource.getUserName();
    this.password = this.dataSource.getPassword();
    this.tbName = tableName;
    this.dbName = schemaName;
  }

  public void getHttpPort(DataSource dataSource) {
    Db use = Db.use(dataSource);
    try {
      List<Entity> frontends = use.query("SHOW FRONTENDS");
      List<FrontendEntity> frontendEntities = BeanUtil.copyToList(frontends, FrontendEntity.class);
      List<FrontendEntity> leader = frontendEntities.stream().filter(i -> i.getRole().equals("LEADER"))
          .collect(Collectors.toList());
      FrontendEntity frontendEntity = leader.get(0);
      this.httpPort = frontendEntity.getHttpport();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public long addOrUpdateData(List<String> fieldNames, List<Object[]> recordValues) {
    if (CollectionUtils.isEmpty(fieldNames) || CollectionUtils.isEmpty(recordValues)) {
      return 0L;
    }
    List<Object> objectList = asObjectList(fieldNames, recordValues);
    JSONArray array = JSONUtil.parseArray(objectList);
    JSONObject jsonObject = JSONUtil.createObj().set("data", array);
    try {
      sendData(jsonObject.toString());
      return recordValues.size();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void sendData(String content) throws Exception {
    final String loadUrl = String.format("http://%s:%s/api/%s/%s/_stream_load",
        this.host,
        this.httpPort,
        this.dbName,
        this.tbName);

    final HttpClientBuilder httpClientBuilder = HttpClients
        .custom()
        .setRedirectStrategy(new DefaultRedirectStrategy() {
          @Override
          protected boolean isRedirectable(String method) {
            return true;
          }
        });
    try (CloseableHttpClient client = httpClientBuilder.build()) {
      HttpPut put = new HttpPut(loadUrl);
      StringEntity entity = new StringEntity(content, "UTF-8");
      put.setHeader(HttpHeaders.EXPECT, "100-continue");
      put.setHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader(this.username, this.password));
      put.setHeader("strip_outer_array", "true");
      put.setHeader("format", "JSON");
      put.setHeader("json_root", "$.data");
      put.setHeader("ignore_json_size", "true");
      put.setHeader("Content-Type", "application/json");
      put.setEntity(entity);
      try (CloseableHttpResponse response = client.execute(put)) {
        String loadResult = "";
        if (response.getEntity() != null) {
          loadResult = EntityUtils.toString(response.getEntity());
        }
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
          throw new IOException(
              String.format("Stream load failed, statusCode=%s load result=%s", statusCode, loadResult));
        }
      }
    }
  }

  private String basicAuthHeader(String username, String password) {
    final String tobeEncode = username + ":" + password;
    byte[] encoded = Base64.getEncoder().encode(tobeEncode.getBytes(StandardCharsets.UTF_8));
    return "Basic " + new String(encoded);
  }

  private List<Object> asObjectList(List<String> fieldNames, List<Object[]> recordValues) {
    int fieldCount = Math.min(fieldNames.size(), recordValues.get(0).length);
    List<Object> rows = new ArrayList<>(recordValues.size());
    for (Object[] row : recordValues) {
      Map<String, Object> columns = new LinkedHashMap<>(fieldCount);
      for (int i = 0; i < fieldCount; ++i) {
        Object rowValue = row[i];
        if (row[i] instanceof Timestamp) {
          rowValue = String.valueOf(rowValue);
        }
        columns.put(fieldNames.get(i), rowValue);
      }
      rows.add(columns);
    }
    return rows;
  }
}
