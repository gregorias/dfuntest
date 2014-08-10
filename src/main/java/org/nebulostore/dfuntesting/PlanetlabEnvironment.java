package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PlanetlabEnvironment extends AbstractConfigurationEnvironment
    implements Environment {

  @Override
  public void copyFilesFromLocalDisk(Path srcPath, Path destRelPath)
      throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void copyFilesToLocalDisk(Path srcRelPath, Path destPath)
      throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public String getHostname() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getId() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CommandResult runCommand(List<String> command)
      throws CommandException, InterruptedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Process runCommandAsynchronously(List<String> command)
      throws CommandException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeFile(Path relPath) throws IOException {
    // TODO Auto-generated method stub

  }

}
