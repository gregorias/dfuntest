package me.gregorias.dfuntest;

import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class RunnerBuilderTest {
  private final EnvironmentFactory<Environment> mMockEnvironmentFactory = mock(
      EnvironmentFactory.class);
  private final EnvironmentPreparator<Environment> mMockEnvironmentPreparator = mock(
      EnvironmentPreparator.class);
  private final ApplicationFactory<Environment, App<Environment>> mMockApplicationFactory = mock(
      ApplicationFactory.class);
  private final TestScript<App<Environment>> mMockTestScript = mock(TestScript.class);

  private final Path mReportPath = FileSystems.getDefault().getPath("report");

  @Test
  public void buildShouldCreateSingleTestRunner() {
    RunnerBuilder<Environment, App<Environment>> builder = new RunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.setTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof SingleTestRunner);
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfEnvironmentFactory() {
    RunnerBuilder<Environment, App<Environment>> builder = new RunnerBuilder<>();

    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.setTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof SingleTestRunner);
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfEnvironmentPreparator() {
    RunnerBuilder<Environment, App<Environment>> builder = new RunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.setTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof SingleTestRunner);
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfApplicationFactory() {
    RunnerBuilder<Environment, App<Environment>> builder = new RunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setReportPath(mReportPath);
    builder.setTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof SingleTestRunner);
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfReportPath() {
    RunnerBuilder<Environment, App<Environment>> builder = new RunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof SingleTestRunner);
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfTestScript() {
    RunnerBuilder<Environment, App<Environment>> builder = new RunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof SingleTestRunner);
  }
}
