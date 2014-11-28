package me.gregorias.dfuntest;

import me.gregorias.dfuntest.testrunnerbuilders.ManualTestRunnerBuilder;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class ManualTestRunnerBuilderTest {
  private final EnvironmentFactory<Environment> mMockEnvironmentFactory = mock(
      EnvironmentFactory.class);
  private final EnvironmentPreparator<Environment> mMockEnvironmentPreparator = mock(
      EnvironmentPreparator.class);
  private final ApplicationFactory<Environment, App<Environment>> mMockApplicationFactory = mock(
      ApplicationFactory.class);
  private final TestScript<App<Environment>> mMockTestScript = mock(TestScript.class);

  private final Path mReportPath = FileSystems.getDefault().getPath("report");

  @Test
  public void buildShouldCreateMultiTestRunner() {
    ManualTestRunnerBuilder<Environment, App<Environment>> builder =
        new ManualTestRunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);

    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.addTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    TestRunner runner = builder.buildRunner();
    assertTrue(runner instanceof MultiTestRunner);
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfEnvironmentFactory() {
    ManualTestRunnerBuilder<Environment, App<Environment>> builder =
        new ManualTestRunnerBuilder<>();

    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.addTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    builder.buildRunner();
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfEnvironmentPreparator() {
    ManualTestRunnerBuilder<Environment, App<Environment>> builder =
        new ManualTestRunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.addTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    builder.buildRunner();
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfApplicationFactory() {
    ManualTestRunnerBuilder<Environment, App<Environment>> builder =
        new ManualTestRunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setReportPath(mReportPath);
    builder.addTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    builder.buildRunner();
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfReportPath() {
    ManualTestRunnerBuilder<Environment, App<Environment>> builder =
        new ManualTestRunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.addTestScript(mMockTestScript);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    builder.buildRunner();
  }

  @Test(expected = IllegalStateException.class)
  public void buildShouldThrowErrorOnLackOfTestScript() {
    ManualTestRunnerBuilder<Environment, App<Environment>> builder =
        new ManualTestRunnerBuilder<>();

    builder.setEnvironmentFactory(mMockEnvironmentFactory);
    builder.setEnvironmentPreparator(mMockEnvironmentPreparator);
    builder.setApplicationFactory(mMockApplicationFactory);
    builder.setReportPath(mReportPath);
    builder.setShouldCleanEnvironments(false);
    builder.setShouldPrepareEnvironments(false);

    builder.buildRunner();
  }
}
