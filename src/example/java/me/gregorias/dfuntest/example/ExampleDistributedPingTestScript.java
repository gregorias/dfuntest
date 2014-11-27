package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestResult;
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
public class ExampleDistributedPingTestScript extends AbstractExampleTestScript {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      ExampleDistributedPingTestScript.class);

  @Override
  public TestResult run(Collection<ExampleApp> apps) {
    List<ExampleApp> appList = new ArrayList<>(apps);
    Set<Integer> expectedIDs = new HashSet<>();
    TestResult result = new TestResult(TestResult.Type.SUCCESS, "Test was successful.");

    try {
      startUpApps(apps);
    } catch (CommandException | IOException e) {
      return new TestResult(TestResult.Type.FAILURE, "An app could not be started.");
    }

    for (ExampleApp app : apps) {
      expectedIDs.add(app.getId());
    }

    try {
      // Sleep to give applications time to start up.
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    ExampleApp zeroApp = appList.get(0);
    try {
      Set<Integer> pingedIDs = new HashSet<>(zeroApp.getPingedIDs());
      if (!pingedIDs.equals(expectedIDs)) {
        String errorMsg = String.format("Returned IDs are not equal to the set of all"
            + " application IDs. Expected: %s, got: %s", expectedIDs, pingedIDs);
        LOGGER.error("run(): " + errorMsg);
        result = new TestResult(TestResult.Type.FAILURE, errorMsg);
      }
    } catch (IOException e) {
      String errorMsg = "Could not get pinged IDs due to IOException";
      LOGGER.error("run(): " + errorMsg, e);
      result = new TestResult(TestResult.Type.FAILURE, errorMsg);
    }

    shutDownApps(apps);
    return result;
  }

  @Override
  public String toString() {
    return "DistributedPingTestScript";
  }
}
