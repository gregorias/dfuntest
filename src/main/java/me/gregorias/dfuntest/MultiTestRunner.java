package me.gregorias.dfuntest;

import com.google.inject.Inject;
import me.gregorias.dfuntest.util.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * Basic implementation of TestRunner which runs sequentially multiple test script on given
 * environments and apps.
 * </p>
 *
 * <p>
 * It runs preparation methods on environments if shouldPrepareEnvironments flag is set and the
 * environment either hasn't been prepared or has been cleaned.
 * </p>
 *
 * @author Grzegorz Milka
 *
 * @param <EnvironmentT>
 * @param <AppT>
 */
public class MultiTestRunner<EnvironmentT extends Environment, AppT extends App<EnvironmentT>>
    implements TestRunner {
  public static final String REPORT_FILENAME = "report.txt";
  public static final String SCRIPTS_ARGUMENT_NAME = "MultiTestRunner.scripts";
  public static final String SHOULD_PREPARE_ARGUMENT_NAME =
      "MultiTestRunner.shouldPrepareEnvironments";
  public static final String SHOULD_CLEAN_ARGUMENT_NAME =
      "MultiTestRunner.shouldCleanEnvironments";
  public static final String REPORT_PATH_ARGUMENT_NAME =
      "MultiTestRunner.reportPath";
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

  @Inject
  public MultiTestRunner(@Named(SCRIPTS_ARGUMENT_NAME) Set<TestScript<AppT>> scripts,
                         EnvironmentFactory<EnvironmentT> environmentFactory,
                         EnvironmentPreparator<EnvironmentT> environmentPreparator,
                         ApplicationFactory<EnvironmentT, AppT> applicationFactory,
                         @Named(SHOULD_PREPARE_ARGUMENT_NAME)
                         boolean shouldPrepareEnvironments,
                         @Named(SHOULD_CLEAN_ARGUMENT_NAME)
                         boolean shouldCleanEnvironments,
                         @Named(REPORT_PATH_ARGUMENT_NAME)
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
    LOGGER.debug("run(): Creating environments.");
    try {
      envs = mEnvironmentFactory.create();
    } catch (IOException e) {
      LOGGER.error("run(): Could not create environments.", e);
      return new TestResult(TestResult.Type.FAILURE, "Could not create environments.");
    }

    boolean hasPrepared = false;
    Collection<String> failedTests = new ArrayList<>();

    for (TestScript<AppT> script : mScripts) {
      LOGGER.debug("run(): Preparing and running {}.", script);
      try {
        if (mShouldPrepareEnvironments && !hasPrepared) {
          LOGGER.debug("run(): Preparing environments.");
          mEnvironmentPreparator.prepare(envs);
          hasPrepared = true;
        } else {
          LOGGER.debug("run(): Restoring environments.");
          mEnvironmentPreparator.restore(envs);
        }
        LOGGER.debug("run(): Environments prepared or restored successfully.");
      } catch (IOException e) {
        String errorMsg = String.format("Could not prepare environments for %s.",
            script.toString());
        LOGGER.error("run(): " + errorMsg, script, e);
        saveResultToSummaryReportFile(
            new TestResult(TestResult.Type.FAILURE, errorMsg),
            script);
        failedTests.add(script.toString());
        continue;
      }

      Collection<AppT> apps = new ArrayList<>();
      for (EnvironmentT env : envs) {
        apps.add(mApplicationFactory.newApp(env));
      }

      LOGGER.info("run(): Running test {}", script);
      TestResult scriptResult = script.run(apps);
      LOGGER.info("run(): Test {} has ended with {}", script, scriptResult.getType());

      if (scriptResult.getType() == TestResult.Type.FAILURE) {
        failedTests.add(script.toString());
      }

      LOGGER.debug("run(): Collecting output and log files.");
      Path testReportPath = mReportPath.resolve(script.toString());
      mEnvironmentPreparator.collectOutputAndLogFiles(envs, testReportPath);
      saveResultToSummaryReportFile(scriptResult, script);
      saveResultToScriptReportFile(scriptResult, testReportPath);
      if (mShouldPrepareEnvironments && mShouldCleanEnvironments) {
        LOGGER.debug("run(): Cleaning environments.");
        mEnvironmentPreparator.clean(envs);
        hasPrepared = false;
      }
    }

    if (mShouldCleanEnvironments) {
      LOGGER.debug("run(): Destroying environments.");
      mEnvironmentFactory.destroy(envs);
    }

    if (failedTests.isEmpty()) {
      return new TestResult(TestResult.Type.SUCCESS, "TestRunner has run all tests successfully.");
    } else {
      return new TestResult(TestResult.Type.FAILURE,
          "Some tests have failed: " + StringUtils.join(failedTests, " "));
    }
  }

  private void createParentDirectories(Path destPath) throws IOException {
    Path parentPath = destPath.getParent();
    assert parentPath != null : "Destination path pointed to current directory.";
    mFileUtils.createDirectories(parentPath);
  }

  private String getResultTypeString(TestResult.Type type) {
    switch (type) {
      case SUCCESS:
        return "[SUCCESS]";
      case FAILURE:
      default:
        return "[FAILURE]";
    }
  }

  private boolean saveResultToSummaryReportFile(TestResult scriptResult,
                                      TestScript<AppT> script) {
    String resultString = getResultTypeString(scriptResult.getType());

    try {
      String content = resultString + " " + script.toString();
      createParentDirectories(mSummaryReportPath);
      mFileUtils.write(mSummaryReportPath, content);
    } catch (IOException e) {
      LOGGER.warn("saveResultToReportFile(): Could not append to summary report file.", e);
      return false;
    }
    return true;
  }

  private void saveResultToScriptReportFile(TestResult scriptResult, Path testScriptReportPath) {
    String resultString = getResultTypeString(scriptResult.getType());
    Path testScriptSummaryReportPath = testScriptReportPath.resolve(REPORT_FILENAME);

    try {
      String content = resultString + " " + scriptResult.getDescription();
      createParentDirectories(testScriptSummaryReportPath);
      mFileUtils.write(testScriptSummaryReportPath, content);
    } catch (IOException e) {
      LOGGER.warn("saveResultToReportFile(): Could not append to report file.", e);
    }
  }
}
