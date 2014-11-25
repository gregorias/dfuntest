package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtilsImpl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Builder of MultiTestRunner instance.
 */
public class RunnerBuilder<EnvironmentT extends Environment, AppT extends App<EnvironmentT>> {
  private EnvironmentFactory<EnvironmentT> mEnvironmentFactory;
  private EnvironmentPreparator<EnvironmentT> mEnvironmentPreparator;
  private ApplicationFactory<EnvironmentT, AppT> mApplicationFactory;
  private final Collection<TestScript<AppT>> mTestScripts = new ArrayList<>();
  private boolean mShouldPrepareEnvironments = true;
  private boolean mShouldCleanEnvironments = true;
  private Path mReportPath;

  public RunnerBuilder<EnvironmentT, AppT> addTestScript(TestScript<AppT> testScript) {
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

  public RunnerBuilder<EnvironmentT, AppT> setApplicationFactory(
      ApplicationFactory<EnvironmentT, AppT> applicationFactory) {
    mApplicationFactory = applicationFactory;
    return this;
  }

  public RunnerBuilder<EnvironmentT, AppT> setEnvironmentFactory(
      EnvironmentFactory<EnvironmentT> environmentFactory) {
    mEnvironmentFactory = environmentFactory;
    return this;
  }

  public RunnerBuilder<EnvironmentT, AppT> setEnvironmentPreparator(
      EnvironmentPreparator<EnvironmentT> environmentPreparator) {
    mEnvironmentPreparator = environmentPreparator;
    return this;
  }

  public RunnerBuilder<EnvironmentT, AppT> setReportPath(Path reportPath) {
    mReportPath = reportPath;
    return this;
  }

  /**
   * Sets whether preparator should prepare or just restore environments. True on default.
   * @param should should runner prepare environments
   * @return this
   */
  public RunnerBuilder<EnvironmentT, AppT> setShouldPrepareEnvironments(boolean should) {
    mShouldPrepareEnvironments = should;
    return this;
  }

  /**
   * Sets whether preparator should clean environments completely. True on default.
   * @param should should runner clean environments
   * @return this
   */
  public RunnerBuilder<EnvironmentT, AppT> setShouldCleanEnvironments(boolean should) {
    mShouldCleanEnvironments = should;
    return this;
  }
}
