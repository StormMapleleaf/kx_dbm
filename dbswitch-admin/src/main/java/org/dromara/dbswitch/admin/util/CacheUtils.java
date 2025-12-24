package org.dromara.dbswitch.admin.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class CacheUtils {

  public static final long CACHE_DURATION_SECONDS = 7200;

  private static Cache<String, Object> loadingCache = CacheBuilder.newBuilder()
      .initialCapacity(10)
      .maximumSize(1000)
      .recordStats()
      .concurrencyLevel(8)
      .expireAfterAccess(CACHE_DURATION_SECONDS, TimeUnit.SECONDS)
      .build();

  public static void put(String key, Object value) {
    loadingCache.put(key, value);
  }

  public static Object get(String key) {
    return loadingCache.getIfPresent(key);
  }

  public static void remove(String key) {
    loadingCache.invalidate(key);
  }

  public static void clear() {
    loadingCache.invalidateAll();
  }

  public static Map<String, Object> getAll() {
    return loadingCache.asMap();
  }

  public static Collection<Object> getAllValue() {
    return loadingCache.asMap().values();
  }

}