package me.gregorias.dfuntest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import me.gregorias.dfuntest.util.SSHClientFactory;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;

/**
 * An UNIX environment accessible through SSH with public key.
 */
public class SSHEnvironment extends AbstractConfigurationEnvironment {
  private static final Logger LOGGER = LoggerFactory.getLogger(SSHEnvironment.class);
  private final int mId;
  private final String mRemoteHomePath;

  private final String mUsername;
  private final Path mPrivateKeyPath;

  private final InetAddress mRemoteInetAddress;
  private final Executor mExecutor;
  private final SSHClientFactory mSSHClientFactory;

  private final String mCDCommand;

  /**
   * @param id Environment's id
   * @param username Username of remote account
   * @param privateKeyPath Path to private key file
   * @param remoteInetAddress Remote host's address
   * @param remoteHomePath Path to remote home where environment will be placed.
   *                       May be relative to user's home.
   * @param executor Executor for running remote commands
   * @param sshClientFactory Factory for SSHClients
   */
  public SSHEnvironment(int id,
      String username,
      Path privateKeyPath,
      InetAddress remoteInetAddress,
      String remoteHomePath,
      Executor executor,
      SSHClientFactory sshClientFactory) {
    super();
    mId = id;
    mRemoteHomePath = remoteHomePath;

    mUsername = username;
    mPrivateKeyPath = privateKeyPath;

    mRemoteInetAddress = remoteInetAddress;
    mExecutor = executor;
    mSSHClientFactory = sshClientFactory;

    mCDCommand = "cd " + remoteHomePath + "; ";
  }

  @Override
  public void copyFilesFromLocalDisk(Path srcPath, String destRelPath) throws IOException {
    LOGGER.trace("copyFilesFromLocalDisk({}, {})", srcPath.toString(), destRelPath);
    mkdirs(destRelPath);
    SSHClient ssh = connectWithSSH();
    try {
      ssh.useCompression();

      String remotePath = FilenameUtils.concat(mRemoteHomePath, destRelPath);
      if (remotePath == null) {
        throw new IllegalArgumentException("Given paths cannot be correctly concatenated.");
      }

      ssh.newSCPFileTransfer().upload(new FileSystemFile(srcPath.toFile()), remotePath);
    } finally {
      ssh.disconnect();
    }
  }

