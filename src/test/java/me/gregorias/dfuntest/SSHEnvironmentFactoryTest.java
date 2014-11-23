package me.gregorias.dfuntest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class SSHEnvironmentFactoryTest {
  private static final String mUsername = "username";
  private static final Path mPrivateKeyPath = FileSystems.getDefault().getPath("./private_key");
  private static final String mRemoteDir = ".";
  private final Executor mExecutor = Executors.newSingleThreadExecutor();

  @Test
  public void createShouldCreateEnvironmentForEveryHost() throws IOException {
    Collection<InetAddress> hosts = new LinkedList<>();
    hosts.add(InetAddress.getByName("localhost"));
    hosts.add(InetAddress.getByName("127.0.0.1"));
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
    Collection<InetAddress> hosts = new LinkedList<>();
    new SSHEnvironmentFactory(
        hosts,
        mUsername,
        mPrivateKeyPath,
        mRemoteDir,
        mExecutor);
  }

  @Test
  public void destroyShouldNotThrowException() throws IOException {
    Collection<InetAddress> hosts = new LinkedList<>();
    hosts.add(InetAddress.getByName("localhost"));
    hosts.add(InetAddress.getByName("127.0.0.1"));
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(
        hosts,
        mUsername,
        mPrivateKeyPath,
        mRemoteDir,
        mExecutor);

    Collection<Environment> envs = sshEnvironmentFactory.create();
    sshEnvironmentFactory.destroy(envs);
  }
}
