package me.gregorias.dfuntest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import me.gregorias.dfuntest.util.FileUtils;
import me.gregorias.dfuntest.util.FileUtilsImpl;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of {@link LocalEnvironment}.
 *
 * This factory uses {@link Configuration} to specify environment configuration.
 * Configuration uses following fields:
 * <ul>
 * <li> environment-count - Number of local environments to create. Has to be positive. </li>
 * <li> dir-prefix - Temporary directory prefix used for creating local
 *  environment in system's temporary directory. Default value is dfuntest. </li>
 * </ul>
 *
 * @author Grzegorz Milka
 */
public class LocalEnvironmentFactory implements EnvironmentFactory<Environment> {
  public static final String XML_ENV_CNT_FIELD = "environment-count";
  public static final String XML_DIR_PREFIX_FIELD = "dir-prefix";

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalEnvironmentFactory.class);
  private static final String DEFAULT_DIR_PREFIX = "dfuntest";
  private static final String ENV_CONFIG_ROOT_DIR = "local-environment-factory-root-dir";

  private final Configuration mConfig;
  private final FileUtils mFileUtils;

  public LocalEnvironmentFactory(Configuration config, FileUtils fileUtils) {
    mConfig = config;
    mFileUtils = fileUtils;
  }

  public LocalEnvironmentFactory(Configuration config) {
    this(config, FileUtilsImpl.getFileUtilsImpl());
  }

  @Override
  public Collection<Environment> create() throws IOException {
    LOGGER.info("create()");
    int count = mConfig.getInteger(XML_ENV_CNT_FIELD, 0);
    if (count <= 0) {
      throw new IllegalArgumentException(
          "Number of environments has not been provided or was invalid.");
    }
    String dirPrefix = mConfig.getString(XML_DIR_PREFIX_FIELD, DEFAULT_DIR_PREFIX);

    Collection<Environment> environments = new LinkedList<>();
    for (int envIdx = 0; envIdx < count; ++envIdx) {
      Path tempDirPath;
      tempDirPath = mFileUtils.createTempDirectory(dirPrefix);
      LocalEnvironment env = new LocalEnvironment(envIdx, tempDirPath, mFileUtils);
      env.setProperty(ENV_CONFIG_ROOT_DIR, tempDirPath);
      environments.add(env);
    }
    return environments;
  }

  @Override
  public void destroy(Collection<Environment> envs) {
    LOGGER.info("destroy()");
    for (Environment env: envs) {
      Path dirPath;
      try {
        dirPath = (Path) env.getProperty(ENV_CONFIG_ROOT_DIR);
      } catch (ClassCastException | NoSuchElementException e) {
        LOGGER.error("Could not destroy environment.", e);
        continue;
      }
      mFileUtils.deleteQuietly(dirPath.toFile());
    }
  }
}
