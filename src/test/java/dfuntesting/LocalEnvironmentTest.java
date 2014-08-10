package dfuntesting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nebulostore.dfuntesting.CommandException;
import org.nebulostore.dfuntesting.CommandResult;
import org.nebulostore.dfuntesting.Environment;
import org.nebulostore.dfuntesting.LocalEnvironment;

public class LocalEnvironmentTest {
  private static final String PREFIX = "NEBTMP";
  private Path envDir_;
  private Path localDir_;
  
  private Environment localEnvironment_;

  @Before
  public void setUp() throws IOException {
    envDir_ = Files.createTempDirectory(null);
    localDir_ = Files.createTempDirectory(null);
    
    localEnvironment_ = new LocalEnvironment(0, envDir_);
    
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(localDir_.toFile());
    FileUtils.deleteQuietly(envDir_.toFile());
  }
  
  @Test 
  public void shouldCreateFileUsingTouchProcess() throws CommandException, InterruptedException, IOException {
    List<String> commands = new LinkedList<>();
    commands.add("touch");
    commands.add(PREFIX);
    
    CommandResult result = localEnvironment_.runCommand(commands);
    Path envFile = envDir_.resolve(PREFIX);
    assertTrue(result.getExitCode() == 0);
    assertTrue(Files.exists(envFile));
    Files.deleteIfExists(envFile);
  }
  
  @Test
  public void shouldCopyDirFromLocalDir() throws IOException {
    Path localFile = Files.createTempDirectory(localDir_, PREFIX);
    Path envFile = localDir_.resolve(localFile.getFileName());
    localEnvironment_.copyFilesFromLocalDisk(localFile, FileSystems.getDefault().getPath("."));
    assertTrue(Files.exists(envFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }
  
  @Test
  public void shouldCopyFileFromLocalDir() throws IOException {
    Path localFile = Files.createTempFile(localDir_, PREFIX, "");
    Path envFile = localDir_.resolve(localFile.getFileName());
    localEnvironment_.copyFilesFromLocalDisk(localFile, FileSystems.getDefault().getPath("."));
    assertTrue(Files.exists(envFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }
  
  @Test
  public void shouldCopyDirToLocalDir() throws IOException {
    Path envFile = Files.createTempDirectory(envDir_, PREFIX);
    Path localFile = localDir_.resolve(envFile.getFileName());
    localEnvironment_.copyFilesToLocalDisk(envFile.getFileName(), localDir_);
    assertTrue(Files.exists(localFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }
  
  @Test
  public void shouldCopyFileToLocalDir() throws IOException {
    Path envFile = Files.createTempFile(envDir_, PREFIX, "");
    Path localFile = localDir_.resolve(envFile.getFileName());
    localEnvironment_.copyFilesToLocalDisk(envFile.getFileName(), localDir_);
    assertTrue(Files.exists(localFile));
    Files.deleteIfExists(envFile);
    Files.deleteIfExists(localFile);
  }
  
  @Test
  public void shouldRemoveFile() throws IOException {
    Path envFile = Files.createTempFile(envDir_, PREFIX, "");
    assertTrue(Files.exists(envFile));
    localEnvironment_.removeFile(envFile.getFileName());
    assertFalse(Files.exists(envFile));
  }
  
}
