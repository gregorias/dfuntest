package me.gregorias.dfuntest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import me.gregorias.dfuntest.util.FileUtils;
import me.gregorias.dfuntest.util.FileUtilsImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalEnvironmentTest {
  @Rule
  public TemporaryFolder mTempFolder = new TemporaryFolder();

  private static final String PREFIX = "NEBTMP";
  private Path mEnvDir;
  private Path mLocalDir;

  private Environment mLocalEnvironment;

  @Before
  public void setUp() throws IOException {
    mEnvDir = mTempFolder.newFolder().toPath();
    mLocalDir = mTempFolder.newFolder().toPath();

    mLocalEnvironment = new LocalEnvironment(0, mEnvDir, FileUtilsImpl.getFileUtilsImpl());
  }

  @Test
  public void copyFilesFromLocalDiskShouldCreateDestinationDirectoryIfItDoesNotExist()
      throws IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    Path sourcePath = FileSystems.getDefault().getPath("sourcedir");
    String destPath = "targetdir";
    Path fullDestPath = mEnvDir.resolve(destPath);
    localEnvironment.copyFilesFromLocalDisk(sourcePath, destPath);
    verify(mockFileUtils).createDirectories(eq(fullDestPath));
  }

  @Test(expected = IOException.class)
  public void copyFilesFromLocalDiskShouldFailIfCreateDestinationDirectoriesFails()
      throws IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    Path sourcePath = FileSystems.getDefault().getPath("sourcedir");
    String destPath = "targetdir";
    Path fullDestPath = mEnvDir.resolve(destPath);
    doThrow(IOException.class).when(mockFileUtils).createDirectories(eq(fullDestPath));
    localEnvironment.copyFilesFromLocalDisk(sourcePath, destPath);
  }

  @Test(expected = IOException.class)
  public void copyFilesFromLocalDiskShouldFailIfDestinationIsNotADirectory() throws IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    Path sourcePath = FileSystems.getDefault().getPath("sourcedir");
    String destPath = "targetdir";
    Path fullDestPath = mEnvDir.resolve(destPath);
    when(mockFileUtils.exists(eq(fullDestPath))).thenReturn(true);
    when(mockFileUtils.isDirectory(eq(fullDestPath))).thenReturn(false);
    localEnvironment.copyFilesFromLocalDisk(sourcePath, destPath);
  }

  @Test
  public void copyFilesToLocalDiskShouldCreateDestinationDirectoryIfItDoesNotExist()
      throws IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    Path targetPath = FileSystems.getDefault().getPath("targetdir");
    localEnvironment.copyFilesToLocalDisk("unittestdir", targetPath);
    verify(mockFileUtils).createDirectories(targetPath);
  }

  @Test(expected = IOException.class)
  public void copyFilesToLocalDiskShouldFailIfCreateDestinationDirectoriesFails()
      throws IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    Path targetPath = FileSystems.getDefault().getPath("targetdir");
    doThrow(IOException.class).when(mockFileUtils).createDirectories(eq(targetPath));
    localEnvironment.copyFilesToLocalDisk("unittestdir", targetPath);
  }

  @Test(expected = IOException.class)
  public void copyFilesToLocalDiskShouldFailIfDestinationIsNotADirectory() throws IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    Path targetPath = FileSystems.getDefault().getPath("targetdir");
    when(mockFileUtils.exists(eq(targetPath))).thenReturn(true);
    when(mockFileUtils.isDirectory(eq(targetPath))).thenReturn(false);
    localEnvironment.copyFilesToLocalDisk("unittestdir", targetPath);
  }

  @Test
  public void getHostnameShouldReturnLocalhost() {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    assertEquals("localhost", localEnvironment.getHostname());
  }

  @Test
  public void getNameShouldReturnHomePathString() {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);
    assertEquals(mEnvDir.toAbsolutePath().toString(), localEnvironment.getName());
  }

  @Test
  public void runCommandShouldRunProcess() throws CommandException, InterruptedException,
      IOException {
    FileUtils mockFileUtils = mock(FileUtils.class);
    Environment localEnvironment = new LocalEnvironment(0, mEnvDir, mockFileUtils);

    List<String> commands = new LinkedList<>();
    commands.add("touch");
    commands.add(PREFIX);

    Process mockProcess = mock(Process.class);
    int returnValue = 0;
    when(mockProcess.exitValue()).thenReturn(returnValue);
    when(mockFileUtils.runCommand(eq(commands), eq(mEnvDir.toFile()))).thenReturn(mockProcess);

    RemoteProcess finishedProcess = localEnvironment.runCommand(commands);
    verify(mockFileUtils).runCommand(eq(commands), eq(mEnvDir.toFile()));
    assertEquals(returnValue, finishedProcess.waitFor());
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
