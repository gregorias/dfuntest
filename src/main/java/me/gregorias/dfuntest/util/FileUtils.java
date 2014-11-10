package me.gregorias.dfuntest.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// This interface was created for testing purposes.
/**
 * Interface for local file utilities.
 */
public interface FileUtils {
  /**
   * {@link org.apache.commons.io.FileUtils#copyDirectoryToDirectory(java.io.File, java.io.File)}
   *
   * @param from Source path
   * @param to Target directory path
   */
  void copyDirectoryToDirectory(File from, File to) throws IOException;

  /**
   * {@link org.apache.commons.io.FileUtils#copyFileToDirectory(java.io.File, java.io.File)}
   *
   * @param from Source path
   * @param to Target directory path
   */
  void copyFileToDirectory(File from, File to) throws IOException;

  /**
   * {@link java.nio.file.Files#createDirectories(java.nio.file.Path,
   *   java.nio.file.attribute.FileAttribute[])}
   *
   * @param path Path to create
   */
  void createDirectories(Path path) throws IOException;

  /**
   * {@link org.apache.commons.io.FileUtils#deleteQuietly(java.io.File)}
   *
   * @param file File to delete
   * @return true iff directory was deleted
   */
  boolean deleteQuietly(File file);

  /**
   * {@link java.nio.file.Files#exists(java.nio.file.Path, java.nio.file.LinkOption...)}
   *
   * @param path path to check
   * @return true iff exists
   */
  boolean exists(Path path);

  /**
   * {@link java.nio.file.Files#isDirectory(java.nio.file.Path, java.nio.file.LinkOption...)}
   *
   * @param path path to check
   * @return true iff path is a directory
   */
  boolean isDirectory(Path path);

  /**
   * {@link java.lang.ProcessBuilder}
   *
   * @param command command to run
   * @param pwdFile working directory of the process
   * @return process running the command
   */
  Process runCommand(List<String> command, File pwdFile) throws IOException;
}