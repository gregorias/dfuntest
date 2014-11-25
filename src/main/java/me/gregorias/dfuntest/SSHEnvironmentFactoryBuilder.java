package me.gregorias.dfuntest;

import org.apache.commons.configuration.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Builder of SSHEnvironmentFactory. It expects a configuration with following properties:
 *
 * <ul>
 *   <li> hosts - list of host addresses</li>
 *   <li> username - username of SSH account</li>
 *   <li> private-key-path - path to private key file</li>
 *   <li> remote-dir - remote directory of created environments</li>
 * </ul>
 */
public class SSHEnvironmentFactoryBuilder implements EnvironmentFactoryBuilder<Environment> {
  public static final String XML_HOSTS_FIELD = "hosts";
  public static final String XML_USERNAME_FIELD = "username";
  public static final String XML_PRIVATE_KEY_PATH_FIELD = "private-key-path";
  public static final String XML_REMOTE_DIR_FIELD = "remote-dir";

  @Override
  public SSHEnvironmentFactory newEnvironmentFactory(
      Configuration configuration,
      Executor executor) throws IOException {
    List<Object> hostsList = configuration.getList(XML_HOSTS_FIELD);
    String username = configuration.getString(XML_USERNAME_FIELD);
    String privateKeyPathString = configuration.getString(XML_PRIVATE_KEY_PATH_FIELD);
    String remoteDir = configuration.getString(XML_REMOTE_DIR_FIELD);

    if (hostsList.size() == 0
        || null == username
        || null == privateKeyPathString
        || null == remoteDir) {
      throw new IllegalArgumentException("One of the required fields is missing or is in"
          + " wrong format.");
    }

    Collection<InetAddress> hosts = new ArrayList<>();
    for (Object host : hostsList) {
      hosts.add(InetAddress.getByName((String) host));
    }
    Path privateKeyPath = FileSystems.getDefault().getPath(privateKeyPathString);

    return new SSHEnvironmentFactory(
        hosts,
        username,
        privateKeyPath,
        remoteDir,
        executor);
  }
}
