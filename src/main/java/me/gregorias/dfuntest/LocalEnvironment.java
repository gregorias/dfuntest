package me.gregorias.dfuntest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class LocalEnvironment extends AbstractConfigurationEnvironment {
  private final int mId;
  private final Path mDir;

  public LocalEnvironment(int id, Path dir) {
    super();
    mId = id;
    mDir = dir;
  }

  @Override
  public void copyFilesFromLocalDisk(Path srcPath, String destRelPath) throws IOException {
    Path destPath = mDir.resolve(destRelPath);
    if (!Files.exists(destPath)) {
      Files.createDirectories(destPath);
    } else if (!Files.isDirectory(destPath)) {
      throw new IOException(
          "Destination path exists and it is not a directory.");
    }
    if (Files.isDirectory(srcPath)) {
      FileUtils.copyDirectoryToDirectory(srcPath.toFile(), destPath.toFile());
    } else {
      FileUtils.copyFileToDirectory(srcPath.toFile(), destPath.toFile());
    }
  }

  @Override
  public void copyFilesToLocalDisk(String srcRelPath, Path destPath) throws IOException {
    Path srcPath = mDir.resolve(srcRelPath);
    if (!Files.exists(destPath)) {
      Files.createDirectories(destPath);
    } else if (!Files.isDirectory(destPath)) {
      throw new IOException(
          "Destination path exists and it is not a directory.");
    }
    if (Files.isDirectory(srcPath)) {
      FileUtils.copyDirectoryToDirectory(srcPath.toFile(), destPath.toFile());
    } else {
      FileUtils.copyFileToDirectory(srcPath.toFile(), destPath.toFile());
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
    ProcessBuilder pb = new ProcessBuilder();
    pb.command(command);
    pb.directory(mDir.toFile());
    Process process;
    process = pb.start();
    return new ProcessAdapter(process);
  }

  @Override
  public void removeFile(String relPath) {
    FileUtils.deleteQuietly(mDir.resolve(relPath).toFile());
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
