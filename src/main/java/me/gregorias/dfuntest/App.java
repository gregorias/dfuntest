package me.gregorias.dfuntest;

import java.io.IOException;

/**
 * This type represents tested applications.
 *
 * It acts as proxy to a real, possibly remote, application.
 *
 * @author Grzegorz Milka
 *
 */
public abstract class App<EnvironmentT extends Environment> {
  private final int mId;
  private final String mName;

  public App(int id, String name) {
    mId = id;
    mName = name;
  }

  /**
   * @return Underlying environment
   */
  public abstract EnvironmentT getEnvironment();

  /**
   * @return Number identifying this application.
   */
  public int getId() {
    return mId;
  }

  /**
   * @return Human readable name of this application.
   */
  public String getName() {
    return mName;
  }

  /**
   * Starts the application and allows it to run in background.
   */
  @SuppressWarnings("unused")
  public abstract void startUp() throws CommandException, IOException;

  /**
   * Shuts down started application and deallocates all resources associated
   * with running application.
   */
  @SuppressWarnings("unused")
  public abstract void shutDown() throws IOException, InterruptedException;
}
