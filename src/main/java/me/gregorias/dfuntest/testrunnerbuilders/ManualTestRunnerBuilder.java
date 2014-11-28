package me.gregorias.dfuntest.testrunnerbuilders;

import me.gregorias.dfuntest.App;
import me.gregorias.dfuntest.ApplicationFactory;
import me.gregorias.dfuntest.Environment;
import me.gregorias.dfuntest.EnvironmentFactory;
import me.gregorias.dfuntest.EnvironmentPreparator;
import me.gregorias.dfuntest.MultiTestRunner;
import me.gregorias.dfuntest.TestRunner;
import me.gregorias.dfuntest.TestScript;
import me.gregorias.dfuntest.util.FileUtilsImpl;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder of MultiTestRunner instance.
 */
public class ManualTestRunnerBuilder<EnvironmentT extends Environment,
    AppT extends App<EnvironmentT>> {
  private EnvironmentFactory<EnvironmentT> mEnvironmentFactory;
  private EnvironmentPreparator<EnvironmentT> mEnvironmentPreparator;
  private ApplicationFactory<EnvironmentT, AppT> mApplicationFactory;
  private final Set<TestScript<AppT>> mTestScripts = new HashSet<>();
  private boolean mShouldPrepareEnvironments = true;
  private boolean mShouldCleanEnvironments = true;
  private Path mReportPath;

  public ManualTestRunnerBuilder<EnvironmentT, AppT> addTestScript(TestScript<AppT> testScript) {
    mTestScripts.add(testScript);
    return this;
  }

  public TestRunner buildRunner() {
    if (mTestScripts.size() == 0
        || null == mEnvironmentFactory
        || null == mEnvironmentPreparator
        || null == mApplicationFactory
        || null == mReportPath) {
      throw new IllegalStateException("One of runner's dependencies was not set.");
    }

    return new MultiTestRunner<>(
        mTestScripts,
        mEnvironmentFactory,
        mEnvironmentPreparator,
        mApplicationFactory,
        mShouldPrepareEnvironments,
        mShouldCleanEnvironments,
        mReportPath,
        FileUtilsImpl.getFileUtilsImpl());
  }

  public ManualTestRunnerBuilder<EnvironmentT, AppT> setApplicationFactory(
      ApplicationFactory<EnvironmentT, AppT> applicationFactory) {
    mApplicationFactory = applicationFactory;
    return this;
  }

  public ManualTestRunnerBuilder<EnvironmentT, AppT> setEnvironmentFactory(
      EnvironmentFactory<EnvironmentT> environmentFactory) {
    mEnvironmentFactory = environmentFactory;
    return this;
  }

  public ManualTestRunnerBuilder<EnvironmentT, AppT> setEnvironmentPreparator(
      EnvironmentPreparator<EnvironmentT> environmentPreparator) {
    mEnvironmentPreparator = environmentPreparator;
    return this;
  }

  public ManualTestRunnerBuilder<EnvironmentT, AppT> setReportPath(Path reportPath) {
    mReportPath = reportPath;
    return this;
  }

  /**
   * Sets whether preparator should prepare or just restore environments. True on default.
   * @param should should runner prepare environments
   * @return this
   */
  public ManualTestRunnerBuilder<EnvironmentT, AppT> setShouldPrepareEnvironments(boolean should) {
    mShouldPrepareEnvironments = should;
    return this;
  }

  /**
   * Sets whether preparator should clean environments completely. True on default.
   * @param should should runner clean environments
   * @return this
   */
  public ManualTestRunnerBuilder<EnvironmentT, AppT> setShouldCleanEnvironments(boolean should) {
    mShouldCleanEnvironments = should;
    return this;
  }
}
