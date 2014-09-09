package org.nebulostore.dfuntesting;

public class TestResult {
  private final Type mType;
  private final String mDescription;

  public TestResult(Type type, String description) {
    mType = type;
    mDescription = description;
  }

  public Type getType() {
    return mType;
  }

  public String getDescription() {
    return mDescription;
  }

  public enum Type {
    SUCCESS, FAILURE
  };
}
