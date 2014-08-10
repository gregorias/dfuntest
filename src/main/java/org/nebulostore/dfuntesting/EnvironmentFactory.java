package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.util.Collection;

/**
 * Objects of this class prepare and clean up environments used for testing.
 *
 * @author Grzegorz Milka
 */
public interface EnvironmentFactory {
  Collection<Environment> createEnvironments() throws IOException;

  /**
   * Cleans up everything created by createEnvironments.
   * 
   * @param env - environments created by createEnvironments.
   */
  void destroyEnvironments(Collection<Environment> envs);
}