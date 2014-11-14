package me.gregorias.dfuntest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class SSHEnvironmentFactoryTest {
  @Test
  public void createShouldCreateEnvironmsentForEveryHost() throws IOException {
    Configuration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactory.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactory.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactory.XML_PRIVATE_KEY_FIELD, "./private_key");
    config.setProperty(SSHEnvironmentFactory.XML_REMOTE_DIR_FIELD, ".");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(config);
    Collection<Environment> envs = sshEnvironmentFactory.createEnvironments();

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
  public void createShouldThrowExceptionOnEmptyHostsField() throws IOException {
    Configuration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactory.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactory.XML_PRIVATE_KEY_FIELD, "./private_key");
    config.setProperty(SSHEnvironmentFactory.XML_REMOTE_DIR_FIELD, ".");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(config);
    sshEnvironmentFactory.createEnvironments();
  }

  @Test(expected = IllegalArgumentException.class)
  public void createShouldThrowExceptionOnEmptyUsernameField() throws IOException {
    Configuration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactory.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactory.XML_PRIVATE_KEY_FIELD, "./private_key");
    config.setProperty(SSHEnvironmentFactory.XML_REMOTE_DIR_FIELD, ".");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(config);
    sshEnvironmentFactory.createEnvironments();
  }

  @Test
  public void destroyShouldNotThrowException() throws IOException {
    Configuration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactory.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactory.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactory.XML_PRIVATE_KEY_FIELD, "./private_key");
    config.setProperty(SSHEnvironmentFactory.XML_REMOTE_DIR_FIELD, ".");
    SSHEnvironmentFactory sshEnvironmentFactory = new SSHEnvironmentFactory(config);
    Collection<Environment> envs = sshEnvironmentFactory.createEnvironments();
    sshEnvironmentFactory.destroyEnvironments(envs);
  }
}
