package me.gregorias.dfuntest;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

/**
 * Basic implementation of TestRunner which runs one test script on given
 * environments and apps.
 *
 * @author Grzegorz Milka
 *
 * @param <TestedAppT>
 */
public class SingleTestRunner<TestedAppT extends App> implements TestRunner {
  private final Logger mLogger;
  private final TestScript<TestedAppT> mScript;

  private final EnvironmentFactory mEnvironmentFactory;

  private final EnvironmentPreparator mEnvironmentPreparator;

  private final ApplicationFactory<TestedAppT> mApplicationFactory;

  public SingleTestRunner(TestScript<TestedAppT> script,
      Logger logger,
      EnvironmentFactory environmentFactory,
      EnvironmentPreparator environmentPreparator,
      ApplicationFactory<TestedAppT> applicationFactory) {
    mScript = script;

    mLogger = logger;

    mEnvironmentFactory = environmentFactory;

    mEnvironmentPreparator = environmentPreparator;

    mApplicationFactory = applicationFactory;
  }

  @Override
  public TestResult run() {
    mLogger.info("run(): Starting preparation for test script {}.", mScript.toString());
    mLogger.info("run(): Creating environments.");
    Collection<Environment> envs;
    try {
      envs = mEnvironmentFactory.createEnvironments();
    } catch (IOException e) {
      mLogger.error("run(): Could not create environments.", e);
      return new TestResult(TestResult.Type.FAILURE, "Could not create environments.");
    }
    try {
      mLogger.info("run(): Preparing environments.");
      mEnvironmentPreparator.prepareEnvironments(envs);
      mLogger.info("run(): Environments prepared: ", envs.size());
    } catch (ExecutionException e) {
      mLogger.error("run(): Could not prepare environments.", e);
      mEnvironmentFactory.destroyEnvironments(envs);
      return new TestResult(TestResult.Type.FAILURE, "Could not prepare environments.");
    }

    Collection<TestedAppT> apps = new LinkedList<>();
    for (Environment env : envs) {
      apps.add(mApplicationFactory.newApp(env));
    }

    TestResult result = mScript.run(apps);

    mLogger.info("run(): Collecting output and log files.");
    mEnvironmentPreparator.collectOutputAndLogFiles(envs);
    mEnvironmentPreparator.cleanEnvironments(envs);
    mEnvironmentFactory.destroyEnvironments(envs);

    return result;
  }
}
