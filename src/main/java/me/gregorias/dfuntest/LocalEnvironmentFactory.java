package me.gregorias.dfuntest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import me.gregorias.dfuntest.util.FileUtils;
import me.gregorias.dfuntest.util.FileUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of {@link LocalEnvironment} working in local temporary directories.
 *
 * @author Grzegorz Milka
 */
public class LocalEnvironmentFactory implements EnvironmentFactory<Environment> {
  public static final String ENV_COUNT_ARGUMENT_NAME = "LocalEnvironmentFactory.environmentCount";
  public static final String DIR_PREFIX_ARGUMENT_NAME = "LocalEnvironmentFactory.dirPrefix";
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalEnvironmentFactory.class);
  private static final String ENV_CONFIG_ROOT_DIR = "localEnvironmentFactoryRootDir";

  private final int mEnvironmentCount;
  private final String mDirPrefix;
  private final FileUtils mFileUtils;

  /**
   * @param environmentCount number of environments create will make
   * @param dirPrefix directory prefix used for creating temporary directories
   * @param fileUtils FileUtils to use
   */
  public LocalEnvironmentFactory(int environmentCount, String dirPrefix, FileUtils fileUtils) {
    if (environmentCount <= 0) {
      throw new IllegalArgumentException("Number of environments was nonpositive.");
    }
    mEnvironmentCount = environmentCount;
    mDirPrefix = dirPrefix;
    mFileUtils = fileUtils;
  }

  @Inject
  public LocalEnvironmentFactory(
      @Named(ENV_COUNT_ARGUMENT_NAME) int environmentCount,
      @Named(DIR_PREFIX_ARGUMENT_NAME) String dirPrefix) {
    this(environmentCount, dirPrefix, FileUtilsImpl.getFileUtilsImpl());
  }

  @Override
  public Collection<Environment> create() throws IOException {
    LOGGER.info("create()");

    Collection<Environment> environments = new ArrayList<>();
    for (int envIdx = 0; envIdx < mEnvironmentCount; ++envIdx) {
      Path tempDirPath;
      tempDirPath = mFileUtils.createTempDirectory(mDirPrefix);
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
