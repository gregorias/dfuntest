package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalEnvironmentFactory implements EnvironmentFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalEnvironmentFactory.class);
  private static final String XML_ENV_CNT_FIELD = "environment-count";
  private static final String XML_ENV_DIR_FIELD = "dir-prefix";
  private static final String ENV_CONFIG_ROOT_DIR = "root-dir";
  private static final String DEFAULT_DIR_PREFIX = "dfuntesting";

  private final Configuration config_;

  public LocalEnvironmentFactory(Configuration config) {
    config_ = config;
  }

  @Override
  public Collection<Environment> createEnvironments() throws IOException {
    LOGGER.info("createEnvironments()");
    int count = config_.getInteger(XML_ENV_CNT_FIELD, 0);
    if (count <= 0) {
      throw new IllegalArgumentException("Number of environments has not been provided or was invalid.");
    }
    String dirPrefix = config_.getString(XML_ENV_DIR_FIELD, DEFAULT_DIR_PREFIX);
    
    Collection<Environment> environments = new LinkedList<>();
    for (int i = 0; i < count; ++i) {
      Path tempDirPath;
      tempDirPath = Files.createTempDirectory(dirPrefix);
      LocalEnvironment env = new LocalEnvironment(i, tempDirPath);
      env.setProperty(ENV_CONFIG_ROOT_DIR, tempDirPath);
      environments.add(env);
    }
    return environments;
  }

  @Override
  public void destroyEnvironments(Collection<Environment> envs) {
    LOGGER.info("destroyEnvironments()");
    for (Environment env: envs) {
      Path dirPath;
      try {
        dirPath = (Path) env.getProperty(ENV_CONFIG_ROOT_DIR);
      } catch (ClassCastException | NoSuchElementException e) {
        LOGGER.error("Could not destroy environment.", e);
        continue;
      }
      FileUtils.deleteQuietly(dirPath.toFile());
    }
  }
}
