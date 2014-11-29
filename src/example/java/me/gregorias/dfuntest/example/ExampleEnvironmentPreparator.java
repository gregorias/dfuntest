package me.gregorias.dfuntest.example;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import me.gregorias.dfuntest.Environment;
import me.gregorias.dfuntest.EnvironmentPreparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Preparator of testing environments for example tests.
 *
 * This preparator prepares environment files for
 * {@link me.gregorias.dfuntest.example.PingApplication} and sets up environment properties for
 * {@link me.gregorias.dfuntest.example.ExampleApp}.
 *
 * It assumes that:
 * <ul>
 * <li> All required dependency libraries are in lib/ directory. </li>
 * <li> This package is in dfuntest-example.jar file. </li>
 * </ul>
 *
 * @author Grzegorz Milka
 */
public class ExampleEnvironmentPreparator implements EnvironmentPreparator<Environment> {
  public static final String INITIAL_PORT_ARGUMENT_NAME =
      "ExampleEnvironmentPreparator.initialPort";
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleEnvironmentPreparator.class);
  private static final Path LIBS_PATH = FileSystems.getDefault().getPath("lib");
  private static final Path JAR_PATH = FileSystems.getDefault().getPath("dfuntest-example.jar");
  private final int mInitialPort;

  @Inject
  public ExampleEnvironmentPreparator(@Named(INITIAL_PORT_ARGUMENT_NAME) int initialPort) {
    mInitialPort = initialPort;
  }

  @Override
  public void cleanAll(Collection<Environment> envs) {
    String errorMsg = "cleanAll(): Could not clean environment.";
    cleanOutput(envs);
    for (Environment env : envs) {
      try {
        env.removeFile("lib");
        env.removeFile("dfuntest-example.jar");
      } catch (IOException e) {
        LOGGER.error(errorMsg, e);
      } catch (InterruptedException e) {
        LOGGER.warn(errorMsg, e);
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void cleanOutput(Collection<Environment> envs) {
    String errorMsg = "cleanOutput(): Could not clean output in environment.";
    for (Environment env : envs) {
      try {
        env.removeFile(ExampleApp.LOG_FILE);
      } catch (IOException e) {
        LOGGER.error(errorMsg, e);
      } catch (InterruptedException e) {
        LOGGER.warn(errorMsg, e);
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void collectOutput(Collection<Environment> envs, Path destPath) {
    for (Environment env : envs) {
      try {
        env.copyFilesToLocalDisk(ExampleApp.LOG_FILE, destPath.resolve(env.getId() + ""));
      } catch (IOException e) {
        LOGGER.warn("collectOutput(): Could not copy log file.", e);
      }
    }
  }

  @Override
  public void prepare(Collection<Environment> envs) throws IOException {
    LOGGER.info("prepare()");
    Collection<Environment> preparedEnvs = new LinkedList<>();
    for (Environment env : envs) {
      prepareEnvConfiguration(env);
      try {
        String targetPath = ".";
        env.copyFilesFromLocalDisk(JAR_PATH.toAbsolutePath(), targetPath);
        env.copyFilesFromLocalDisk(LIBS_PATH.toAbsolutePath(), targetPath);
        preparedEnvs.add(env);
      } catch (IOException e) {
        cleanAll(preparedEnvs);
        LOGGER.error("prepare() -> Could not prepare environment.", e);
        throw e;
      }
    }
  }

  @Override
  public void restore(Collection<Environment> envs) throws IOException {
    LOGGER.info("restore()");
  }

  private void prepareEnvConfiguration(Environment env) {
    int portSkew = env.getId();
    env.setProperty(ExampleApp.LOCAL_PORT_ENV_FIELD, mInitialPort + portSkew);
    env.setProperty(ExampleApp.SERVER_HOSTNAME_ENV_FIELD, env.getHostname());
    env.setProperty(ExampleApp.SERVER_PORT_ENV_FIELD, mInitialPort);
  }
}
