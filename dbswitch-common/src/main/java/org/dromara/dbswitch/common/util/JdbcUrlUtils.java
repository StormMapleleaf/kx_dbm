package org.dromara.dbswitch.common.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;


@UtilityClass
public final class JdbcUrlUtils {

  public static final String PROP_HOST = "host"; 
  public static final String PROP_PORT = "port"; 
  public static final String PROP_DATABASE = "database"; 
  public static final String PROP_SERVER = "server"; 
  public static final String PROP_PARAMS = "params";
  public static final String PROP_FOLDER = "folder"; 
  public static final String PROP_FILE = "file"; 
  public static final String PROP_USER = "user"; 
  public static final String PROP_PASSWORD = "password"; 

  private static String getPropertyRegex(String property) {
    switch (property) {
      case PROP_FOLDER:
      case PROP_FILE:
      case PROP_PARAMS:
        return ".+?";
      default:
        return "[\\\\w\\\\-_.~]+";
    }
  }

  private static String replaceAll(String input, String regex, Function<Matcher, String> replacer) {
    final Matcher matcher = Pattern.compile(regex).matcher(input);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, replacer.apply(matcher));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  public static Pattern getPattern(String sampleUrl) {
    String pattern = sampleUrl;
    pattern = replaceAll(pattern, "\\[(.*?)]", m -> "\\\\E(?:\\\\Q" + m.group(1) + "\\\\E)?\\\\Q");
    pattern = replaceAll(pattern, "\\{(.*?)}",
        m -> "\\\\E(\\?<\\\\Q" + m.group(1) + "\\\\E>" + getPropertyRegex(m.group(1)) + ")\\\\Q");
    pattern = "^\\Q" + pattern + "\\E$";
    return Pattern.compile(pattern);
  }


  public static boolean reachable(String host, String port) {
    try {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress(host, Integer.parseInt(port)), 1500);
      }
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  public static String getTemplateUrl(String url) {
    return url.replaceAll("\\[(\\?|;|:)\\{params}\\]|\\[|\\]", "")
        .replace("\\?{params}", "");
  }


  public static void main(String[] args) {
    final Matcher matcher0 = JdbcUrlUtils
        .getPattern("jdbc:teradata://{host}/DATABASE={database},DBS_PORT={port}[,{params}]")
        .matcher(
            "jdbc:teradata://localhost/DATABASE=test,DBS_PORT=1234,CLIENT_CHARSET=EUC_CN,TMODE=TERA,CHARSET=ASCII,LOB_SUPPORT=true");
    if (matcher0.matches()) {
      System.out.println("teradata host:" + matcher0.group("host"));
      System.out.println("teradata port:" + matcher0.group("port"));
      System.out.println("teradata database:" + matcher0.group("database"));
      String params = matcher0.group("params");
      if (null != params) {
        String[] pairs = params.split(",");
        for (String pair : pairs) {
          System.out.println("teradata params:" + pair);
        }
      }
    } else {
      System.out.println("error for teradata!");
    }

    final Matcher matcher1 = JdbcUrlUtils
        .getPattern("jdbc:postgresql://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:postgresql://localhost:5432/dvdrental?currentSchema=test&ssl=true");
    if (matcher1.matches()) {
      System.out.println("postgresql host:" + matcher1.group("host"));
      System.out.println("postgresql port:" + matcher1.group("port"));
      System.out.println("postgresql database:" + matcher1.group("database"));
      String params = matcher1.group("params");
      if (null != params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
          System.out.println("postgresql params:" + pair);
        }
      }
    } else {
      System.out.println("error for postgresql!");
    }

    final Matcher matcher2 = JdbcUrlUtils.getPattern("jdbc:oracle:thin:@{host}[:{port}]:{sid}")
        .matcher("jdbc:oracle:thin:@localhost:1521:orcl");
    if (matcher2.matches()) {
      System.out.println("oracle sid host:" + matcher2.group("host"));
      System.out.println("oracle sid port:" + matcher2.group("port"));
      System.out.println("oracle sid name:" + matcher2.group("sid"));
    } else {
      System.out.println("error for oracle sid!");
    }

    final Matcher matcher2_1 = JdbcUrlUtils.getPattern("jdbc:oracle:thin:@//{host}[:{port}]/{name}")
        .matcher("jdbc:oracle:thin:@//localhost:1521/orcl.city.com");
    if (matcher2_1.matches()) {
      System.out.println("oracle ServiceName host:" + matcher2_1.group("host"));
      System.out.println("oracle ServiceName port:" + matcher2_1.group("port"));
      System.out.println("oracle ServiceName name:" + matcher2_1.group("name"));
    } else {
      System.out.println("error for oracle ServiceName!");
    }

