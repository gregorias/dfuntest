package me.gregorias.dfuntest;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.gregorias.dfuntest.util.FileUtilsImpl;
import me.gregorias.dfuntest.util.SSHClientFactory;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of {@link SSHEnvironment}.
 *
 * This factory uses {@link Configuration} to specify environment configuration.
 * Configuration uses following fields:
 * <ul>
 * <li> hosts - List of hosts to use. </li>
 * <li> username - Remote username. </li>
 * <li> private-key - Path to private key to use. </li>
 * <li> remote-dir - Directory to use on remote system. </li>
 * </ul>
 *
 * @author Grzegorz Milka
 */
public class SSHEnvironmentFactory implements EnvironmentFactory<Environment> {
  public static final String XML_HOSTS_FIELD = "hosts";
  public static final String XML_USERNAME_FIELD = "username";
  public static final String XML_PRIVATE_KEY_FIELD = "private-key";
  public static final String XML_REMOTE_DIR_FIELD = "remote-dir";
  private static final Logger LOGGER = LoggerFactory.getLogger(SSHEnvironmentFactory.class);
  private static final String DEFAULT_REMOTE_DIR = "";

  private final Configuration mConfig;
  private final Executor mExecutor;

  public SSHEnvironmentFactory(Configuration config) {
    mConfig = config;
    /* TODO Externalize */
    mExecutor = Executors.newFixedThreadPool(100);
  }

  @Override
  public Collection<Environment> create() throws IOException {
    LOGGER.info("create()");
    List<Object> hosts = mConfig.getList(XML_HOSTS_FIELD);
    if (null == hosts || hosts.isEmpty()) {
      throw new IllegalArgumentException("Hosts field does not exist or is empty.");
    }

    String username = mConfig.getString(XML_USERNAME_FIELD);
    String privateKeyString = mConfig.getString(XML_PRIVATE_KEY_FIELD);
    String remoteDir = mConfig.getString(XML_REMOTE_DIR_FIELD, DEFAULT_REMOTE_DIR);
    if (null == username || null == privateKeyString || null == remoteDir) {
      throw new IllegalArgumentException("Some configuration fields are missing.");
    }
    Path privateKeyPath = FileSystems.getDefault().getPath(privateKeyString);

    Collection<Environment> environments = new LinkedList<>();
    for (int envIdx = 0; envIdx < hosts.size(); ++envIdx) {
      LOGGER.trace("create(): Setting up environment for host: {}.",
          hosts.get(envIdx).toString());
      final Environment env = new SSHEnvironment(envIdx,
        username,
        privateKeyPath,
        InetAddress.getByName(hosts.get(envIdx).toString()),
        remoteDir,
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