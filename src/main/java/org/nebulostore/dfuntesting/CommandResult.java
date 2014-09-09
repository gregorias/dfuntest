package org.nebulostore.dfuntesting;

public class CommandResult {
  private final int mExitCode;
  private final String mStdOut;
  private final String mStdErr;
  
  public CommandResult(int exitCode, String stdOut, String stdErr) {
    mExitCode = exitCode;
    mStdOut = stdOut;
    mStdErr = stdErr;
  }
  
  public int getExitCode() {
    return mExitCode;
  }
  
  public String getStdOut() {
    return mStdOut;
  }
  
  public String getStdErr() {
    return mStdErr;
  }
}
