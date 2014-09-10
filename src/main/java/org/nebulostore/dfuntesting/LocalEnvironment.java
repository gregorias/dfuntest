package org.nebulostore.dfuntesting;

import java.io.IOException;
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
  public void copyFilesFromLocalDisk(Path srcPath, Path destRelPath) throws IOException {
    Path destPath = mDir.resolve(destRelPath);
    if (!Files.exists(destPath)) {
      Files.createDirectory(destPath);
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
  public void copyFilesToLocalDisk(Path srcRelPath, Path destPath) throws IOException {
    Path srcPath = mDir.resolve(srcRelPath);
    if (!Files.exists(destPath)) {
      Files.createDirectory(destPath);
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
  public Process runCommand(List<String> command) throws
      CommandException,
      InterruptedException {
    ProcessBuilder pb = new ProcessBuilder();
    pb.command(command);
    pb.directory(mDir.toFile());
    Process process;
    try {
      process = pb.start();
    } catch (IOException e) {
      throw new CommandException(e);
    }
    process.waitFor();
    return process;
  }

  @Override
  public Process runCommandAsynchronously(List<String> command) throws
      CommandException {
    ProcessBuilder pb = new ProcessBuilder();
    pb.command(command);
    pb.directory(mDir.toFile());
    Process process;
    try {
      process = pb.start();
    } catch (IOException e) {
      throw new CommandException(e);
    }
    return process;
  }

  @Override
  public void removeFile(Path relPath) {
    FileUtils.deleteQuietly(mDir.resolve(relPath).toFile());
  }
}
