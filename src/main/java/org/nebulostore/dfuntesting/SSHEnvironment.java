package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;

/**
 * An UNIX environment accessible through SSH with public key.
 */
public class SSHEnvironment extends AbstractConfigurationEnvironment {
  private static final Logger LOGGER = LoggerFactory.getLogger(SSHEnvironment.class);
  private final int mId;
  private final Path mRemoteHomePath;

  private final String mUsername;
  private final Path mPrivateKeyPath;

  private final InetAddress mRemoteInetAddress;
  private final Executor mExecutor;

  public SSHEnvironment(int id,
      String username,
      Path privateKeyPath,
      InetAddress remoteInetAddress,
      Path remoteHomePath,
      Executor executor) {
    super();
    mId = id;
    mRemoteHomePath = remoteHomePath;

    mUsername = username;
    mPrivateKeyPath = privateKeyPath;

    mRemoteInetAddress = remoteInetAddress;
    mExecutor = executor;
  }

  @Override
  public void copyFilesFromLocalDisk(Path srcPath, Path destRelPath) throws IOException {
    LOGGER.trace("copyFilesFromLocalDisk({}, {})", srcPath.toString(), destRelPath.toString());
    mkdirs(destRelPath.toString());
    SSHClient ssh = connectWithSSH();
    try {
      ssh.useCompression();

      String remotePath = mRemoteHomePath.resolve(destRelPath).toString();

      ssh.newSCPFileTransfer().upload(new FileSystemFile(srcPath.toFile()), remotePath);
    } finally {
      ssh.disconnect();
    }
  }

  @Override
  public void copyFilesToLocalDisk(Path srcRelPath, Path destPath) throws IOException {
    LOGGER.trace("copyFilesToLocalDisk({}, {})", srcRelPath.toString(), destPath.toString());
    createDestinationDirectoriesLocally(destPath);
    SSHClient ssh = connectWithSSH();
    try {
      ssh.useCompression();

      String remotePath = mRemoteHomePath.resolve(srcRelPath).toString();

      ssh.newSCPFileTransfer().download(remotePath, new FileSystemFile(destPath.toFile()));
    } finally {
      ssh.disconnect();
    }
  }

  @Override
  public String getHostname() {
    return mRemoteInetAddress.getHostName();
  }

  @Override
  public int getId() {
    return mId;
  }

  @Override
  public String getName() {
    return mRemoteInetAddress.getHostName();
  }

  /**
   * Create directories in environment if they don't exist. If directoryPath consists of several directories all
   * required parent directories are created as well.
   * @param directoryPath
   * @throws IOException
   */
  public void mkdirs(String directoryPath) throws IOException {
    LOGGER.trace("mkdirs({})", directoryPath);
    String remoteHomePathString = mRemoteHomePath.toString();
    String finalDirectoryPath = FilenameUtils.concat(remoteHomePathString, directoryPath);
    if (finalDirectoryPath == null) {
      throw new IOException("Provided directory path is invalid and could not be concatenated with base path.");
    }
    FilenameUtils.normalize(finalDirectoryPath, true);
    if (!finalDirectoryPath.startsWith("/")) {
      // SFTP mkdirs requires to a dot for relative path, otherwise it assumes given path is absolute.
      finalDirectoryPath = "./" + finalDirectoryPath;
    }

    SSHClient ssh = connectWithSSH();
    try {
      try {
        SFTPClient sftp = ssh.newSFTPClient();
        try {
          sftp.mkdirs(finalDirectoryPath);
        } finally {
          sftp.close();
        }
      } catch (IOException e) {
        // SFTP has failed (on some systems it may be just disabled) revert to mkdir.
        List<String> command = new LinkedList<>();
        command.add("mkdir");
        command.add("-p");
        command.add(finalDirectoryPath);
        int exitStatus = runCommand(command, ssh);
        if (exitStatus != 0) {
          throw new IOException("Could not create suggested directories.");
        }
      }
    } finally {
      ssh.disconnect();
    }
  }

  @Override
  public void removeFile(Path relPath) throws IOException {
    List<String> command = new LinkedList<>();
    command.add("rm");
    command.add("-rf");
    command.add(relPath.toString());
    Process finishedProcess;
    try {
      finishedProcess = runCommand(command);
    } catch (CommandException | InterruptedException e) {
      throw new IOException(e);
    }
    int exitCode;
    try {
      exitCode = finishedProcess.waitFor();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Encountered unexpected interrupt during file removal.");
    }
    if (exitCode != 0) {
      throw new IOException(String.format("Removal of %s has ended with failure exit code: %d",
          relPath, exitCode));
    }
  }

