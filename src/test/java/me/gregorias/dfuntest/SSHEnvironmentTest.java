package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.SSHClientFactory;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SSHEnvironmentTest {
  private static final int ID = 0;
  private static final String USERNAME = "username";
  private static final Path PRIVATE_KEY_PATH = FileSystems.getDefault().getPath("key");
  private static final InetAddress REMOTE_ADDRESS = InetAddress.getLoopbackAddress();
  private static final String REMOTE_HOME_PATH = "home";
  private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

  private SSHClient mMockSSHClient = null;
  private SSHEnvironment mSSHEnv = null;

  @Before
  public void setUp() {
    SSHClientFactory mockSSHClientFactory = mock(SSHClientFactory.class);
    mMockSSHClient = mock(SSHClient.class);
    mSSHEnv = new SSHEnvironment(ID,
        USERNAME,
        PRIVATE_KEY_PATH,
        REMOTE_ADDRESS,
        REMOTE_HOME_PATH,
        EXECUTOR,
        mockSSHClientFactory);
    when(mockSSHClientFactory.newSSHClient()).thenReturn(mMockSSHClient);
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

  @Test(expected = IOException.class)
  public void mkdirsShouldThrowExceptionOnIncorrectDestinationPath() throws IOException {
    mSSHEnv.mkdirs("../../../mock");
  }

  @Test
  public void runCommandShouldConnectProperly() throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    List<String> command = new LinkedList<>();
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
    List<String> command = new LinkedList<>();
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
    List<String> command = new LinkedList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
  }

  @Test
  public void runCommandShouldEndSuccessfullyOnSSHDisconectFailure()
    throws IOException, InterruptedException {
    Session mockSession = mock(Session.class);
    when(mMockSSHClient.startSession()).thenReturn(mockSession);
    Command mockCommand = mock(Command.class);
    when(mockSession.exec(anyString())).thenReturn(mockCommand);
    doThrow(new IOException()).when(mMockSSHClient).disconnect();
    List<String> command = new LinkedList<>();
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
    List<String> command = new LinkedList<>();
    command.add("echo");
    command.add("hello");
    mSSHEnv.runCommand(command);
  }
}
