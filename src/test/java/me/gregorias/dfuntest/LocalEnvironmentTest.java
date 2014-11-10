package me.gregorias.dfuntest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LocalEnvironmentTest {
  private static final String PREFIX = "NEBTMP";
  private Path mEnvDir;
  private Path mLocalDir;

  private Environment mLocalEnvironment;

  @Before
  public void setUp() throws IOException {
    mEnvDir = Files.createTempDirectory(null);
    mLocalDir = Files.createTempDirectory(null);

    mLocalEnvironment = new LocalEnvironment(0, mEnvDir);
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(mLocalDir.toFile());
    FileUtils.deleteQuietly(mEnvDir.toFile());
  }

  @Test
  public void shouldCreateFileUsingTouchProcess() throws
      CommandException,
      InterruptedException,
      IOException {
    List<String> commands = new LinkedList<>();
    commands.add("touch");
    commands.add(PREFIX);

    RemoteProcess finishedProcess = mLocalEnvironment.runCommand(commands);
    Path envFile = mEnvDir.resolve(PREFIX);
    assertTrue(finishedProcess.waitFor() == 0);
    assertTrue(Files.exists(envFile));
    Files.deleteIfExists(envFile);
  }

  @Test
  public void shouldCopyDirFromLocalDir() throws IOException {
    Path localFile = Files.createTempDirectory(mLocalDir, PREFIX);
    Path envFile = mLocalDir.resolve(localFile.getFileName());
    mLocalEnvironment.copyFilesFromLocalDisk(localFile, ".");
    assertTrue(Files.exists(envFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }

  @Test
  public void shouldCopyFileFromLocalDir() throws IOException {
    Path localFile = Files.createTempFile(mLocalDir, PREFIX, "");
    Path envFile = mLocalDir.resolve(localFile.getFileName());
    mLocalEnvironment.copyFilesFromLocalDisk(localFile, ".");
    assertTrue(Files.exists(envFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }

  @Test
  public void shouldCopyDirToLocalDir() throws IOException {
    Path envFile = Files.createTempDirectory(mEnvDir, PREFIX);
    Path localFile = mLocalDir.resolve(envFile.getFileName());
    mLocalEnvironment.copyFilesToLocalDisk(envFile.toString(), mLocalDir);
    assertTrue(Files.exists(localFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }

  @Test
  public void shouldCopyFileToLocalDir() throws IOException {
    Path envFile = Files.createTempFile(mEnvDir, PREFIX, "");
    Path localFile = mLocalDir.resolve(envFile.getFileName());
    mLocalEnvironment.copyFilesToLocalDisk(envFile.toString(), mLocalDir);
    assertTrue(Files.exists(localFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }

  @Test
  public void shouldRemoveFile() throws InterruptedException, IOException {
    Path envFile = Files.createTempFile(mEnvDir, PREFIX, "");
    assertTrue(Files.exists(envFile));
    mLocalEnvironment.removeFile(envFile.toString());
    assertFalse(Files.exists(envFile));
  }

}