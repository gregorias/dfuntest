package org.nebulostore.dfuntesting;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * This class given collection of environments prepares their configuration so that application can be run.
 * 
 * @author Grzegorz Milka
 */
public interface EnvironmentPreparator {
  void prepareEnvironments(Collection<Environment> envs) throws ExecutionException;

  /**
   * Performs best-effort try to collect output and log files for report.
   * 
   * @param envs prepared environments
   */
  void collectOutputAndLogFiles(Collection<Environment> envs); 

  /**
   * Performs best-effort try to clean prepared environments.
   * 
   * @param envs
   */
  void cleanEnvironments(Collection<Environment> envs);
}