  @Override
  public Process runCommand(List<String> command) throws CommandException, InterruptedException {
    Process process = runCommandAsynchronously(command);
    process.waitFor();
    return process;
  }

  @Override
  public Process runCommandAsynchronously(List<String> command) throws CommandException {

    SSHClient ssh;
    try {
      ssh = connectWithSSH();
    } catch (IOException e) {
      throw new CommandException(e);
    }
    try {
      Session session = ssh.startSession();
      Command cmd = session.exec("cd " + mRemoteHomePath.toString() + "; " + StringUtils.join(command, ' '));
      ProcessAdapter process = new ProcessAdapter(ssh, cmd);
      mExecutor.execute(process);
      return process;
    } catch (IOException e) {
      try {
        ssh.disconnect();
      } catch (IOException ioException) {
        LOGGER.warn("runCommandAsynchronously(): Could not disconnect ssh.", ioException);
      }
      throw new CommandException(e);
    }
  }

  private static class ProcessAdapter extends Process implements Runnable {
    private final SSHClient mSSHClient;
    private final Command mCommand;
    private final AtomicBoolean mHasJoined;
    private int mExitCode;

    public ProcessAdapter(SSHClient client, Command command) {
      mSSHClient = client;
      mCommand = command;
      mHasJoined = new AtomicBoolean(false);
    }

    @Override
    public void destroy() {
      throw new UnsupportedOperationException("Destroy is for now unavailable till process refactoring.");
    }

    @Override
    public int exitValue() {
      Integer exitStatus = mCommand.getExitStatus();
      if (null == exitStatus) {
        throw new IllegalThreadStateException();
      }
      return exitStatus;
    }

    @Override
    public InputStream getErrorStream() {
      return mCommand.getErrorStream();
    }

    @Override
    public InputStream getInputStream() {
      return mCommand.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
      return mCommand.getOutputStream();
    }

    @Override
    public synchronized void run() {
      try {
        mCommand.join();
        mExitCode = mCommand.getExitStatus();
        mCommand.close();
      } catch (ConnectionException | TransportException e) {
        LOGGER.error("run(): Could not correctly wait for command finish.", e);
        mExitCode = -1;
      } finally {
        mHasJoined.set(true);
        this.notifyAll();
        try {
          mSSHClient.disconnect();
        } catch (IOException e) {
          LOGGER.error("run(): Could not disconnect.", e);
        }
      }
    }

    @Override
    public int waitFor() {
      synchronized (this) {
        while (!mHasJoined.get()) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            LOGGER.trace("run(): Caught interrupt - ignoring.");
            Thread.currentThread().interrupt();
          }
        }
        return mExitCode;
      }
    }
  }

  private void changeDirectoryToRelativeHome(Session session) throws IOException {
    List<String> cdCommands = new LinkedList<>();
    cdCommands.add("cd");
    cdCommands.add(mRemoteHomePath.toString());
    Command cmd = session.exec(StringUtils.join(cdCommands, ' '));
    try {
      cmd.join();
      int exitStatus = cmd.getExitStatus();
      if (exitStatus != 0) {
        throw new IOException("cd has failed");
      }
    } finally {
      cmd.close();
    }
  }

  private SSHClient connectWithSSH() throws IOException {
    SSHClient ssh = new SSHClient();
    ssh.loadKnownHosts();
    ssh.connect(mRemoteInetAddress);
    KeyProvider keys = ssh.loadKeys(mPrivateKeyPath.toString());
    ssh.authPublickey(mUsername, keys);
    return ssh;
  }

  private static void createDestinationDirectoriesLocally(Path destPath) throws IOException {
    boolean hasCreated = destPath.toFile().mkdirs();
    if (!hasCreated && !destPath.toFile().isDirectory()) {
      throw new IOException("Could not create required directories.");
    }
  }

  /**
   *
   * @param command
   * @param ssh Connected SSHClient
   * @return
   * @throws CommandException
   */
  private int runCommand(List<String> command, SSHClient ssh) throws IOException {
    Session session = ssh.startSession();
    try {
      Command cmd = session.exec(StringUtils.join(command, ' '));
      cmd.join();
      int exitStatus = cmd.getExitStatus();
      cmd.close();
      return exitStatus;
    } finally {
      session.close();
    }
  }
}
