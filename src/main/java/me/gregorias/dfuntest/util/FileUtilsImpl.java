package me.gregorias.dfuntest.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Concrete implementation of {@link FileUtils}
 */
public class FileUtilsImpl implements FileUtils {
  private static FileUtilsImpl FILE_UTILS_IMPL = new FileUtilsImpl();

  @Override
  public void copyDirectoryToDirectory(File from, File to) throws IOException {
    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(from, to);
  }

  @Override
  public void copyFileToDirectory(File from, File to) throws IOException {
    org.apache.commons.io.FileUtils.copyFileToDirectory(from, to);
  }

  @Override
  public void createDirectories(Path path) throws IOException {
    Files.createDirectories(path);
  }

  @Override
  public Path createTempDirectory(String dirPrefix) throws IOException {
    return Files.createTempDirectory(dirPrefix);
  }

  @Override
  public boolean deleteQuietly(File file) {
    return org.apache.commons.io.FileUtils.deleteQuietly(file);
  }

  @Override
  public boolean exists(Path path) {
    return Files.exists(path);
  }

  public static FileUtilsImpl getFileUtilsImpl() {
    return FILE_UTILS_IMPL;
  }

  @Override
  public boolean isDirectory(Path path) {
    return Files.isDirectory(path);
  }

  @Override
  public Process runCommand(List<String> command, File pwdFile) throws IOException {
    ProcessBuilder pb = new ProcessBuilder();
    pb.command(command).directory(pwdFile);
    return pb.start();
  }
}
