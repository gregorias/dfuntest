package me.gregorias.dfuntest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A process that may be running on a remote host.
 */
@SuppressWarnings("unused")
public interface RemoteProcess {
  /**
   * Forcibly terminates the process.
   */
  void destroy() throws IOException;

  InputStream getErrorStream();

  InputStream getInputStream();

  OutputStream getOutputStream();

  /**
   * Blocks till process finishes and returns its exit code.
   *
   * @return exit value of process
   */
  int waitFor() throws InterruptedException, IOException;
}
