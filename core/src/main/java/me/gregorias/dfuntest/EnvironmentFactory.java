package me.gregorias.dfuntest;

import java.io.IOException;
import java.util.Collection;

/**
 * Objects of this class prepare and clean up environments used for testing.
 *
 * @author Grzegorz Milka
 */
public interface EnvironmentFactory<EnvironmentT extends Environment> {
  Collection<EnvironmentT> create() throws IOException;

  /**
   * Cleans up everything created by create.
   *
   * @param envs - environments created by create.
   */
  void destroy(Collection<EnvironmentT> envs);
}