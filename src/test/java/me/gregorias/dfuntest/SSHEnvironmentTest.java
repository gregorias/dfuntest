package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
import me.gregorias.dfuntest.util.SSHClientFactory;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class SSHEnvironmentTest {
  private static final int ID = 0;
  private static final String USERNAME = "username";
  private static final Path PRIVATE_KEY_PATH = FileSystems.getDefault().getPath("key");
  private static final InetAddress REMOTE_ADDRESS = InetAddress.getLoopbackAddress();
  private static final String REMOTE_HOME_PATH = "home";
  private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

  private SSHClient mMockSSHClient = null;
  private SSHEnvironment mSSHEnv = null;
  private FileUtils mMockFileUtils = null;

  @Before
  public void setUp() {
    SSHClientFactory mockSSHClientFactory = mock(SSHClientFactory.class);
    mMockSSHClient = mock(SSHClient.class);
    mMockFileUtils = mock(FileUtils.class);
    mSSHEnv = new SSHEnvironment(ID,
        USERNAME,
        PRIVATE_KEY_PATH,
        REMOTE_ADDRESS,
        REMOTE_HOME_PATH,
        EXECUTOR,
        mockSSHClientFactory,
        mMockFileUtils);
    when(mockSSHClientFactory.newSSHClient()).thenReturn(mMockSSHClient);
  }

  @Test
  public void copyFilesFromLocalDiskShouldRunSCPAndToCorrectDirectory() throws IOException {
    SCPFileTransfer mockSCPFileTransfer = mock(SCPFileTransfer.class);
    when(mMockSSHClient.newSCPFileTransfer()).thenReturn(mockSCPFileTransfer);
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenReturn(mockSFTP);

    Path from = FileSystems.getDefault().getPath(".");
    String to = "foo";

    mSSHEnv.copyFilesFromLocalDisk(from, to);

    verify(mMockSSHClient).newSCPFileTransfer();
    String expectedDestination = FilenameUtils.concat(REMOTE_HOME_PATH, to);
    verify(mockSCPFileTransfer).upload(any(LocalSourceFile.class), eq(expectedDestination));
  }

  @Test(expected = IOException.class)
  public void copyFilesShouldThrowExceptionWhenSCPFails() throws IOException {
    SCPFileTransfer mockSCPFileTransfer = mock(SCPFileTransfer.class);
    when(mMockSSHClient.newSCPFileTransfer()).thenReturn(mockSCPFileTransfer);
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenReturn(mockSFTP);
    doThrow(new IOException()).when(mockSCPFileTransfer).upload(
        any(LocalSourceFile.class), anyString());

    Path from = FileSystems.getDefault().getPath(".");
    String to = "foo";

    mSSHEnv.copyFilesFromLocalDisk(from, to);
  }

  @Test(expected = IllegalArgumentException.class)
  public void copyFilesShouldThrowExceptionWhenDestinationIsIncorrect() throws IOException {
    SCPFileTransfer mockSCPFileTransfer = mock(SCPFileTransfer.class);
    when(mMockSSHClient.newSCPFileTransfer()).thenReturn(mockSCPFileTransfer);
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenReturn(mockSFTP);

    Path from = FileSystems.getDefault().getPath(".");
    String to = "../../..";

    mSSHEnv.copyFilesFromLocalDisk(from, to);
  }

  @Test(expected = IOException.class)
  public void copyFilesToLocalDiskShouldThrowExceptionWhenCanNotCreateDirectories()
      throws IOException {
    SCPFileTransfer mockSCPFileTransfer = mock(SCPFileTransfer.class);
    when(mMockSSHClient.newSCPFileTransfer()).thenReturn(mockSCPFileTransfer);

    doThrow(IOException.class).when(mMockFileUtils).createDirectories(any(Path.class));

    String from = ".";
    Path to = FileSystems.getDefault().getPath(".");

    mSSHEnv.copyFilesToLocalDisk(from, to);
  }

  @Test
  public void copyFilesToLocalDiskShouldRunSCPAndToCorrectDirectory() throws IOException {
    SCPFileTransfer mockSCPFileTransfer = mock(SCPFileTransfer.class);
    when(mMockSSHClient.newSCPFileTransfer()).thenReturn(mockSCPFileTransfer);

    String from = "foo";
    Path to = FileSystems.getDefault().getPath(".");

    mSSHEnv.copyFilesToLocalDisk(from, to);

    verify(mMockSSHClient).newSCPFileTransfer();
    String expectedDestination = FilenameUtils.concat(REMOTE_HOME_PATH, from);
    verify(mockSCPFileTransfer).download(eq(expectedDestination), any(LocalDestFile.class));
  }

  @Test
  public void mkdirsShouldUseSFTPFirstAndDestinationPathShouldContainDot() throws IOException {
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenReturn(mockSFTP);
    mSSHEnv.mkdirs("mock");

    String properDestinationPath = "./" + FilenameUtils.concat(REMOTE_HOME_PATH, "mock");
    verify(mockSFTP).mkdirs(eq(properDestinationPath));
  }

  @Test
  public void mkdirsShouldUseDirectCommandOnSFTPFailure() throws IOException {
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenThrow(new IOException());
    doThrow(new IOException()).when(mockSFTP).mkdirs(anyString());
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    mSSHEnv.mkdirs("mock");
    verify(mockSession).exec(contains("mkdir -p"));
    verify(mockSession).exec(contains(FilenameUtils.concat(REMOTE_HOME_PATH, "mock")));
    verify(mockCommand).join();
  }

  @Test
  public void mkdirsShouldUseDirectCommandOnSFTPFailure2() throws IOException {
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenReturn(mockSFTP);
    doThrow(new IOException()).when(mockSFTP).mkdirs(anyString());
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    mSSHEnv.mkdirs("mock");
    verify(mockSession).exec(contains("mkdir -p"));
    verify(mockCommand).join();
  }

  @Test(expected = IOException.class)
  public void mkdirsShouldThrowExceptionOnDirectCommandFailure() throws IOException {
    SFTPClient mockSFTP = mock(SFTPClient.class);
    when(mMockSSHClient.newSFTPClient()).thenReturn(mockSFTP);
    doThrow(new IOException()).when(mockSFTP).mkdirs(anyString());
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    when(mockCommand.getExitStatus()).thenReturn(-1);
    mSSHEnv.mkdirs("mock");
  }

  @Test(expected = IllegalArgumentException.class)
  public void mkdirsShouldThrowExceptionOnIncorrectDestinationPath() throws IOException {
    mSSHEnv.mkdirs("../../../mock");
  }

  @Test
  public void removeFileShouldCallProperCommand() throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);

    String file = "foo";
    mSSHEnv.removeFile(file);
    verify(mockSession).exec(contains("rm -rf"));
    verify(mockSession).exec(contains(file));
  }

  @Test(expected = IOException.class)
  public void removeFileShouldFailOnRMCommandFail() throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    when(mockCommand.getExitStatus()).thenReturn(1);

    String file = "foo";
    mSSHEnv.removeFile(file);
  }

  @Test
  public void runCommandShouldConnectProperly() throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
    verify(mMockSSHClient).loadKnownHosts();
    verify(mMockSSHClient).connect(eq(REMOTE_ADDRESS));
    verify(mMockSSHClient).authPublickey(eq(USERNAME), eq(PRIVATE_KEY_PATH.toString()));
  }

  @Test
  public void runCommandShouldRunCommandWithProperArgumentsAndWaitForIt()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
    verify(mockSession).exec(contains(StringUtils.join(command, " ")));
    verify(mockCommand).join();
    verify(mockCommand).close();
    verify(mockSession).close();
    verify(mMockSSHClient).disconnect();
  }

  @Test(expected = IOException.class)
  public void runCommandShouldThrowExceptionIfCommandFails()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    doThrow(new ConnectionException("mock test")).when(mockCommand).join();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
  }

  @Test
  public void runCommandShouldEndSuccessfullyOnSSHDisconnectFailure()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    doThrow(new IOException()).when(mMockSSHClient).disconnect();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
  }

  @Test(expected = ConnectionException.class)
  public void runCommandShouldThrowOriginalExceptionOnFail()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    when(mockSession.exec(anyString())).thenThrow(new ConnectionException("mock test"));
    doThrow(new IOException()).when(mMockSSHClient).disconnect();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
  }

  @Test
  public void runCommandAsynchronouslyShouldRunCommandWithProperArgumentsAndWaitForIt()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    BlockingQueue clientDisconnectQueue = new LinkedBlockingQueue();
    doAnswer(new BlockingWaitAnswer(clientDisconnectQueue)).when(mMockSSHClient).disconnect();
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    BlockingQueue<Object> joinAnswers = new LinkedBlockingQueue<>();
    doAnswer(new BlockingAnswer(joinAnswers)).when(mockCommand).join();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    RemoteProcess process = mSSHEnv.runCommandAsynchronously(command);
    verify(mockSession).exec(contains(StringUtils.join(command, " ")));
    verify(mockCommand, never()).close();
    verify(mockSession, never()).close();
    verify(mMockSSHClient, never()).disconnect();
    joinAnswers.add(0);
    int defaultReturnValue = 0;
    int returnValue = process.waitFor();
    assertEquals(defaultReturnValue, returnValue);
    clientDisconnectQueue.take();
    verify(mockCommand).close();
    verify(mockSession).close();
    verify(mMockSSHClient).disconnect();
  }

  @Test
  public void runCommandAsynchronouslyShouldStillWorkOnSessionCloseFailure()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    BlockingQueue clientDisconnectQueue = new LinkedBlockingQueue();
    doAnswer(new BlockingWaitAnswer(clientDisconnectQueue)).when(mMockSSHClient).disconnect();
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    doThrow(ConnectionException.class).when(mockSession).close();
    BlockingQueue<Object> joinAnswers = new LinkedBlockingQueue<>();
    doAnswer(new BlockingAnswer(joinAnswers)).when(mockCommand).join();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    RemoteProcess process = mSSHEnv.runCommandAsynchronously(command);
    verify(mockSession).exec(contains(StringUtils.join(command, " ")));
    verify(mockCommand, never()).close();
    verify(mockSession, never()).close();
    verify(mMockSSHClient, never()).disconnect();
    joinAnswers.add(0);
    int defaultReturnValue = 0;
    int returnValue = process.waitFor();
    assertEquals(defaultReturnValue, returnValue);
    clientDisconnectQueue.take();
    verify(mockCommand).close();
    verify(mockSession).close();
    verify(mMockSSHClient).disconnect();
  }

  @Test(expected = ConnectionException.class)
  public void runCommandAsynchronouslyShouldProperlyShutDownConnectionOnJoinAndSessionCloseFail()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    BlockingQueue clientDisconnectQueue = new LinkedBlockingQueue();
    doAnswer(new BlockingWaitAnswer(clientDisconnectQueue)).when(mMockSSHClient).disconnect();
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    doThrow(ConnectionException.class).when(mockSession).close();
    doThrow(ConnectionException.class).when(mockCommand).join();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    RemoteProcess process = mSSHEnv.runCommandAsynchronously(command);
    try {
      process.waitFor();
      fail();
    } catch (ConnectionException e) {
      clientDisconnectQueue.take();
      verify(mockCommand, never()).close();
      verify(mMockSSHClient).disconnect();
      throw e;
    }
  }

  @Test(expected = ConnectionException.class)
  public void runCommandAsynchronouslyShouldProperlyShutDownConnectionOnDestroyBlockingProcess()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    BlockingQueue<Object> joinAnswers = new LinkedBlockingQueue<>();
    doAnswer(new BlockingAnswer(joinAnswers)).when(mockCommand).join();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    RemoteProcess process = mSSHEnv.runCommandAsynchronously(command);
    verify(mockSession).exec(contains(StringUtils.join(command, " ")));
    verify(mockCommand, never()).close();
    verify(mockSession, never()).close();
    verify(mMockSSHClient, never()).disconnect();
    process.destroy();
    verify(mMockSSHClient).disconnect();
    joinAnswers.put(new ConnectionException("unit test error"));
    process.waitFor();
  }

  @Test(expected = ConnectionException.class)
  public void runCommandAsynchronouslyShouldThrowExceptionOnExecFailAndDisconnectSSHClient()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    when(mockSession.exec(anyString())).thenThrow(new ConnectionException("mock test"));
    doThrow(new IOException()).when(mMockSSHClient).disconnect();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommandAsynchronously(command);
    verify(mMockSSHClient).disconnect();
  }

  @Test(expected = ConnectionException.class)
  public void runCommandAsynchronouslyShouldThrowExceptionOnExecFailAndDisconnectSSHClientFail()
      throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    doThrow(IOException.class).when(mMockSSHClient).disconnect();
    when(mockSession.exec(anyString())).thenThrow(new ConnectionException("mock test"));
    doThrow(new IOException()).when(mMockSSHClient).disconnect();
    List<String> command = new ArrayList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommandAsynchronously(command);
  }

  private static class BlockingAnswer implements Answer {
    private final BlockingQueue<Object> mQueue;

    public BlockingAnswer(BlockingQueue<Object> queue) {
      mQueue = queue;
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
      Object result = mQueue.take();
      if (result instanceof Throwable) {
        throw (Throwable) result;
      }
      return null;
    }
  }

  private static class BlockingWaitAnswer implements Answer {
    private final BlockingQueue<Object> mQueue;

    public BlockingWaitAnswer(BlockingQueue<Object> queue) {
      mQueue = queue;
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws InterruptedException {
      mQueue.put(0);
      return null;
    }
  }
}
