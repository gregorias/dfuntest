package me.gregorias.dfuntest;

import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class SSHEnvironmentFactoryBuilderTest {
  private final SSHEnvironmentFactoryBuilder mFFactory = new SSHEnvironmentFactoryBuilder();
  private final Executor mMockExecutor = mock(Executor.class);

  @Test
  public void newEnvironmentFactoryShouldCreateNewFactory() throws IOException {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_REMOTE_DIR_FIELD, "remote-dir");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_PRIVATE_KEY_PATH_FIELD, "private-key-path");
    EnvironmentFactory factory = mFFactory.newEnvironmentFactory(config, mMockExecutor);
    assertThat(factory, instanceOf(SSHEnvironmentFactory.class));
    int expectedEnvironmentsSize = 2;
    assertEquals(expectedEnvironmentsSize, factory.create().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void newEnvironmentFactoryShouldThrowExceptionOnHostsMissing() throws IOException {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_REMOTE_DIR_FIELD, "remote-dir");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_PRIVATE_KEY_PATH_FIELD, "private-key-path");
    mFFactory.newEnvironmentFactory(config, mMockExecutor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void newEnvironmentFactoryShouldThrowExceptionOnUsernameMissing() throws IOException {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_REMOTE_DIR_FIELD, "remote-dir");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_PRIVATE_KEY_PATH_FIELD, "private-key-path");
    mFFactory.newEnvironmentFactory(config, mMockExecutor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void newEnvironmentFactoryShouldThrowExceptionOnRemoteDirMissing() throws IOException {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_PRIVATE_KEY_PATH_FIELD, "private-key-path");
    mFFactory.newEnvironmentFactory(config, mMockExecutor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void newEnvironmentFactoryShouldThrowExceptionOnPrivateKeyPathMissing()
      throws IOException {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_HOSTS_FIELD, "localhost, 127.0.0.1");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_USERNAME_FIELD, "username");
    config.setProperty(SSHEnvironmentFactoryBuilder.XML_REMOTE_DIR_FIELD, "remote-dir");
    mFFactory.newEnvironmentFactory(config, mMockExecutor);
  }
}
