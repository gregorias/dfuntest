package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Base implementation for Example's test scripts which adds start up and shut down functionality.
 */
public abstract class AbstractExampleTestScript implements TestScript<ExampleApp> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExampleTestScript.class);

  protected void startUpApps(Collection<ExampleApp> apps) throws CommandException, IOException {
    Collection<ExampleApp> startedApps = new ArrayList<>();
    for (ExampleApp app : apps) {
      try {
        app.startUp();
        startedApps.add(app);
      } catch (CommandException | IOException e) {
        LOGGER.error("run(): Could not start app {}.", app.getId(), e);
        shutDownApps(startedApps);
        throw e;
      }
    }
  }

  /**
   * Shuts down applications.
   *
   * @param apps Applications to shut down
   * @return true iff every app was shut down correctly.
   */
  protected boolean shutDownApps(Collection<ExampleApp> apps) {
    boolean hasAnAppFailed = false;
    for (ExampleApp app : apps) {
      try {
        app.shutDown();
      } catch (IOException | InterruptedException e) {
        hasAnAppFailed = true;
        LOGGER.warn("run(): Could not shut down app {}.", app.getId(), e);
      }
    }
    return !hasAnAppFailed;
  }
}