  @Override
  public void copyFilesToLocalDisk(String srcRelPath, Path destPath) throws IOException {
    LOGGER.trace("copyFilesToLocalDisk({}, {})", srcRelPath, destPath.toString());
    createDestinationDirectoriesLocally(destPath);
    SSHClient ssh = connectWithSSH();
    try {
      ssh.useCompression();

      String remotePath = FilenameUtils.concat(mRemoteHomePath, srcRelPath);
      if (remotePath == null) {
        throw new IllegalArgumentException("Given paths cannot be correctly concatenated.");
      }

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
   * Create directories in environment if they don't exist. If directoryPath consists of several
   * directories all required parent directories are created as well.
   *
   * @param directoryPath path to create
   */
  public void mkdirs(String directoryPath) throws IOException {
    LOGGER.trace("mkdirs({})", directoryPath);
    String finalDirectoryPath = FilenameUtils.concat(mRemoteHomePath, directoryPath);
    if (finalDirectoryPath == null) {
      throw new IOException("Provided directory path is invalid and could not be concatenated"
        + " with base path.");
    }
    FilenameUtils.normalize(finalDirectoryPath, true);
    if (!finalDirectoryPath.startsWith("/")) {
      // SFTP mkdirs requires to a dot for relative path, otherwise it assumes given path is
      // absolute.
      finalDirectoryPath = "./" + finalDirectoryPath;
    }

    SSHClient ssh = connectWithSSH();
    try {
      try {
        try (SFTPClient sftp = ssh.newSFTPClient()) {
          sftp.mkdirs(finalDirectoryPath);
        }
      } catch (IOException e) {
        // SFTP has failed (on some systems it may be just disabled) revert to mkdir.
        List<String> command = new LinkedList<>();
        command.add("mkdir");
        command.add("-p");
        command.add(directoryPath);
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
  public void removeFile(String relPath) throws InterruptedException, IOException {
    List<String> command = new LinkedList<>();
    command.add("rm");
    command.add("-rf");
    command.add(relPath);
    RemoteProcess finishedProcess = runCommand(command);
    int exitCode = finishedProcess.waitFor();
    if (exitCode != 0) {
      throw new IOException(String.format("Removal of %s has ended with failure exit code: %d",
          relPath, exitCode));
    }
  }

  @Override
  public RemoteProcess runCommand(List<String> command) throws InterruptedException, IOException {
    SSHClient ssh;
    ssh = connectWithSSH();
    try {
      ProcessAdapter process = runCommandAndWrapInProcessAdapter(command, ssh);
      process.run();
      process.waitFor();
      return process;
    } catch (InterruptedException | IOException e) {
      try {
        ssh.disconnect();
      } catch (IOException ioException) {
        LOGGER.warn("runCommand(): Could not disconnect ssh.", ioException);
      }
      throw e;
    }
  }

  @Override
  public RemoteProcess runCommandAsynchronously(List<String> command) throws IOException {
    SSHClient ssh;
    ssh = connectWithSSH();
    try {
      ProcessAdapter process = runCommandAndWrapInProcessAdapter(command, ssh);
      mExecutor.execute(process);
      return process;
    } catch (IOException e) {
      try {
        ssh.disconnect();
      } catch (IOException ioException) {
        LOGGER.warn("runCommandAsynchronously(): Could not disconnect ssh.", ioException);
      }
      throw e;
    }
  }

  private static class ProcessAdapter implements RemoteProcess, Runnable {
    private final SSHClient mSSHClient;
    private final Session mSSHSession;
    private final Command mCommand;
    private final AtomicBoolean mHasJoined;
    private IOException mIOException;
    private int mExitCode;

    public ProcessAdapter(SSHClient client, Session session, Command command) {
      mSSHClient = client;
      mCommand = command;
      mSSHSession = session;
      mHasJoined = new AtomicBoolean(false);
    }

    @Override
    public void destroy() throws IOException {
      synchronized (mSSHClient) {
        mSSHClient.close();
      }
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
      } catch (IOException e) {
        LOGGER.error("run(): Could not correctly wait for command finish.", e);
        mIOException = e;
      } finally {
        mHasJoined.set(true);
        this.notifyAll();

        try {
          mSSHSession.close();
        } catch (IOException e) {
          LOGGER.warn("run(): Could not SSHSession.", e);
        }

        try {
          synchronized (mSSHClient) {
            mSSHClient.disconnect();
          }
        } catch (IOException e) {
          LOGGER.warn("run(): Could not SSHClient.", e);
        }
      }
    }

    @Override
    public int waitFor() throws InterruptedException, IOException {
      synchronized (this) {
        while (!mHasJoined.get()) {
          this.wait();
        }

        if (mIOException != null) {
          throw mIOException;
        } else {
          return mExitCode;
        }
      }
    }
  }

  private SSHClient connectWithSSH() throws IOException {
    SSHClient ssh = mSSHClientFactory.newSSHClient();
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

  private int runCommand(List<String> command, SSHClient ssh) throws IOException {
    try (Session session = ssh.startSession()) {
      Command cmd = session.exec(mCDCommand + StringUtils.join(command, ' '));
      cmd.join();
      int exitStatus = cmd.getExitStatus();
      cmd.close();
      return exitStatus;
    }
  }

  private ProcessAdapter runCommandAndWrapInProcessAdapter(List<String> command, SSHClient ssh)
    throws IOException {
    Session session = ssh.startSession();
    try {
      Command cmd = session.exec(mCDCommand + StringUtils.join(command, ' '));
      return new ProcessAdapter(ssh, session, cmd);
    } catch (IOException e) {
      session.close();
      throw e;
    }
  }
}
