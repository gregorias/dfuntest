package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestResult;
import me.gregorias.dfuntest.TestScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Basic test script checking whether application can be started up and shut down.
 */
public class ExampleSanityTestScript implements TestScript<ExampleApp> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleSanityTestScript.class);
  @Override
  public TestResult run(Collection<ExampleApp> apps) {
    Collection<ExampleApp> startedApps = new ArrayList<>();
    TestResult result = new TestResult(TestResult.Type.SUCCESS, "Test was successful.");
    for (ExampleApp app : apps) {
      try {
        app.startUp();
        startedApps.add(app);
      } catch (CommandException | IOException e) {
        LOGGER.error("run(): Test could not start an app.", e);
        result = new TestResult(TestResult.Type.FAILURE, "An app could not be started.");
      }
    }

    try {
      // Sleep to give applications time to start up.
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
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
    return "SanityTestScript";
  }
}
