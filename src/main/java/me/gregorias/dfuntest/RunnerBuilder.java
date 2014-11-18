package me.gregorias.dfuntest;

import java.nio.file.Path;

/**
 * Builder of SingleTestRunner instance
 */
public class RunnerBuilder<EnvironmentT extends Environment, AppT extends App<EnvironmentT>> {
  private EnvironmentFactory<EnvironmentT> mEnvironmentFactory;
  private EnvironmentPreparator<EnvironmentT> mEnvironmentPreparator;
  private ApplicationFactory<EnvironmentT, AppT> mApplicationFactory;
  private TestScript<AppT> mTestScript;
  private boolean mShouldPrepareEnvironments = true;
  private boolean mShouldCleanEnvironments = true;
  private Path mReportPath;

  public TestRunner buildRunner() {
    if (null == mTestScript
        || null == mEnvironmentFactory
        || null == mEnvironmentPreparator
        || null == mApplicationFactory
        || null == mReportPath) {
      throw new IllegalStateException("One of runner's dependencies was not set.");
    }

    return new SingleTestRunner<EnvironmentT, AppT>(
        mTestScript,
        mEnvironmentFactory,
        mEnvironmentPreparator,
        mApplicationFactory,
        mShouldPrepareEnvironments,
        mShouldCleanEnvironments,
        mReportPath);
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

  public RunnerBuilder<EnvironmentT, AppT> setTestScript(TestScript<AppT> testScript) {
    mTestScript = testScript;
    return this;
  }
}
