package org.dromara.dbswitch.common.consts;

public final class Constants {

  public static final String FILE_SEPARATOR = System.getProperty("file.separator");

  public static final String PATH_SEPARATOR = System.getProperty("path.separator");

  public static final String CR = System.getProperty("line.separator");

  public static final String DOSCR = "\n\r";

  public static final String EMPTY_STRING = "";

  public static final String JAVA_VERSION = System.getProperty("java.vm.version");

  public static final String CREATE_TABLE = "CREATE TABLE ";

  public static final String DROP_TABLE = "DROP TABLE ";

  public static final String IF_NOT_EXISTS = "IF NOT EXISTS ";

  public static final String IF_EXISTS = "IF EXISTS ";

  public static final int CLOB_LENGTH = 9999999;

  public static Integer DEFAULT_QUERY_TIMEOUT_SECONDS = 1 * 60 * 60;

  public static int DEFAULT_FETCH_SIZE = 1000;

  public static int MINIMUM_FETCH_SIZE = 100;

  public static final String SPI_FILE = "META-INF/services/dbswitch.providers";

  public static final String DISTRIBUTED_KEY = "distributed_key_";
}
