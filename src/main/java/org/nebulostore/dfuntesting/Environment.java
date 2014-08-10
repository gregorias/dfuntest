package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class which represents the environment on which given application is run,
 * such as local or remote host.
 * It tries to provide uniform console-like environment independent of what kind of environment
 * it is.
 */
public interface Environment {

  void copyFilesFromLocalDisk(Path srcPath, Path destRelPath) throws IOException;

  void copyFilesToLocalDisk(Path srcRelPath, Path destPath) throws IOException;
  
  String getHostname();

  int getId();

  String getName();

  Object getProperty(String key) throws NoSuchElementException;

  CommandResult runCommand(List<String> command) throws CommandException, InterruptedException;

  Process runCommandAsynchronously(List<String> command) throws CommandException;

  void removeFile(Path relPath) throws IOException;

  void setProperty(String key, Object value);
}
