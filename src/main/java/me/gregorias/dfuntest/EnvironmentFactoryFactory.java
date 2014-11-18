package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
import org.apache.commons.configuration.Configuration;

import java.util.concurrent.Executor;

/**
 * Factory of {@link me.gregorias.dfuntest.EnvironmentFactory} which builds EnvironmentFactory
 * based on configuration file.
 */
// Yey, I get two Java points for FactoryFactory
public interface EnvironmentFactoryFactory<EnvironmentT extends Environment> {
  EnvironmentFactory<EnvironmentT> newEnvironmentFactory(
      Configuration configuration,
      FileUtils fileUtils,
      Executor executor);
}
