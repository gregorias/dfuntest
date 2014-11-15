package me.gregorias.dfuntest;

import java.io.IOException;
import java.util.Collection;

/**
 * This class given collection of environments prepares their configuration so
 * that application can be run.
 *
 * @author Grzegorz Milka
 */
public interface EnvironmentPreparator<EnvironmentT extends Environment> {
  void prepareEnvironments(Collection<EnvironmentT> envs) throws IOException;

  /**
   * Performs best-effort try to collect output and log files for report.
   *
   * @param envs prepared environments
   */
  void collectOutputAndLogFiles(Collection<EnvironmentT> envs);

  /**
   * Performs best-effort try to clean prepared environments.
   *
   * @param envs prepraredEnvironments
   */
  void cleanEnvironments(Collection<EnvironmentT> envs);
}
