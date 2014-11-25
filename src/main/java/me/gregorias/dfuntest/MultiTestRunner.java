package me.gregorias.dfuntest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Basic implementation of TestRunner which runs sequentially multiple test script on given
 * environments and apps.
 *
 * It runs preparation methods on environments if shouldPrepareEnvironments flag is set and the
 * environment either hasn't been prepared or has been cleaned.
 *
 * @author Grzegorz Milka
 *
 * @param <EnvironmentT>
 * @param <AppT>
 */
public class MultiTestRunner<EnvironmentT extends Environment, AppT extends App<EnvironmentT>>
    implements TestRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiTestRunner.class);
  private final Collection<TestScript<AppT>> mScripts;
  private final EnvironmentFactory<EnvironmentT> mEnvironmentFactory;
  private final EnvironmentPreparator<EnvironmentT> mEnvironmentPreparator;
  private final ApplicationFactory<EnvironmentT, AppT> mApplicationFactory;
  private final boolean mShouldPrepareEnvironments;
  private final boolean mShouldCleanEnvironments;
  private final Path mReportPath;

  public MultiTestRunner(TestScript<AppT> script,
                         EnvironmentFactory<EnvironmentT> environmentFactory,
                         EnvironmentPreparator<EnvironmentT> environmentPreparator,
                         ApplicationFactory<EnvironmentT, AppT> applicationFactory,
                         boolean shouldPrepareEnvironments,
                         boolean shouldCleanEnvironments,
                         Path reportPath) {
    mScripts = new ArrayList<>();
    mScripts.add(script);
    mEnvironmentFactory = environmentFactory;
    mEnvironmentPreparator = environmentPreparator;
    mApplicationFactory = applicationFactory;
    mShouldPrepareEnvironments = shouldPrepareEnvironments;
    mShouldCleanEnvironments = shouldCleanEnvironments;
    mReportPath = reportPath;
  }

  public MultiTestRunner(Collection<TestScript<AppT>> scripts,
                         EnvironmentFactory<EnvironmentT> environmentFactory,
                         EnvironmentPreparator<EnvironmentT> environmentPreparator,
                         ApplicationFactory<EnvironmentT, AppT> applicationFactory,
                         boolean shouldPrepareEnvironments,
                         boolean shouldCleanEnvironments,
                         Path reportPath) {
    mScripts = scripts;
    mEnvironmentFactory = environmentFactory;
    mEnvironmentPreparator = environmentPreparator;
    mApplicationFactory = applicationFactory;
    mShouldPrepareEnvironments = shouldPrepareEnvironments;
    mShouldCleanEnvironments = shouldCleanEnvironments;
    mReportPath = reportPath;
  }

  @Override
  public TestResult run() {
    LOGGER.info("run()");

    Collection<EnvironmentT> envs;
    LOGGER.info("run(): Creating environments.");
    try {
      envs = mEnvironmentFactory.create();
    } catch (IOException e) {
      LOGGER.error("run(): Could not create environments.", e);
      return new TestResult(TestResult.Type.FAILURE, "Could not create environments.");
    }

    int testIdx = 0;
    boolean hasPrepared = false;
    TestResult result = new TestResult(TestResult.Type.SUCCESS, "Everything went ok.");
    for (TestScript<AppT> script : mScripts) {
      try {
        if (mShouldPrepareEnvironments && !hasPrepared) {
          LOGGER.info("run(): Preparing environments.");
          mEnvironmentPreparator.prepare(envs);
          hasPrepared = true;
        } else {
          LOGGER.info("run(): Restoring environments.");
          mEnvironmentPreparator.restore(envs);
        }
        LOGGER.info("run(): Environments prepared: {}", envs.size());
      } catch (IOException e) {
        LOGGER.error("run(): Could not prepare environments for test {}.", script, e);
        result = new TestResult(TestResult.Type.FAILURE, "Could not prepare environments.");
        continue;
      }


      Collection<AppT> apps = new LinkedList<>();
      for (EnvironmentT env : envs) {
        apps.add(mApplicationFactory.newApp(env));
      }

      LOGGER.info("run(): Running test {}", script);
      TestResult scriptResult = script.run(apps);
      LOGGER.info("run(): script {} has ended with result: {}", script, scriptResult.getType());
      if (scriptResult.getType() == TestResult.Type.FAILURE) {
        result = new TestResult(TestResult.Type.FAILURE, String.format("%s has failed.",
            script.toString()));
      }

      LOGGER.info("run(): Collecting output and log files.");
      Path testReportPath = mReportPath.resolve(testIdx + "_" + script.toString());
      mEnvironmentPreparator.collectOutputAndLogFiles(envs, testReportPath);
      if (mShouldPrepareEnvironments && mShouldCleanEnvironments) {
        mEnvironmentPreparator.clean(envs);
        hasPrepared = false;
      }
      ++testIdx;
    }

    if (mShouldCleanEnvironments) {
      mEnvironmentFactory.destroy(envs);
    }

    return result;
  }
}
