package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class MultiTestRunnerTest {
  private MultiTestRunner<Environment, App<Environment>> mMultiTestRunner = null;

  private final TestScript<App<Environment>> mMockTestScript = mock(TestScript.class);
  private final EnvironmentFactory<Environment> mMockEnvironmentFactory =
      mock(EnvironmentFactory.class);
  private final EnvironmentPreparator<Environment> mMockEnvironmentPreparator =
      mock(EnvironmentPreparator.class);
  private final ApplicationFactory<Environment, App<Environment>> mMockApplicationFactory =
      mock(ApplicationFactory.class);

  private final Path mReportPath = FileSystems.getDefault().getPath("reportDir");
  private final FileUtils mMockFileUtils = mock(FileUtils.class);

  @Before
  public void setUp() {
    mMultiTestRunner = new MultiTestRunner<>(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath,
        mMockFileUtils);
  }

  @Test
  public void runShouldPrepareAndDestroyInOrder() throws IOException {
    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    TestResult result = mMultiTestRunner.run();

    InOrder inOrder = inOrder(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory);
    inOrder.verify(mMockEnvironmentFactory).create();
    inOrder.verify(mMockEnvironmentPreparator).prepare(eq(envs));
    inOrder.verify(mMockApplicationFactory).newApp(any(Environment.class));
    inOrder.verify(mMockTestScript).run(anyCollection());
    inOrder.verify(mMockEnvironmentPreparator).collectOutput(eq(envs), any(Path.class));
    inOrder.verify(mMockEnvironmentPreparator).cleanAll(eq(envs));
    inOrder.verify(mMockEnvironmentFactory).destroy(eq(envs));
    assertEquals(TestResult.Type.SUCCESS, result.getType());
  }

  @Test
  public void runShouldNotCallRedundantPreparationAndCleaningMethods() throws IOException {
    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    mMultiTestRunner.run();

    verify(mMockEnvironmentPreparator, never()).restore(anyCollection());
    verify(mMockEnvironmentPreparator, never()).cleanOutput(anyCollection());
  }

  @Test
  public void runShouldFailOnCreateEnvironmentsFail() throws IOException {
    when(mMockEnvironmentFactory.create()).thenThrow(IOException.class);
    TestResult result = mMultiTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldFailOnPrepareEnvironmentsFail() throws IOException {
    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    doThrow(IOException.class).when(mMockEnvironmentPreparator).prepare(
        anyCollection());

    TestResult result = mMultiTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldCallRestoreAndCleanOutputIfSetInConstructor() throws IOException {
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        false,
        false,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    TestResult result = multiTestRunner.run();

    verify(mMockEnvironmentFactory).create();
    verify(mMockEnvironmentPreparator).restore(eq(envs));
    verify(mMockEnvironmentPreparator, never()).prepare(anyCollection());
    verify(mMockTestScript).run(anyCollection());
    verify(mMockEnvironmentPreparator).cleanOutput(eq(envs));
    verify(mMockEnvironmentPreparator, never()).cleanAll(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
    assertEquals(TestResult.Type.SUCCESS, result.getType());
  }

  @Test
  public void runShouldFailOnMultipleTestScriptFailButRunAllTests() throws IOException {
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Set<TestScript<App<Environment>>> scripts = new HashSet<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        false,
        false,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.FAILURE, "Failure"));
    when(secondMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    TestResult result = multiTestRunner.run();

    verify(mMockEnvironmentFactory).create();
    verify(mMockEnvironmentPreparator, times(2)).restore(eq(envs));
    verify(mMockEnvironmentPreparator, never()).prepare(anyCollection());
    verify(mMockTestScript).run(anyCollection());
    verify(secondMockTestScript).run(anyCollection());
    verify(mMockEnvironmentPreparator, times(2)).cleanOutput(eq(envs));
    verify(mMockEnvironmentPreparator, never()).cleanAll(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldOnlyCallPrepareOnceIfCleanIsNotSet() throws IOException {
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Set<TestScript<App<Environment>>> scripts = new HashSet<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        false,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(secondMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    multiTestRunner.run();

    verify(mMockEnvironmentFactory).create();
    verify(mMockEnvironmentPreparator).prepare(eq(envs));
    verify(mMockEnvironmentPreparator).restore(eq(envs));
    verify(mMockTestScript).run(anyCollection());
    verify(secondMockTestScript).run(anyCollection());
    verify(mMockEnvironmentPreparator, times(2)).cleanOutput(eq(envs));
    verify(mMockEnvironmentPreparator, never()).cleanAll(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
  }

  @Test
  public void runShouldOnlyCallPrepareTwice() throws IOException {
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Set<TestScript<App<Environment>>> scripts = new HashSet<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(secondMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    multiTestRunner.run();

    verify(mMockEnvironmentFactory).create();
    verify(mMockEnvironmentPreparator, times(2)).prepare(eq(envs));
    verify(mMockEnvironmentPreparator, never()).restore(anyCollection());
    verify(mMockTestScript).run(anyCollection());
    verify(secondMockTestScript).run(anyCollection());
    verify(mMockEnvironmentPreparator, times(2)).cleanAll(eq(envs));
    verify(mMockEnvironmentFactory).destroy(eq(envs));
  }

  @Test
  public void runShouldCreateReportsForEveryTestScriptAndSummary() throws IOException {
    String firstTestScriptName = "FirstTestScript";
    String secondTestScriptName = "SecondTestScript";
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Set<TestScript<App<Environment>>> scripts = new HashSet<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(mMockTestScript.toString()).thenReturn(firstTestScriptName);
    when(secondMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(secondMockTestScript.toString()).thenReturn(secondTestScriptName);

    multiTestRunner.run();

    Path expectedSummaryReportPath = mReportPath.resolve(MultiTestRunner.REPORT_FILENAME);
    verify(mMockFileUtils, times(2)).write(eq(expectedSummaryReportPath), anyString());

    Path expectedFirstReportPath = mReportPath.resolve(firstTestScriptName).resolve(
        MultiTestRunner.REPORT_FILENAME);
    verify(mMockFileUtils).write(eq(expectedFirstReportPath), anyString());

    Path expectedSecondReportPath = mReportPath.resolve(firstTestScriptName).resolve(
        MultiTestRunner.REPORT_FILENAME);
    verify(mMockFileUtils).write(eq(expectedSecondReportPath), anyString());
  }

  @Test
  public void runShouldContinueEvenIfWriteSummaryFails() throws IOException {
    String firstTestScriptName = "FirstTestScript";
    String secondTestScriptName = "SecondTestScript";
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Set<TestScript<App<Environment>>> scripts = new HashSet<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(mMockTestScript.toString()).thenReturn(firstTestScriptName);
    when(secondMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(secondMockTestScript.toString()).thenReturn(secondTestScriptName);
    Path expectedSummaryReportPath = mReportPath.resolve(MultiTestRunner.REPORT_FILENAME);
    Path expectedFirstReportPath = mReportPath.resolve(firstTestScriptName).resolve(
        MultiTestRunner.REPORT_FILENAME);
    Path expectedSecondReportPath = mReportPath.resolve(firstTestScriptName).resolve(
        MultiTestRunner.REPORT_FILENAME);

    doThrow(IOException.class).when(mMockFileUtils).write(
        eq(expectedSummaryReportPath), anyString());

    multiTestRunner.run();

    verify(mMockFileUtils).write(eq(expectedFirstReportPath), anyString());
    verify(mMockFileUtils).write(eq(expectedSecondReportPath), anyString());
  }

  @Test
  public void runShouldContinueEvenIfWriteScriptReportFails() throws IOException {
    String firstTestScriptName = "FirstTestScript";
    String secondTestScriptName = "SecondTestScript";
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Set<TestScript<App<Environment>>> scripts = new HashSet<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath,
        mMockFileUtils);

    Collection<Environment> envs = new ArrayList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(mMockTestScript.toString()).thenReturn(firstTestScriptName);
    when(secondMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));
    when(secondMockTestScript.toString()).thenReturn(secondTestScriptName);
    Path expectedSummaryReportPath = mReportPath.resolve(MultiTestRunner.REPORT_FILENAME);
    Path expectedFirstReportPath = mReportPath.resolve(firstTestScriptName).resolve(
        MultiTestRunner.REPORT_FILENAME);
    Path expectedSecondReportPath = mReportPath.resolve(firstTestScriptName).resolve(
        MultiTestRunner.REPORT_FILENAME);

    doThrow(IOException.class).when(mMockFileUtils).write(
        eq(expectedFirstReportPath), anyString());

    multiTestRunner.run();

    verify(mMockFileUtils, times(2)).write(eq(expectedSummaryReportPath), anyString());
    verify(mMockFileUtils).write(eq(expectedSecondReportPath), anyString());
  }
}
