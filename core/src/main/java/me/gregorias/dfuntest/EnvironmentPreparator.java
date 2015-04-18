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
   * It cleans everything prepared by {@link #prepare(java.util.Collection)}
   *
   * @param envs preprared Environments
   */
  void cleanAll(Collection<EnvironmentT> envs);

  /**
   * <p>
   * Performs best-effort try to clean prepared environments from all transient output files
   * such as logs, result files. This method should always perform a subset of tasks done by
   * {@link #cleanAll(java.util.Collection)}}
   * </p>
   *
   * @param envs preprared Environments
   */
  void cleanOutput(Collection<EnvironmentT> envs);

  /**
   * Performs best-effort try to collect result and log files for report and put them into local
   * directory.
   *
   * @param envs prepared environments
   * @param destPath existing directory to put files to
   */
  void collectOutput(Collection<EnvironmentT> envs, Path destPath);

  /**
   * <p>
   * Initializes environment with necessary files to run the application.
   * </p>
   *
   * <p>
   * Preparation may include such things as copying application, library dependencies, configuration
   * files etc.,
   * </p>
   *
   * @param envs Environments on which test will run
   */
  void prepare(Collection<EnvironmentT> envs) throws IOException;

  /**
   * <p>
   * Prepares environments to run a new test assuming all permanent files are in place.
   * This may include such things as configuration file transfer etc.
   * This method should only perform a subset of tasks done by
   * {@link #prepare(java.util.Collection)}.
   * </p>
   *
   * @param envs Environments to restore
   */
  void restore(Collection<EnvironmentT> envs) throws IOException;
}
