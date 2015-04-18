package me.gregorias.dfuntest.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple network client/server. Instances of this application listen on a TCP connection waiting
 * for messages and requests and also occasionally ping suggested server. On ping
 *
 * End of message is signalled by output connection shutdown from the sending side.
 * The application handles following message format:
 * <pre>
 * =====================================
 * | bytes | 0               | 1 - ... |
 * ------------------------------------
 * |       | type of message | payload |
 * =====================================
 * </pre>
 *
 * There are following kind of messages:
 * PING - type is equal to 0; payload contains 4 byte integer with id of sending side.
 *     This message ends the connection.
 * GET_ID - type is equal to 1; payload is empty. On response the server sends a stream of 4
 * byte ids of applications that have sent a ping.
 * CLOSE - type is equal to 2; payload is empty. Closes this application.
 */
public class PingApplication implements Runnable {
  public static final byte PING_TYPE = 0;
  public static final byte GET_ID_TYPE = 1;
  public static final byte CLOSE_TYPE = 2;
  public static final int PING_MESSAGE_LENGTH = 5;
  private static final Logger LOGGER = LoggerFactory.getLogger(PingApplication.class);
  private static final int INTEGER_BYTE_SIZE = Integer.SIZE / Byte.SIZE;
  private static final long CLIENT_DELAY = 1;
  private static final int EXPECTED_ARGUMENT_COUNT = 4;
  private final int mLocalPort;
  private final InetSocketAddress mServerSocketAddress;
  private final ScheduledExecutorService mScheduledExecutorService =
      Executors.newScheduledThreadPool(1);
  private final Set<Integer> mPingedIDs = new HashSet<>();
  private ByteBuffer mResponseBuffer = ByteBuffer.wrap(new byte[0]);
  private boolean mIsClosed = false;

  private final ClientTask mClientTask;
  private final ByteBuffer mPingBuffer;

  public PingApplication(int id, int localPort, InetSocketAddress serverSocketAddress) {
    mClientTask = new ClientTask();
    mLocalPort = localPort;
    mServerSocketAddress = serverSocketAddress;
    mPingBuffer = preparePingBuffer(id);
  }

  /**
   * This main starts PingApplication. It expects 4 arguments:
   *
   * id - unique integer id.
   * local port - local port number to listen for connections.
   * server hostname - hostname of server to which pings will be sent.
   * server port - port number of server.
   * @param args program arguments.
   */
  public static void main(String[] args) {
    if (args.length != EXPECTED_ARGUMENT_COUNT) {
      LOGGER.error("Incorrect arguments provided.");
      return;
    }
    try {
      int id;
      int localPort;
      String hostname = args[2];
      int remotePort;
      try {
        id = Integer.parseInt(args[0]);
        localPort = Integer.parseInt(args[1]);
        remotePort = Integer.parseInt(args[3]);
      } catch (NumberFormatException e) {
        LOGGER.error("Incorrect arguments provided.");
        return;
      }
      LOGGER.info("main(): id={}, localPort={}, hostname={}, remotePort={}", id, localPort,
          hostname,
          remotePort);
      InetSocketAddress serverSocketAddress = new InetSocketAddress(hostname, remotePort);
      PingApplication app = new PingApplication(id, localPort, serverSocketAddress);
      app.run();
    } catch (RuntimeException e) {
      LOGGER.error("Caught runtime exception.", e);
    }
  }

