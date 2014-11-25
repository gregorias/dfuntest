package me.gregorias.dfuntest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
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

  @Before
  public void setUp() {
    mMultiTestRunner = new MultiTestRunner<>(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath);
  }

  @Test
  public void runShouldPrepareAndDestroyProperly() throws IOException {
    Collection<Environment> envs = new LinkedList<>();
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
    inOrder.verify(mMockEnvironmentPreparator).clean(eq(envs));
    inOrder.verify(mMockEnvironmentFactory).destroy(eq(envs));
    assertEquals(TestResult.Type.SUCCESS, result.getType());
  }

  @Test
  public void runShouldFailOnCreateEnvironmentsFail() throws IOException {
    when(mMockEnvironmentFactory.create()).thenThrow(IOException.class);
    TestResult result = mMultiTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldFailOnPrepareEnvironmentsFail() throws IOException {
    Collection<Environment> envs = new LinkedList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.create()).thenReturn(envs);
    doThrow(IOException.class).when(mMockEnvironmentPreparator).prepare(
        anyCollection());

    TestResult result = mMultiTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldOnlyCallRestoreIfSetInConstructor() throws IOException {
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        false,
        false,
        mReportPath);

    Collection<Environment> envs = new LinkedList<>();
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
    verify(mMockEnvironmentPreparator, never()).clean(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
    assertEquals(TestResult.Type.SUCCESS, result.getType());
  }

  @Test
  public void runShouldFailOnMultipleTestScriptFailButRunAllTests() throws IOException {
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Collection<TestScript<App<Environment>>> scripts = new ArrayList<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        false,
        false,
        mReportPath);

    Collection<Environment> envs = new LinkedList<>();
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
    verify(mMockEnvironmentPreparator, never()).clean(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldOnlyCallPrepareOnceIfCleanIsNotSet() throws IOException {
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Collection<TestScript<App<Environment>>> scripts = new ArrayList<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        false,
        mReportPath);

    Collection<Environment> envs = new LinkedList<>();
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
    verify(mMockEnvironmentPreparator, never()).clean(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
  }

  @Test
  public void runShouldOnlyCallPrepareTwice() throws IOException {
    TestScript<App<Environment>> secondMockTestScript = mock(TestScript.class);
    Collection<TestScript<App<Environment>>> scripts = new ArrayList<>();
    scripts.add(mMockTestScript);
    scripts.add(secondMockTestScript);
    MultiTestRunner multiTestRunner = new MultiTestRunner<>(scripts,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory,
        true,
        true,
        mReportPath);

    Collection<Environment> envs = new LinkedList<>();
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
    verify(mMockEnvironmentPreparator, times(2)).clean(eq(envs));
    verify(mMockEnvironmentFactory).destroy(eq(envs));
  }
}
