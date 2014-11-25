package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

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
  public static final String REPORT_FILENAME = "report.txt";
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiTestRunner.class);
  private final Collection<TestScript<AppT>> mScripts;
  private final EnvironmentFactory<EnvironmentT> mEnvironmentFactory;
  private final EnvironmentPreparator<EnvironmentT> mEnvironmentPreparator;
  private final ApplicationFactory<EnvironmentT, AppT> mApplicationFactory;
  private final boolean mShouldPrepareEnvironments;
  private final boolean mShouldCleanEnvironments;
  private final Path mReportPath;
  private final FileUtils mFileUtils;

  private final Path mSummaryReportPath;

  public MultiTestRunner(TestScript<AppT> script,
                         EnvironmentFactory<EnvironmentT> environmentFactory,
                         EnvironmentPreparator<EnvironmentT> environmentPreparator,
                         ApplicationFactory<EnvironmentT, AppT> applicationFactory,
                         boolean shouldPrepareEnvironments,
                         boolean shouldCleanEnvironments,
                         Path reportPath,
                         FileUtils fileUtils) {
    mScripts = new ArrayList<>();
    mScripts.add(script);
    mEnvironmentFactory = environmentFactory;
    mEnvironmentPreparator = environmentPreparator;
    mApplicationFactory = applicationFactory;
    mShouldPrepareEnvironments = shouldPrepareEnvironments;
    mShouldCleanEnvironments = shouldCleanEnvironments;
    mReportPath = reportPath;
    mFileUtils = fileUtils;

    mSummaryReportPath = mReportPath.resolve(REPORT_FILENAME);
  }

  public MultiTestRunner(Collection<TestScript<AppT>> scripts,
                         EnvironmentFactory<EnvironmentT> environmentFactory,
                         EnvironmentPreparator<EnvironmentT> environmentPreparator,
                         ApplicationFactory<EnvironmentT, AppT> applicationFactory,
                         boolean shouldPrepareEnvironments,
                         boolean shouldCleanEnvironments,
                         Path reportPath,
                         FileUtils fileUtils) {
    mScripts = scripts;
    mEnvironmentFactory = environmentFactory;
    mEnvironmentPreparator = environmentPreparator;
    mApplicationFactory = applicationFactory;
    mShouldPrepareEnvironments = shouldPrepareEnvironments;
    mShouldCleanEnvironments = shouldCleanEnvironments;
    mReportPath = reportPath;
    mFileUtils = fileUtils;

    mSummaryReportPath = mReportPath.resolve(REPORT_FILENAME);
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

    boolean hasPrepared = false;
    boolean hasWrittenToSummary = false;
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
        hasWrittenToSummary = saveResultToSummaryReportFile(result, script, !hasWrittenToSummary);
        continue;
      }


      Collection<AppT> apps = new ArrayList<>();
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
      Path testReportPath = mReportPath.resolve(script.toString());
      mEnvironmentPreparator.collectOutputAndLogFiles(envs, testReportPath);
      hasWrittenToSummary = saveResultToSummaryReportFile(result, script, !hasWrittenToSummary);
      saveResultToScriptReportFile(result, testReportPath);
      if (mShouldPrepareEnvironments && mShouldCleanEnvironments) {
        mEnvironmentPreparator.clean(envs);
        hasPrepared = false;
      }
    }

    if (mShouldCleanEnvironments) {
      mEnvironmentFactory.destroy(envs);
    }

    return result;
  }

  private boolean saveResultToSummaryReportFile(TestResult scriptResult,
                                      TestScript<AppT> script,
                                      boolean shouldTruncate) {
    String resultString;
    if (scriptResult.getType() == TestResult.Type.FAILURE) {
      resultString = "[FAILURE]";
    } else {
      resultString = "[SUCCESS]";
    }

    try {
      String content = resultString + " " + script.toString();
      mFileUtils.write(mSummaryReportPath, content, shouldTruncate);
    } catch (IOException e) {
      LOGGER.warn("saveResultToReportFile(): Could not append to summary report file.", e);
      return false;
    }
    return true;
  }

  private void saveResultToScriptReportFile(TestResult scriptResult, Path testScriptReportPath) {
    String resultString;
    if (scriptResult.getType() == TestResult.Type.FAILURE) {
      resultString = "[FAILURE]";
    } else {
      resultString = "[SUCCESS]";
    }

    try {
      String content = resultString + " " + scriptResult.getDescription();
      mFileUtils.write(testScriptReportPath.resolve(REPORT_FILENAME), content, true);
    } catch (IOException e) {
      LOGGER.warn("saveResultToReportFile(): Could not append to report file.", e);
    }
  }
}