    final Matcher matcher3 = JdbcUrlUtils
        .getPattern("jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:mysql://localhost:3306/test_demo?useUnicode=true&useSSL=false");
    if (matcher3.matches()) {
      System.out.println("mysql host:" + matcher3.group("host"));
      System.out.println("mysql port:" + matcher3.group("port"));
      System.out.println("mysql database:" + matcher3.group("database"));
      String params = matcher3.group("params");
      if (null != params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
          System.out.println("mysql params:" + pair);
        }
      }
    } else {
      System.out.println("error for mysql!");
    }

    final Matcher matcher4 = JdbcUrlUtils
        .getPattern("jdbc:mariadb://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:mariadb://localhost:3306/test_demo");
    if (matcher4.matches()) {
      System.out.println("mariadb host:" + matcher4.group("host"));
      System.out.println("mariadb port:" + matcher4.group("port"));
      System.out.println("mariadb database:" + matcher4.group("database"));
      String params = matcher4.group("params");
      if (null != params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
          System.out.println("mysql params:" + pair);
        }
      }
    } else {
      System.out.println("error for mariadb!");
    }

    final Matcher matcher5 = JdbcUrlUtils
        .getPattern("jdbc:sqlserver://{host}[:{port}][;DatabaseName={database}][;{params}]")
        .matcher("jdbc:sqlserver://localhost:1433;DatabaseName=master;user=MyUserName");
    if (matcher5.matches()) {
      System.out.println("sqlserver host:" + matcher5.group("host"));
      System.out.println("sqlserver port:" + matcher5.group("port"));
      System.out.println("sqlserver database:" + matcher5.group("database"));
      String params = matcher5.group("params");
      if (null != params) {
        String[] pairs = params.split(";");
        for (String pair : pairs) {
          System.out.println("sqlserver params:" + pair);
        }
      }
    } else {
      System.out.println("error for sqlserver!");
    }

    final Matcher matcher6 = JdbcUrlUtils
        .getPattern("jdbc:kingbase8://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:kingbase8://localhost:54321/sample");
    if (matcher6.matches()) {
      System.out.println("kingbase8 host:" + matcher6.group("host"));
      System.out.println("kingbase8 port:" + matcher6.group("port"));
      System.out.println("kingbase8 database:" + matcher6.group("database"));
      String params = matcher6.group("params");
      if (null != params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
          System.out.println("mysql params:" + pair);
        }
      }
    } else {
      System.out.println("error for kingbase8!");
    }

    final Matcher matcher7 = JdbcUrlUtils.getPattern("jdbc:dm://{host}:{port}[/{database}][\\?{params}]")
        .matcher("jdbc:dm://localhost:5236");
    if (matcher7.matches()) {
      System.out.println("dm host:" + matcher7.group("host"));
      System.out.println("dm port:" + matcher7.group("port"));
      System.out.println("dm database:" + matcher7.group("database"));
      String params = matcher7.group("params");
      if (null != params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
          System.out.println("dm params:" + pair);
        }
      }
    } else {
      System.out.println("error for dm!");
    }

    final Matcher matcher8 = JdbcUrlUtils.getPattern("jdbc:db2://{host}:{port}/{database}[:{params}]")
        .matcher("jdbc:db2://localhost:50000/testdb:driverType=4;fullyMaterializeLobData=true");
    if (matcher8.matches()) {
      System.out.println("db2 host:" + matcher8.group("host"));
      System.out.println("db2 port:" + matcher8.group("port"));
      System.out.println("db2 database:" + matcher8.group("database"));
      String params = matcher8.group("params");
      if (null != params) {
        String[] pairs = params.split(";");
        for (String pair : pairs) {
          System.out.println("mysql params:" + pair);
        }
      }
    } else {
      System.out.println("error for db2!");
    }

    final Matcher matcher9 = JdbcUrlUtils
        .getPattern("jdbc:hive2://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:hive2://127.0.0.1:10000/default?useUnicode=true&useSSL=false");
    if (matcher9.matches()) {
      System.out.println("hive host:" + matcher3.group("host"));
      System.out.println("hive port:" + matcher3.group("port"));
      System.out.println("hive database:" + matcher3.group("database"));
      String params = matcher9.group("params");
      if (null != params) {
        String[] pairs = params.split("&");
        for (String pair : pairs) {
          System.out.println("mysql params:" + pair);
        }
      }
    } else {
      System.out.println("error for hive!");
    }

    final Matcher matcher10 = JdbcUrlUtils.getPattern("jdbc:sqlite:{file}")
        .matcher("jdbc:sqlite:D:\\Project\\Test\\phone.db");
    if (matcher10.matches()) {
      System.out.println("sqlite file:" + matcher10.group("file"));
    } else {
      System.out.println("error for sqlite!");
    }

    final Matcher matcher11 = JdbcUrlUtils.getPattern("jdbc:mongodb://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:mongodb://127.0.0.1:27017/admin?authSource=admin&authMechanism=SCRAM-SHA-1");
    if (matcher11.matches()) {
      System.out.println("mongodb database:" + matcher11.group("database"));
    } else {
      System.out.println("error for mongodb!");
    }

    final Matcher matcher12 = JdbcUrlUtils.getPattern("jdbc:clickhouse://{host}[:{port}]/[{database}][\\?{params}]")
        .matcher("jdbc:clickhouse://127.0.0.1:8123/default");
    if (matcher12.matches()) {
      System.out.println("clickhouse database:" + matcher12.group("database"));
    } else {
      System.out.println("error for clickhouse!");
    }
  }

}
