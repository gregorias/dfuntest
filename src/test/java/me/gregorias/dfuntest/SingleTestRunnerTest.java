package me.gregorias.dfuntest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class SingleTestRunnerTest {
  private SingleTestRunner<Environment, App<Environment>> mSingleTestRunner = null;

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
    mSingleTestRunner = new SingleTestRunner<>(mMockTestScript,
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

    TestResult result = mSingleTestRunner.run();

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
    TestResult result = mSingleTestRunner.run();
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

    TestResult result = mSingleTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldOnlyCallRestoreIfSetInConstructor() throws IOException {
    SingleTestRunner singleTestRunner = new SingleTestRunner<>(mMockTestScript,
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

    TestResult result = singleTestRunner.run();

    verify(mMockEnvironmentFactory).create();
    verify(mMockEnvironmentPreparator).restore(eq(envs));
    verify(mMockEnvironmentPreparator, never()).prepare(anyCollection());
    verify(mMockTestScript).run(anyCollection());
    verify(mMockEnvironmentPreparator, never()).clean(anyCollection());
    verify(mMockEnvironmentFactory, never()).destroy(anyCollection());
    assertEquals(TestResult.Type.SUCCESS, result.getType());
  }
}
