package me.gregorias.dfuntest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * This class given collection of environments prepares their configuration so
 * that application can be run.
 *
 * @author Grzegorz Milka
 */
public interface EnvironmentPreparator<EnvironmentT extends Environment> {

  /**
   * Performs best-effort try to clean prepared environments from all app related files.
   *
   * @param envs preprared Environments
   */
  void clean(Collection<EnvironmentT> envs);

  /**
   * Performs best-effort try to collect output and log files for report and put them into local
   * directory.
   *
   * @param envs prepared environments
   * @param destPath existing directory to put files to
   */
  void collectOutputAndLogFiles(Collection<EnvironmentT> envs, Path destPath);

  /**
   * Initializes environment with necessary files to run the application.
   *
   * @param envs Environments on which test will run
   */
  void prepare(Collection<EnvironmentT> envs) throws IOException;

  /**
   * Cleans up environments from temporary application files and restores them to initial state.
   *
   * @param envs Environments to restore
   */
  void restore(Collection<EnvironmentT> envs) throws IOException;
}