  @Override
  public void run() {
    LOGGER.info("run()");
    mScheduledExecutorService.scheduleWithFixedDelay(mClientTask,
        CLIENT_DELAY,
        CLIENT_DELAY,
        TimeUnit.SECONDS);
    try {
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      SocketAddress localAddress = new InetSocketAddress(mLocalPort);
      serverSocketChannel.bind(localAddress, 1);
      while (!mIsClosed) {
        LOGGER.debug("run(): Ready to handle new connection.");
        try (SocketChannel socketChannel = serverSocketChannel.accept()) {
          LOGGER.debug("run(): Accepted new connection from {}.", socketChannel.getRemoteAddress());
          socketChannel.configureBlocking(true);
          handleClientConnection(socketChannel);
        }
      }
    } catch (IOException e) {
      LOGGER.error("run()", e);
    }
    LOGGER.info("run(): Shutting down ping task.");
    mScheduledExecutorService.shutdown();
    try {
      mScheduledExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    LOGGER.info("run() -> void");
  }

  private class ClientTask implements Runnable {
    @Override
    public void run() {
      LOGGER.debug("ClientTask.run()");
      try (SocketChannel channel = SocketChannel.open(mServerSocketAddress)) {
        writeEntireBufferAndHideException(channel, mPingBuffer);
      } catch (IOException e) {
        LOGGER.warn("ClientTask.run()", e);
      } finally {
        mPingBuffer.rewind();
      }
    }
  }

  private void addPing(int pingID) {
    LOGGER.debug("addPing({})", pingID);
    synchronized (mPingedIDs) {
      if (!mPingedIDs.contains(pingID)) {
        mPingedIDs.add(pingID);
        ByteBuffer updatedByteBuffer = ByteBuffer.allocateDirect(
            mResponseBuffer.capacity() + INTEGER_BYTE_SIZE);
        updatedByteBuffer.put(mResponseBuffer);
        updatedByteBuffer.putInt(pingID);
        mResponseBuffer = updatedByteBuffer;
        mResponseBuffer.rewind();
      }
    }
  }

  private void closeApplication() {
    LOGGER.info("closeApplication()");
    mIsClosed = true;
  }

  private void handleClientConnection(SocketChannel socketChannel) {
    LOGGER.debug("handleClientConnection()");
    byte[] messageArray = new byte[PING_MESSAGE_LENGTH];
    ByteBuffer initialMessageBuffer = ByteBuffer.wrap(messageArray);
    try {
      readInitialMessage(socketChannel, initialMessageBuffer);
    } catch (IOException e) {
      LOGGER.warn("handleClientConnection(): Caught exception when reading a message.", e);
      return;
    }
    initialMessageBuffer.flip();
    if (!initialMessageBuffer.hasRemaining()) {
      LOGGER.warn("handleClientConnection(): Initial message is empty.");
      return;
    }

    byte type = initialMessageBuffer.get();
    LOGGER.debug("handleClientConnection(): Received message of type: {}", type);
    switch (type) {
      case PING_TYPE:
        try {
          int pingID = initialMessageBuffer.getInt();
          addPing(pingID);
        } catch (BufferUnderflowException e) {
          LOGGER.warn("handleClientConnection(): Received ping, but without adequate ID.");
        }
        break;
      case GET_ID_TYPE:
        sendPingedIDs(socketChannel);
        break;
      case CLOSE_TYPE:
        try {
          LOGGER.debug("handleClientConnection(): Received close order.");
          waitForClientClose(socketChannel);
          socketChannel.shutdownOutput();
        } catch (IOException e) {
          LOGGER.warn("handleClientConnection(): Caught IOException when closing channel which"
                  + " requested application close.", e);
        }
        closeApplication();
        break;
      default:
    }
  }

  private ByteBuffer preparePingBuffer(int id) {
    ByteBuffer buffer = ByteBuffer.allocate(PING_MESSAGE_LENGTH);
    buffer.put(PING_TYPE);
    buffer.putInt(id);
    buffer.flip();
    return buffer;
  }

  private void readInitialMessage(SocketChannel channel, ByteBuffer initialMessageBuffer)
      throws IOException {
    int readCount = 0;
    while (initialMessageBuffer.hasRemaining() && readCount != -1) {
      readCount = channel.read(initialMessageBuffer);
    }
  }

  private void sendPingedIDs(SocketChannel socketChannel) {
    synchronized (mPingedIDs) {
      writeEntireBufferAndHideException(socketChannel, mResponseBuffer);
      mResponseBuffer.rewind();
    }
  }

  private void waitForClientClose(SocketChannel socketChannel) throws IOException {
    int readCount = 0;
    while (readCount != -1) {
      ByteBuffer sponge = ByteBuffer.wrap(new byte[PING_MESSAGE_LENGTH]);
      readCount = socketChannel.read(sponge);
    }
  }

  private void writeEntireBufferAndHideException(SocketChannel channel, ByteBuffer buffer) {
    try {
      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }
    } catch (IOException e) {
      LOGGER.warn("writeEntireBufferAndHideException(): Encountered exception when sending"
          + " buffer.", e);
    }
  }
}
