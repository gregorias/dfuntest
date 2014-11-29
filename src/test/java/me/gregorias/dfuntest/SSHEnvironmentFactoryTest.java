package me.gregorias.dfuntest;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class SSHEnvironmentFactoryTest {
  private static final String mUsername = "username";
  private static final Path mPrivateKeyPath = FileSystems.getDefault().getPath("./private_key");
  private static final String mRemoteDir = ".";
  private final Executor mExecutor = Executors.newSingleThreadExecutor();

  @Test
  public void createShouldCreateEnvironmentForEveryHost() throws IOException {
    Collection<String> hosts = new ArrayList<>();
    hosts.add("localhost");
    hosts.add("127.0.0.1");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(
        hosts,
        mUsername,
        mPrivateKeyPath,
        mRemoteDir,
        mExecutor);

    Collection<Environment> envs = sshEnvironmentFactory.create();

    final int expectedEnvironmentCount = 2;
    assertEquals(expectedEnvironmentCount, envs.size());
    int currentId = 0;
    for (Environment env : envs) {
      assertEquals(currentId, env.getId());
      assertEquals("localhost", env.getHostname());
      currentId += 1;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void createShouldThrowExceptionOnEmptyHostsList() throws IOException {
    Collection<String> hosts = new ArrayList<>();
    new SSHEnvironmentFactory(
        hosts,
        mUsername,
        mPrivateKeyPath,
        mRemoteDir,
        mExecutor);
  }

  @Test
  public void destroyShouldRemoveRemoteDirDirectory() throws IOException, InterruptedException {
    Collection<String> hosts = new ArrayList<>();
    hosts.add("localhost");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(
        hosts,
        mUsername,
        mPrivateKeyPath,
        mRemoteDir,
        mExecutor);

    SSHEnvironment sshEnvironment = mock(SSHEnvironment.class);
    when(sshEnvironment.getRemoteHomePath()).thenReturn(mRemoteDir);

    Collection<Environment> envs = new ArrayList<>();
    envs.add(sshEnvironment);

    sshEnvironmentFactory.destroy(envs);

    List<String> expectedCommand = new ArrayList<>();
    expectedCommand.add("rmdir");
    expectedCommand.add(mRemoteDir);
    verify(sshEnvironment).runCommand(eq(expectedCommand), eq("."));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void destroyShouldNotThrowException() throws IOException {
    Collection<String> hosts = new ArrayList<>();
    hosts.add("localhost");
    hosts.add("127.0.0.1");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(
        hosts,
        mUsername,
        mPrivateKeyPath,
        mRemoteDir,
        mExecutor);

    SSHEnvironment sshEnvironment = mock(SSHEnvironment.class);
    when(sshEnvironment.getRemoteHomePath()).thenReturn(mRemoteDir);
    when(sshEnvironment.runCommand(anyList(), anyString())).thenReturn(1);

    Collection<Environment> envs = new ArrayList<>();
    envs.add(sshEnvironment);

    sshEnvironmentFactory.destroy(envs);
  }
}
