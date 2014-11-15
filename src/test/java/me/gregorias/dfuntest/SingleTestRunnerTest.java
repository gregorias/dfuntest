package me.gregorias.dfuntest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class SingleTestRunnerTest {
  private SingleTestRunner<Environment, App<Environment>> mSingleTestRunner = null;

  private TestScript<App<Environment>> mMockTestScript = null;
  private EnvironmentFactory<Environment> mMockEnvironmentFactory = null;
  private EnvironmentPreparator<Environment> mMockEnvironmentPreparator = null;

  private ApplicationFactory<Environment, App<Environment>> mMockApplicationFactory = null;

  @Before
  public void setUp() {
    mMockTestScript = mock(TestScript.class);
    mMockEnvironmentFactory = mock(EnvironmentFactory.class);
    mMockEnvironmentPreparator = mock(EnvironmentPreparator.class);
    mMockApplicationFactory = mock(ApplicationFactory.class);
    mSingleTestRunner = new SingleTestRunner<Environment, App<Environment>>(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory);
  }

  @Test
  public void runShouldPrepareAndDestroyProperly() throws IOException {
    Collection<Environment> envs = new LinkedList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.createEnvironments()).thenReturn(envs);
    when(mMockTestScript.run(anyCollection())).thenReturn(
        new TestResult(TestResult.Type.SUCCESS, "Success"));

    TestResult result = mSingleTestRunner.run();

    InOrder inOrder = inOrder(mMockTestScript,
        mMockEnvironmentFactory,
        mMockEnvironmentPreparator,
        mMockApplicationFactory);
    inOrder.verify(mMockEnvironmentFactory).createEnvironments();
    inOrder.verify(mMockEnvironmentPreparator).prepareEnvironments(eq(envs));
    inOrder.verify(mMockApplicationFactory).newApp(any(Environment.class));
    inOrder.verify(mMockTestScript).run(anyCollection());
    inOrder.verify(mMockEnvironmentPreparator).cleanEnvironments(eq(envs));
    inOrder.verify(mMockEnvironmentFactory).destroyEnvironments(eq(envs));
    assertEquals(TestResult.Type.SUCCESS, result.getType());
  }

  @Test
  public void runShouldFailOnCreateEnvironmentsFail() throws IOException {
    when(mMockEnvironmentFactory.createEnvironments()).thenThrow(IOException.class);
    TestResult result = mSingleTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }

  @Test
  public void runShouldFailOnPrepareEnvironmentsFail() throws IOException {
    Collection<Environment> envs = new LinkedList<>();
    Environment mockEnv = mock(Environment.class);
    envs.add(mockEnv);
    when(mMockEnvironmentFactory.createEnvironments()).thenReturn(envs);
    doThrow(IOException.class).when(mMockEnvironmentPreparator).prepareEnvironments(
        anyCollection());

    TestResult result = mSingleTestRunner.run();
    assertEquals(TestResult.Type.FAILURE, result.getType());
  }
}
