package org.nebulostore.dfuntesting;

public class CommandResult {
  private final int exitCode_;
  private final String stdOut_;
  private final String stdErr_;
  
  public CommandResult(int exitCode, String stdOut, String stdErr) {
    exitCode_ = exitCode;
    stdOut_ = stdOut;
    stdErr_ = stdErr;
  }
  
  public int getExitCode() {
    return exitCode_;
  }
  
  public String getStdOut() {
    return stdOut_;
  }
  
  public String getStdErr() {
    return stdErr_;
  }
}
