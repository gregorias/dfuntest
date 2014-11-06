package me.gregorias.dfuntest;

public class TestResult {
  private final Type mType;
  private final String mDescription;

  public enum Type {
    SUCCESS, FAILURE
  };

  public TestResult(Type type, String description) {
    mType = type;
    mDescription = description;
  }

  public String getDescription() {
    return mDescription;
  }

  public Type getType() {
    return mType;
  }
}
