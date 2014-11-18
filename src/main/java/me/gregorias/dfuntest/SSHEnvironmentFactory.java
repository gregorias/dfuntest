package me.gregorias.dfuntest;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import me.gregorias.dfuntest.util.FileUtilsImpl;
import me.gregorias.dfuntest.util.SSHClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of {@link SSHEnvironment}.
 *
 * @author Grzegorz Milka
 */
public class SSHEnvironmentFactory implements EnvironmentFactory<Environment> {
  public static final String XML_HOSTS_FIELD = "hosts";
  public static final String XML_USERNAME_FIELD = "username";
  public static final String XML_PRIVATE_KEY_FIELD = "private-key";
  public static final String XML_REMOTE_DIR_FIELD = "remote-dir";
  private static final Logger LOGGER = LoggerFactory.getLogger(SSHEnvironmentFactory.class);

  private final List<InetAddress> mHosts;
  private final String mUsername;
  private final Path mPrivateKeyPath;
  private final String mRemoteDir;
  private final Executor mExecutor;

  /**
   * @param hosts list of remote hosts
   * @param username ssh account username
   * @param privateKeyPath path to private key file
   * @param remoteDir name of remote directory to put files to
   * @param executor executor for ssh background execution of ssh tasks
   */
  public SSHEnvironmentFactory(
      Collection<InetAddress> hosts,
      String username,
      Path privateKeyPath,
      String remoteDir,
      Executor executor) {
    if (hosts.size() == 0) {
      throw new IllegalArgumentException("Hosts collection is empty.");
    }
    mHosts = new ArrayList<>(hosts);
    mUsername = username;
    mPrivateKeyPath = privateKeyPath;
    mRemoteDir = remoteDir;
    mExecutor = executor;
  }

  @Override
  public Collection<Environment> create() throws IOException {
    LOGGER.info("create()");

    Collection<Environment> environments = new LinkedList<>();
    for (int envIdx = 0; envIdx < mHosts.size(); ++envIdx) {
      LOGGER.trace("create(): Setting up environment for host: {}.", mHosts.get(envIdx).toString());
      final Environment env = new SSHEnvironment(envIdx,
          mUsername,
          mPrivateKeyPath,
          mHosts.get(envIdx),
          mRemoteDir,
          mExecutor,
          SSHClientFactory.getSSHClientFactory(),
          FileUtilsImpl.getFileUtilsImpl());
      environments.add(env);
    }
    return environments;
  }

  @Override
  public void destroy(Collection<Environment> envs) {
    LOGGER.info("destroy()");
  }
}