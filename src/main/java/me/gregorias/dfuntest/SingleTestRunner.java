package me.gregorias.dfuntest;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of TestRunner which runs one test script on given
 * environments and apps.
 *
 * @author Grzegorz Milka
 *
 * @param <EnvironmentT>
 * @param <AppT>
 */
public class SingleTestRunner<EnvironmentT extends Environment, AppT extends App<EnvironmentT>>
    implements TestRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(SingleTestRunner.class);
  private final TestScript<AppT> mScript;

  private final EnvironmentFactory<EnvironmentT> mEnvironmentFactory;

  private final EnvironmentPreparator<EnvironmentT> mEnvironmentPreparator;

  private final ApplicationFactory<EnvironmentT, AppT> mApplicationFactory;

  public SingleTestRunner(TestScript<AppT> script,
      EnvironmentFactory<EnvironmentT> environmentFactory,
      EnvironmentPreparator<EnvironmentT> environmentPreparator,
      ApplicationFactory<EnvironmentT, AppT> applicationFactory) {
    mScript = script;

    mEnvironmentFactory = environmentFactory;

    mEnvironmentPreparator = environmentPreparator;

    mApplicationFactory = applicationFactory;
  }

  @Override
  public TestResult run() {
    LOGGER.info("run(): Starting preparation for test script {}.", mScript.toString());
    LOGGER.info("run(): Creating environments.");
    Collection<EnvironmentT> envs;
    try {
      envs = mEnvironmentFactory.createEnvironments();
    } catch (IOException e) {
      LOGGER.error("run(): Could not create environments.", e);
      return new TestResult(TestResult.Type.FAILURE, "Could not create environments.");
    }
    try {
      LOGGER.info("run(): Preparing environments.");
      mEnvironmentPreparator.prepareEnvironments(envs);
      LOGGER.info("run(): Environments prepared: ", envs.size());
    } catch (IOException e) {
      LOGGER.error("run(): Could not prepare environments.", e);
      mEnvironmentFactory.destroyEnvironments(envs);
      return new TestResult(TestResult.Type.FAILURE, "Could not prepare environments.");
    }

    Collection<AppT> apps = new LinkedList<>();
    for (EnvironmentT env : envs) {
      apps.add(mApplicationFactory.newApp(env));
    }

    TestResult result = mScript.run(apps);

    LOGGER.info("run(): Collecting output and log files.");
    mEnvironmentPreparator.collectOutputAndLogFiles(envs);
    mEnvironmentPreparator.cleanEnvironments(envs);
    mEnvironmentFactory.destroyEnvironments(envs);

    return result;
  }
}
