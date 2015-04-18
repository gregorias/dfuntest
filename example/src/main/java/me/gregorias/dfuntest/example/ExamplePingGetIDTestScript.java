package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This TestScript checks whether applications will return all ids that were pinged to them.
 */
public class ExamplePingGetIDTestScript extends AbstractExampleTestScript {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplePingGetIDTestScript.class);
  private static final int PING_ID = 1087;

  @Override
  public TestResult run(Collection<ExampleApp> apps) {
    LOGGER.info("run()");
    try {
      startUpApps(apps);
    } catch (CommandException | IOException e) {
      return new TestResult(TestResult.Type.FAILURE, "An app could not be started.");
    }

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    TestResult result = new TestResult(TestResult.Type.SUCCESS, "Test was successful.");
    try {
      for (ExampleApp app : apps) {
        LOGGER.info("run(): Testing app {}", app.getId());
        try {
          app.ping(PING_ID);
          Set<Integer> pingedIDs = new HashSet<>(app.getPingedIDs());

          if (!pingedIDs.contains(PING_ID)) {
            return new TestResult(TestResult.Type.FAILURE, String.format(
                "%d app did not return expected ping: %d.", app.getId(), PING_ID));
          }
        } catch (IOException e) {
          String errorMsg = String.format("%d app did not return valid pings due to IOException.",
              app.getId());
          LOGGER.error("run(): " + errorMsg, e);
          return new TestResult(TestResult.Type.FAILURE, errorMsg);
        }
      }
    } finally {
      shutDownApps(apps);
    }
    return result;
  }

  @Override
  public String toString() {
    return "PingGetIDTestScript";
  }
}
