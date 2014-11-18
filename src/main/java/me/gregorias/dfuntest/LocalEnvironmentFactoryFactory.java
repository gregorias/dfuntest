package me.gregorias.dfuntest;

import me.gregorias.dfuntest.util.FileUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;

import java.util.concurrent.Executor;

/**
 * EnvironmentFactoryFactory for LocalEnvironmentFactory. It expects a configuration with following
 * properties.
 *
 * <ul>
 *   <li>environment-count - number of environments to create</li>
 *   <li>dir-prefix - prefix used for creating new temporary directories</li>
 * </ul>
 */
public class LocalEnvironmentFactoryFactory implements
    EnvironmentFactoryFactory<Environment> {
  public static final String XML_ENV_CNT_FIELD = "environment-count";
  public static final String XML_DIR_PREFIX_FIELD = "dir-prefix";

  @Override
  public EnvironmentFactory<Environment> newEnvironmentFactory(
      Configuration configuration,
      FileUtils fileUtils,
      Executor executor) throws ConversionException, IllegalArgumentException {
    Integer envCount = configuration.getInteger(XML_ENV_CNT_FIELD, -1);
    String dirPrefix = configuration.getString(XML_DIR_PREFIX_FIELD);

    if (-1 == envCount || null == dirPrefix) {
      throw new IllegalArgumentException("One of the required fields is missing or is in"
          + " wrong format.");
    }

    return new LocalEnvironmentFactory(envCount, dirPrefix, fileUtils);
  }
}
