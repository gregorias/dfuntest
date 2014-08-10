package org.nebulostore.dfuntesting.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.nebulostore.dfuntesting.CommandResult;

public final class ProcessUtils {
  public static CommandResult runProcess(Process p) throws InterruptedException, IOException {
    int exitCode = p.waitFor();
    try (InputStreamReader stdOut = new InputStreamReader(p.getInputStream(),
        Charset.defaultCharset())) {
      try (InputStreamReader stdErr = new InputStreamReader(p.getErrorStream(),
          Charset.defaultCharset())) {
        CharBuffer stdOutBuf = CharBuffer.wrap(new StringBuilder());
        CharBuffer stdErrBuf = CharBuffer.wrap(new StringBuilder());
        stdOut.read(stdOutBuf);
        stdErr.read(stdErrBuf);
        return new CommandResult(exitCode, stdOut.toString(), stdErr.toString());
      }
    }

  }
}
