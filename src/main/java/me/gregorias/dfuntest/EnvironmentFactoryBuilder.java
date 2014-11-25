package me.gregorias.dfuntest;

import org.apache.commons.configuration.Configuration;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Builder of {@link me.gregorias.dfuntest.EnvironmentFactory} which builds EnvironmentFactory
 * based on configuration file.
 */
// This builder is provided to ease configuration of environment from library client perspective.
// Yey, I get two Java points for FactoryBuilder.
public interface EnvironmentFactoryBuilder<EnvironmentT extends Environment> {
  EnvironmentFactory<EnvironmentT> newEnvironmentFactory(
      Configuration configuration,
      Executor executor) throws IOException;
}
