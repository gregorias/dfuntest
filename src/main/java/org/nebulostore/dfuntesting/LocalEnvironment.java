package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.nebulostore.dfuntesting.utils.ProcessUtils;

public class LocalEnvironment extends AbstractConfigurationEnvironment {
	private final int id_;
	private final Path dir_;

	public LocalEnvironment(int id, Path dir) {
	  super();
		id_ = id;
		dir_ = dir;
	}

	@Override
	public void copyFilesFromLocalDisk(Path srcPath, Path destRelPath) throws IOException {
	  Path destPath = dir_.resolve(destRelPath);
	  if (!Files.exists(destPath)) {
	    Files.createDirectory(destPath);
	  } else if (!Files.isDirectory(destPath)) {
		  throw new IOException("Destination path exists and it is not a directory.");
	  }
		if (Files.isDirectory(srcPath)) {
		  FileUtils.copyDirectoryToDirectory(srcPath.toFile(), destPath.toFile());
		} else {
      FileUtils.copyFileToDirectory(srcPath.toFile(), destPath.toFile());
		}
	}

	@Override
  public void copyFilesToLocalDisk(Path srcRelPath, Path destPath) throws IOException {
	  Path srcPath = dir_.resolve(srcRelPath);
	  if (!Files.exists(destPath)) {
	    Files.createDirectory(destPath);
	  } else if (!Files.isDirectory(destPath)) {
		  throw new IOException("Destination path exists and it is not a directory.");
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
		return id_;
	}

	@Override
	public String getName() {
	  return dir_.toAbsolutePath().toString();
	}

	@Override
	public CommandResult runCommand(List<String> command) throws CommandException, InterruptedException {
	  ProcessBuilder pb = new ProcessBuilder();
	  pb.command(command);
	  pb.directory(dir_.toFile());
	  Process p;
	  try {
      p = pb.start();
	  } catch (IOException e) {
	    throw new CommandException(e);
	  }
	  try {
      return ProcessUtils.runProcess(p);
    } catch (IOException e) {
      throw new CommandException(e);
    }
	}

	@Override
	public Process runCommandAsynchronously(List<String> command) throws CommandException {
	  ProcessBuilder pb = new ProcessBuilder();
	  pb.command(command);
	  pb.directory(dir_.toFile());
	  Process p;
	  try {
      p = pb.start();
	  } catch (IOException e) {
	    throw new CommandException(e);
	  }
	  return p;
	}

  @Override
  public void removeFile(Path relPath) {
    FileUtils.deleteQuietly(dir_.resolve(relPath).toFile());
  }
}
