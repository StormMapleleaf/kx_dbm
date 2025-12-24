package org.flywaydb.core.internal.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.FileCopyUtils;

public class VersionPrinter {

  private static final Log LOG = LogFactory.getLog(VersionPrinter.class);
  private static final String version = readVersion();
  private static boolean printed;

  public static final Edition EDITION = Edition.COMMUNITY;

    private VersionPrinter() {
  }

  public static String getVersion() {
    return version;
  }

    public static void printVersion() {
    if (printed) {
      return;
    }
    printed = true;

    printVersionOnly();
  }

  public static void printVersionOnly() {
    LOG.info(EDITION + " " + version + " by Redgate");
  }

  private static String readVersion() {
    try {
      return FileCopyUtils.copyToString(
          VersionPrinter.class.getClassLoader().getResourceAsStream("org/flywaydb/core/internal/version.txt"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new FlywayException("Unable to read Flyway version: " + e.getMessage(), e);
    }
  }
}