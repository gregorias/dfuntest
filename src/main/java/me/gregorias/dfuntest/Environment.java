package me.gregorias.dfuntest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class which represents the environment on which given application is run,
 * such as local or remote host.
 *
 * It tries to provide uniform console-like environment independent of what kind of environment
 * it is.
 */
public interface Environment {

  /**
   * Copies specified file or directory to destination directory on environment.
   *
   * @param srcPath Local source path.
   * @param destRelPath Relative destination directory on this environment.
   */
  void copyFilesFromLocalDisk(Path srcPath, String destRelPath) throws IOException;

  /**
   * Copies specified file or directory to destination directory on local host.
   *
   * @param srcRelPath Relative source path on this environment.
   * @param destPath Local destination directory.
   */
  void copyFilesToLocalDisk(String srcRelPath, Path destPath) throws IOException;

  /**
   * @return Hostname of this environment. May be IP address, WWW address etc.
   */
  String getHostname();

  /**
   * @return Numeric identifier of this environment
   */
  int getId();

  /**
   * @return Human readable name for this environment
   */
  String getName();

  /**
   * Gets saved property.
   *
   * @param key property's key
   * @return value corresponding to given key
   * @throws NoSuchElementException thrown when given key is not present
   */
  Object getProperty(String key) throws NoSuchElementException;

  /**
   * Synchronously runs arbitrary command on this environment's OS.
   *
   * @param command Command to run
   * @return Finished process.
   */
  RemoteProcess runCommand(List<String> command) throws InterruptedException, IOException;

  RemoteProcess runCommandAsynchronously(List<String> command) throws IOException;

  /**
   * Removes specified file. If path targets a directory its content is removed as well.
   *
   * @param relPath Relative path to file on this environment.
   */
  void removeFile(String relPath) throws InterruptedException, IOException;

  /**
   * Set arbitrary property of this environment.
   *
   * @param key property's key
   * @param value property's value
   */
  void setProperty(String key, Object value);
}
