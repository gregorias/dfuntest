package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.App;
import me.gregorias.dfuntest.CommandException;
import me.gregorias.dfuntest.Environment;
import me.gregorias.dfuntest.RemoteProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * App interface to PingApplication which translates from Java's method calling to
 * PingApplication web interface allowing easy use in test scripts.
 */
public class ExampleApp extends App<Environment> {
  public static final String LOG_FILE = "stdlog.log";
  public static final String LOCAL_PORT_ENV_FIELD = "local-port";
  public static final String SERVER_HOSTNAME_ENV_FIELD = "server-hostname";
  public static final String SERVER_PORT_ENV_FIELD = "server-port";

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleApp.class);
  private final Environment mEnvironment;
  private final InetSocketAddress mThisAppSocketAddress;
  private final String mServerHostname;
  private final int mServerPort;

  private RemoteProcess mProcess;

  public ExampleApp(int id, String name, Environment environment) {
    super(id, name);
    mEnvironment = environment;
    int localPort = (Integer) mEnvironment.getProperty(LOCAL_PORT_ENV_FIELD);
    mThisAppSocketAddress = new InetSocketAddress(mEnvironment.getHostname(), localPort);
    mServerHostname = (String) mEnvironment.getProperty(SERVER_HOSTNAME_ENV_FIELD);
    mServerPort = (Integer) mEnvironment.getProperty(SERVER_PORT_ENV_FIELD);
  }

  @Override
  public Environment getEnvironment() {
    return mEnvironment;
  }

  public List<Integer> getPingedIDs() throws IOException {
    ByteBuffer getIDMessage = ByteBuffer.allocateDirect(1);
    getIDMessage.put(PingApplication.GET_ID_TYPE);
    getIDMessage.flip();
    return sendMessageAndGetResponse(getIDMessage);
  }

  public void ping(int id) throws IOException {
    ByteBuffer pingMessage = ByteBuffer.allocateDirect(PingApplication.PING_MESSAGE_LENGTH);
    pingMessage.put(PingApplication.PING_TYPE);
    pingMessage.putInt(id);
    pingMessage.flip();
    sendMessageAndGetResponse(pingMessage);
  }

  @Override
  public synchronized void startUp() throws CommandException, IOException {
    LOGGER.debug("[{}] startUp()", getId());
    List<String> runCommand = new LinkedList<>();

    runCommand.add("java");
    runCommand.add("-Dorg.slf4j.simpleLogger.logFile=" + LOG_FILE);
    runCommand.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=trace");
    runCommand.add("-cp");
    runCommand.add("lib/*:dfuntest-example.jar");
    runCommand.add("me.gregorias.dfuntest.example.PingApplication");
    runCommand.add(Integer.toString(mEnvironment.getId()));
    runCommand.add(Integer.toString(mThisAppSocketAddress.getPort()));
    runCommand.add(mServerHostname);
    runCommand.add(Integer.toString(mServerPort));
    mProcess = mEnvironment.runCommandAsynchronously(runCommand);
  }

  @Override
  public synchronized void shutDown() throws IOException, InterruptedException {
    LOGGER.debug("[{}] shutDown()", getId());
    if (mProcess == null) {
      throw new IllegalStateException("Process has not been started.");
    }
    try {
      close();
      mProcess.waitFor();
    } catch (IOException e) {
      LOGGER.info("shutDown(): Sending close message has generated an error.", e);
      mProcess.destroy();
    }
  }

  private void close() throws IOException {
    ByteBuffer closeMessage = ByteBuffer.allocateDirect(1);
    closeMessage.put(PingApplication.CLOSE_TYPE);
    closeMessage.flip();
    sendMessageAndGetResponse(closeMessage);
  }

  private List<Integer> readIntListResponse(SocketChannel channel) throws IOException {
    LOGGER.trace("readIntListResponse()");

    final int integerByteSize = Integer.SIZE / Byte.SIZE;
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(integerByteSize);
    List<Integer> responseList = new LinkedList<>();
    int readCount = 0;
    while (readCount != -1) {
      while (readBuffer.hasRemaining() && readCount != -1) {
        readCount = channel.read(readBuffer);
      }
      if (!readBuffer.hasRemaining()) {
        readBuffer.flip();
        responseList.add(readBuffer.getInt());
        readBuffer.flip();
      } else if (readBuffer.hasRemaining() && readBuffer.position() != 0) {
        throw new EOFException("Unexpected EOF encountered.");
      }
    }
    LOGGER.trace("readIntListResponse() -> {}", responseList.size());
    return responseList;
  }

  private List<Integer> sendMessageAndGetResponse(ByteBuffer message) throws IOException {
    LOGGER.trace("sendMessageAndGetResponse(): sending message to {}.", mThisAppSocketAddress);
    try (SocketChannel sc = SocketChannel.open(mThisAppSocketAddress)) {
      writeEntireBufferToChannel(sc, message);
      sc.shutdownOutput();
      return readIntListResponse(sc);
    }
  }

  private void writeEntireBufferToChannel(SocketChannel sc, ByteBuffer message)
      throws IOException {
    while (message.hasRemaining()) {
      sc.write(message);
    }
  }
}
