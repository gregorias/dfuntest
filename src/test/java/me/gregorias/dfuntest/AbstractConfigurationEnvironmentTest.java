package me.gregorias.dfuntest;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

public class AbstractConfigurationEnvironmentTest {
  private AbstractConfigurationEnvironment mACEnv = null;

  @Before
  public void setUp() {
    mACEnv = new AbstractConfigurationEnvironmentImpl();
  }

  @Test
  public void shouldOverwriteProperty() {
    final String key = "A";
    final String firstValue = "B";
    final String secondValue = "C";
    mACEnv.setProperty(key, firstValue);
    mACEnv.setProperty(key, secondValue);
    assertEquals(secondValue, mACEnv.getProperty(key));
  }

  @Test
  public void shouldReturnProperty() {
    final String key = "A";
    final String value = "B";
    mACEnv.setProperty(key, value);
    assertEquals(value, mACEnv.getProperty(key));
  }

  @Test(expected = NoSuchElementException.class)
  public void shouldThrowException() {
    final String key = "A";
    mACEnv.getProperty(key);
  }

  private static class AbstractConfigurationEnvironmentImpl
      extends AbstractConfigurationEnvironment {
    @Override
    public void copyFilesFromLocalDisk(Path srcPath, String destRelPath) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void copyFilesToLocalDisk(String srcRelPath, Path destPath) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getHostname() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public RemoteProcess runCommand(List<String> command) throws InterruptedException,
      IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public RemoteProcess runCommandAsynchronously(List<String> command) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeFile(String relPath) throws InterruptedException, IOException {
      throw new UnsupportedOperationException();
    }
  }
}
