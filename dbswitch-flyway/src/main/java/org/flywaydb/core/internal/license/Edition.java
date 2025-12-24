package org.flywaydb.core.internal.license;

public enum Edition {
  COMMUNITY("Community"),
  PRO("Pro"),
  ENTERPRISE("Enterprise");

  private final String description;

  Edition(String name) {
    this.description = "Flyway " + name + " Edition";
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return description;
  }
}