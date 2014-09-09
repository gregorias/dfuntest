package org.nebulostore.dfuntesting;

public abstract class App {
  private final int mId;
  private final String mName;

  public App(int id, String name) {
    mId = id;
    mName = name;
  }

  public int getId() {
    return mId;
  }

  public String getName() {
    return mName;
  }

  public abstract boolean isRunning();

  public abstract boolean isWorking();

  public abstract void run() throws Exception;

  public abstract void shutDown() throws Exception;
}
