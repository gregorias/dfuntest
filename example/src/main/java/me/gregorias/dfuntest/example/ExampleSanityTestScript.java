package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * Basic test script checking whether application can be started up and shut down.
 */
public class ExampleSanityTestScript extends AbstractExampleTestScript {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleSanityTestScript.class);
  @Override
  public TestResult run(Collection<ExampleApp> apps) {
    LOGGER.info("run()");
    try {
      startUpApps(apps);
    } catch (CommandException | IOException e) {
      return new TestResult(TestResult.Type.FAILURE, "An app could not be started.");
    }

    try {
      // Sleep to give applications time to start up.
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    boolean wasSuccessful = shutDownApps(apps);
    if (!wasSuccessful) {
      return new TestResult(TestResult.Type.FAILURE, "An app could not be shut down.");
    }
    return new TestResult(TestResult.Type.SUCCESS, "Test was successful.");
  }

  @Override
  public String toString() {
    return "SanityTestScript";
  }
}
