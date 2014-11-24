package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestResult;
import me.gregorias.dfuntest.TestScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This TestScript checks whether applications will return all ids that were pinged to them.
 */
public class ExamplePingGetIDTestScript implements TestScript<ExampleApp> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplePingGetIDTestScript.class);
  private static final int PING_ID = 1087;

  @Override

  public TestResult run(Collection<ExampleApp> apps) {
    LOGGER.info("run()");
    TestResult result = new TestResult(TestResult.Type.SUCCESS, "Test was successful.");
    for (ExampleApp app : apps) {
      LOGGER.info("run(): testing app {}", app.getId());
      if (result.getType() == TestResult.Type.FAILURE) {
        break;
      }

      try {
        app.startUp();
      } catch (CommandException | IOException e) {
        LOGGER.error("run(): Test could not start an app.", e);
        return new TestResult(TestResult.Type.FAILURE,
            String.format("%d app could not be started.", app.getId()));
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      try {
        app.ping(PING_ID);
        Set<Integer> pingedIDs = new HashSet<>(app.getPingedIDs());

        if (!pingedIDs.contains(PING_ID) || pingedIDs.size() != 1) {
          return new TestResult(TestResult.Type.FAILURE,
              String.format("%d app did not return valid pings.", app.getId()));
        }
      } catch (IOException e) {
        LOGGER.error("run(): {} app did not return valid pings.", app.getId(), e);
        return new TestResult(TestResult.Type.FAILURE,
            String.format("%d app did not return valid pings.", app.getId()));
      } finally {
        try {
          app.shutDown();
        } catch (IOException | InterruptedException e) {
          LOGGER.error("run(): Test could not shut down app.", e);
          result = new TestResult(TestResult.Type.FAILURE,
              String.format("%d app could not be shut down.", app.getId()));
        }
      }
    }
    return result;
  }
}
