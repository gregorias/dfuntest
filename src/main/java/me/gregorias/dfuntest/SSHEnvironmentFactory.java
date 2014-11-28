package me.gregorias.dfuntest;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
  public static final String HOSTS_ARGUMENT_NAME = "SSHEnvironmentFactory.hosts";
  public static final String USERNAME_ARGUMENT_NAME = "SSHEnvironmentFactory.username";
  public static final String PRIVATE_KEY_PATH = "SSHEnvironmentFactory.privateKeyPath";
  public static final String REMOTE_DIR_ARGUMENT_NAME = "SSHEnvironmentFactory.remoteDir";
  public static final String EXECUTOR_ARGUMENT_NAME = "SSHEnvironmentFactory.executor";
  private static final Logger LOGGER = LoggerFactory.getLogger(SSHEnvironmentFactory.class);

  private final List<String> mHostnames;
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
  @Inject
  public SSHEnvironmentFactory(
      @Named(HOSTS_ARGUMENT_NAME) Collection<String> hosts,
      @Named(USERNAME_ARGUMENT_NAME) String username,
      @Named(PRIVATE_KEY_PATH) Path privateKeyPath,
      @Named(REMOTE_DIR_ARGUMENT_NAME) String remoteDir,
      @Named(EXECUTOR_ARGUMENT_NAME) Executor executor) {
    if (hosts.size() == 0) {
      throw new IllegalArgumentException("Hosts collection is empty.");
    }
    mHostnames = new ArrayList<>(hosts);
    mUsername = username;
    mPrivateKeyPath = privateKeyPath;
    mRemoteDir = remoteDir;
    mExecutor = executor;
  }

  @Override
  public Collection<Environment> create() throws IOException {
    LOGGER.info("create()");

    Collection<Environment> environments = new ArrayList<>();
    for (int envIdx = 0; envIdx < mHostnames.size(); ++envIdx) {
      LOGGER.trace("create(): Setting up environment for host: {}.", mHostnames.get(envIdx));

      InetAddress hostInetAddress = InetAddress.getByName(mHostnames.get(envIdx));
      final Environment env = new SSHEnvironment(envIdx,
          mUsername,
          mPrivateKeyPath,
          hostInetAddress,
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