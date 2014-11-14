package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class LocalEnvironmentFactoryTest {
  @Test
  public void createEnvironmentsShouldCreateLocalEnvironments() throws IOException {
    final int envCount = 5;
    final String dirPrefix = "unittest";
    Configuration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactory.XML_ENV_CNT_FIELD, envCount);
    config.setProperty(LocalEnvironmentFactory.XML_DIR_PREFIX_FIELD, dirPrefix);

    FileUtils mockFileUtils = mock(FileUtils.class);
    Path path = mock(Path.class);
    when(mockFileUtils.createTempDirectory(eq(dirPrefix))).thenReturn(path);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(config, mockFileUtils);
    Collection<Environment> envs = factory.createEnvironments();
    verify(mockFileUtils, times(envCount)).createTempDirectory(eq(dirPrefix));
    assertEquals(envCount, envs.size());
    factory.destroyEnvironments(envs);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createEnvironmentsShouldThrowExceptionOnZeroEnvCount() throws IOException {
    final int envCount = 0;
    final String dirPrefix = "unittest";
    Configuration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactory.XML_ENV_CNT_FIELD, envCount);
    config.setProperty(LocalEnvironmentFactory.XML_DIR_PREFIX_FIELD, dirPrefix);

    FileUtils mockFileUtils = mock(FileUtils.class);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(config, mockFileUtils);
    factory.createEnvironments();
  }

  @Test
  public void destroyEnvironmentsShouldDisposeOfCreatedEnvironmentsDirectory() throws IOException {
    final int envCount = 5;
    final String dirPrefix = "unittest";
    Configuration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactory.XML_ENV_CNT_FIELD, envCount);
    config.setProperty(LocalEnvironmentFactory.XML_DIR_PREFIX_FIELD, dirPrefix);

    FileUtils mockFileUtils = mock(FileUtils.class);
    Path path = mock(Path.class);
    when(mockFileUtils.createTempDirectory(eq(dirPrefix))).thenReturn(path);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(config, mockFileUtils);
    Collection<Environment> envs = factory.createEnvironments();
    factory.destroyEnvironments(envs);
    verify(mockFileUtils, times(envCount)).deleteQuietly(eq(path.toFile()));
  }

  @Test
  public void destroyEnvironmentsShouldNotDoAnythingWhenGivenWrongEnvironment() throws IOException {
    final int envCount = 0;
    final String dirPrefix = "unittest";
    Configuration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactory.XML_ENV_CNT_FIELD, envCount);
    config.setProperty(LocalEnvironmentFactory.XML_DIR_PREFIX_FIELD, dirPrefix);
    FileUtils mockFileUtils = mock(FileUtils.class);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(config, mockFileUtils);
    Collection<Environment> envs = new LinkedList<>();
    Environment env = mock(Environment.class);
    when(env.getProperty(anyString())).thenThrow(NoSuchElementException.class);
    envs.add(env);
    factory.destroyEnvironments(envs);
    verify(mockFileUtils, never()).deleteQuietly(any(File.class));
  }
}
