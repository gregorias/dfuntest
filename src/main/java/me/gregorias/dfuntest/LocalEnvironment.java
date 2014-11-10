package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Environment on local host with specified home path
 */
public class LocalEnvironment extends AbstractConfigurationEnvironment {
  private final int mId;
  private final Path mDir;
  private final FileUtils mFileUtils;

  /**
   * @param id Environment's id
   * @param dir Environment's home directory
   * @param fileUtils file utilities to use
   */
  public LocalEnvironment(int id, Path dir, FileUtils fileUtils) {
    super();
    mId = id;
    mDir = dir;
    mFileUtils = fileUtils;
  }

  @Override
  public void copyFilesFromLocalDisk(Path srcPath, String destRelPath) throws IOException {
    Path destPath = mDir.resolve(destRelPath);
    if (!mFileUtils.exists(destPath)) {
      mFileUtils.createDirectories(destPath);
    } else if (!mFileUtils.isDirectory(destPath)) {
      throw new IOException("Destination path exists and it is not a directory.");
    }
    if (mFileUtils.isDirectory(srcPath)) {
      mFileUtils.copyDirectoryToDirectory(srcPath.toFile(), destPath.toFile());
    } else {
      mFileUtils.copyFileToDirectory(srcPath.toFile(), destPath.toFile());
    }
  }

  @Override
  public void copyFilesToLocalDisk(String srcRelPath, Path destPath) throws IOException {
    Path srcPath = mDir.resolve(srcRelPath);
    if (!mFileUtils.exists(destPath)) {
      mFileUtils.createDirectories(destPath);
    } else if (!mFileUtils.isDirectory(destPath)) {
      throw new IOException(
          "Destination path exists and it is not a directory.");
    }
    if (mFileUtils.isDirectory(srcPath)) {
      mFileUtils.copyDirectoryToDirectory(srcPath.toFile(), destPath.toFile());
    } else {
      mFileUtils.copyFileToDirectory(srcPath.toFile(), destPath.toFile());
    }
  }

  @Override
  public String getHostname() {
    return "localhost";
  }

  @Override
  public int getId() {
    return mId;
  }

  @Override
  public String getName() {
    return mDir.toAbsolutePath().toString();
  }

  @Override
  public RemoteProcess runCommand(List<String> command) throws InterruptedException, IOException {
    RemoteProcess process = runCommandAsynchronously(command);
    process.waitFor();
    return process;
  }

  @Override
  public RemoteProcess runCommandAsynchronously(List<String> command) throws IOException {
    Process process = mFileUtils.runCommand(command, mDir.toFile());
    return new ProcessAdapter(process);
  }

  @Override
  public void removeFile(String relPath) {
    mFileUtils.deleteQuietly(mDir.resolve(relPath).toFile());
  }

  private static class ProcessAdapter implements RemoteProcess {
    private final Process mProcess;

    public ProcessAdapter(Process process) {
      mProcess = process;
    }

    @Override
    public void destroy() {
      mProcess.destroy();
    }

    @Override
    public InputStream getErrorStream() {
      return mProcess.getErrorStream();
    }

    @Override
    public InputStream getInputStream() {
      return mProcess.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
      return mProcess.getOutputStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
      return mProcess.waitFor();
    }
  }
}
