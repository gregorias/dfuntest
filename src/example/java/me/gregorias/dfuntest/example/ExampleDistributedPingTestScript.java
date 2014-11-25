package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestResult;
import me.gregorias.dfuntest.TestScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This test script tests whether all applications send their id to initial application.
 */
public class ExampleDistributedPingTestScript implements TestScript<ExampleApp> {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      ExampleDistributedPingTestScript.class);

  @Override
  public TestResult run(Collection<ExampleApp> apps) {
    List<ExampleApp> startedApps = new ArrayList<>();
    Set<Integer> expectedIDs = new HashSet<>();
    TestResult result = new TestResult(TestResult.Type.SUCCESS, "Test was successful.");
    for (ExampleApp app : apps) {
      try {
        app.startUp();
        startedApps.add(app);
        expectedIDs.add(app.getId());
      } catch (CommandException | IOException e) {
        LOGGER.error("run(): Test could not start an app.", e);
        result = new TestResult(TestResult.Type.FAILURE, "An app could not be started.");
      }
    }

    try {
      // Sleep to give applications time to start up.
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (result.getType() != TestResult.Type.FAILURE && startedApps.size() != 0) {
      ExampleApp zeroApp = startedApps.get(0);
      try {
        Set<Integer> pingedIDs = new HashSet<>(zeroApp.getPingedIDs());
        if (!pingedIDs.equals(expectedIDs)) {
          LOGGER.error("run(): Returned IDs are not equal to the set of all application IDs.");
          result = new TestResult(TestResult.Type.FAILURE, "An app could not be started.");
        }
      } catch (IOException e) {
        LOGGER.error("run(): Could not get pinged IDs.", e);
        result = new TestResult(TestResult.Type.FAILURE,
            "Could not get pinged IDs due to IOException.");
      }
    }

    for (ExampleApp app : startedApps) {
      try {
        app.shutDown();
      } catch (IOException | InterruptedException e) {
        LOGGER.error("run(): Test could not shut down an app.", e);
        if (result.getType() != TestResult.Type.SUCCESS) {
          result = new TestResult(TestResult.Type.FAILURE, "An app could not be shut down.");
        }
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "DistributedPingTestScript";
  }
}
