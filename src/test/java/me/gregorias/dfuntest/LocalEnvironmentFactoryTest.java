package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
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

    FileUtils mockFileUtils = mock(FileUtils.class);
    Path path = mock(Path.class);
    when(mockFileUtils.createTempDirectory(eq(dirPrefix))).thenReturn(path);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(envCount,
        dirPrefix,
        mockFileUtils);
    Collection<Environment> envs = factory.create();
    verify(mockFileUtils, times(envCount)).createTempDirectory(eq(dirPrefix));
    assertEquals(envCount, envs.size());
    int currentId = 0;
    for (Environment env : envs) {
      assertEquals(currentId, env.getId());
      currentId += 1;
    }
    factory.destroy(envs);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorShouldThrowExceptionOnZeroEnvCount() throws IOException {
    final int envCount = 0;
    final String dirPrefix = "unittest";

    FileUtils mockFileUtils = mock(FileUtils.class);

    new LocalEnvironmentFactory(envCount, dirPrefix, mockFileUtils);
  }

  @Test
  public void destroyEnvironmentsShouldDisposeOfCreatedEnvironmentsDirectory() throws IOException {
    final int envCount = 5;
    final String dirPrefix = "unittest";

    FileUtils mockFileUtils = mock(FileUtils.class);
    Path path = mock(Path.class);
    when(mockFileUtils.createTempDirectory(eq(dirPrefix))).thenReturn(path);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(envCount,
        dirPrefix,
        mockFileUtils);
    Collection<Environment> envs = factory.create();
    factory.destroy(envs);
    verify(mockFileUtils, times(envCount)).deleteQuietly(eq(path.toFile()));
  }

  @Test
  public void destroyEnvironmentsShouldNotDoAnythingWhenGivenWrongEnvironment() throws IOException {
    final int envCount = 1;
    final String dirPrefix = "unittest";
    FileUtils mockFileUtils = mock(FileUtils.class);

    LocalEnvironmentFactory factory = new LocalEnvironmentFactory(envCount,
        dirPrefix,
        mockFileUtils);
    Collection<Environment> envs = new LinkedList<>();
    Environment env = mock(Environment.class);
    when(env.getProperty(anyString())).thenThrow(NoSuchElementException.class);
    envs.add(env);
    factory.destroy(envs);
    verify(mockFileUtils, never()).deleteQuietly(any(File.class));
  }
}
