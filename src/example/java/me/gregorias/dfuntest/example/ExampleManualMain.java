package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.Environment;
import me.gregorias.dfuntest.EnvironmentFactory;
import me.gregorias.dfuntest.LocalEnvironmentFactory;
import me.gregorias.dfuntest.testrunnerbuilders.ManualTestRunnerBuilder;
import me.gregorias.dfuntest.SSHEnvironmentFactory;
import me.gregorias.dfuntest.TestResult;
import me.gregorias.dfuntest.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Executors;

/**
 * <p>
 * Main class for running dfuntest for PingApplication. This class shows how a typical preparation
 * of dfuntest could look and involves:
 * <ul>
 * <li>parsing input (here through command line arguments, but can also be done through for example
 * xml config file)</li>
 * <li>depending on input settings preparing appriopiate EnvironmentFactory, EnvironmentPreparator,
 * ApplicationFactory and choosing TestScript;</li>
 * <li>running the TestRunner.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The main should be run with following arguments:
 * INITIAL_PORT - initial port number assigned to first application,
 * Either:
 * <ul>
 *   <li>local ENV_COUNT - run tests locally with ENV_COUNT environments</li>
 *   <li>ssh USERNAME PRIVATE_KEY_PATH HOSTS... - run tests on given ssh hosts which are accessible for
 *     USERNAME user with given private key</li>
 * </ul>
 * </p>
 *
 * <p>
 * This Main assumes that in current working directory there is dfuntest-example.jar with
 * dfuntest.example package and lib directory with all necessary dependencies.
 * </p>
 *
 * <p>
 * Run "./gradlew cERD" to ensure all dependencies are in place.
 * </p>
 */
public class ExampleManualMain {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleManualMain.class);
  private static final String USAGE = "Usage: ExampleManualMain INITIAL_PORT "
      + "(local ENV_COUNT | ssh USERNAME PRIVATE_KEY_PATH HOSTS...)";
  private static final String REPORT_PATH_PREFIX = "report_";

  private static final String ENV_DIR_PREFIX = "Example";

  public static void main(String[] args) {
    if (args.length < 3) {
      LOGGER.error(USAGE);
      System.exit(1);
    }

    int initialPort = Integer.parseInt(args[0]);
    EnvironmentFactory<Environment> environmentFactory = null;
    environmentFactory = initializeEnvironmentFactory(args);
    if (environmentFactory == null) {
      System.exit(1);
    }
    ManualTestRunnerBuilder<Environment, ExampleApp> builder = new ManualTestRunnerBuilder<>();

    builder.setEnvironmentFactory(environmentFactory);
    builder.setEnvironmentPreparator(new ExampleEnvironmentPreparator(initialPort));
    builder.setApplicationFactory(new ExampleAppFactory());

    builder.addTestScript(new ExampleSanityTestScript());
    builder.addTestScript(new ExamplePingGetIDTestScript());
    builder.addTestScript(new ExampleDistributedPingTestScript());

    builder.setShouldPrepareEnvironments(true);
    builder.setShouldCleanEnvironments(true);
    builder.setReportPath(calculateReportPath());

    TestRunner testRunner = builder.buildRunner();
    TestResult result = testRunner.run();

    int status;
    String resultStr;
    if (result.getType() == TestResult.Type.SUCCESS) {
      status = 0;
      resultStr = "successfully";
    } else {
      status = 1;
      resultStr = "with failure";
    }
    LOGGER.info("main(): Test has ended {} with description: {}", resultStr,
        result.getDescription());
    System.exit(status);
  }

  private static String calculateCurrentTimeStamp() {
    return new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date());
  }

  private static Path calculateReportPath() {
    return FileSystems.getDefault().getPath(REPORT_PATH_PREFIX + calculateCurrentTimeStamp());
  }

  private static EnvironmentFactory<Environment> initializeEnvironmentFactory(String[] args) {
    switch (args[1]) {
      case "local":
        int envCount = Integer.parseInt(args[2]);
        return new LocalEnvironmentFactory(envCount, ENV_DIR_PREFIX);
      case "ssh":
        if (args.length < 5) {
          LOGGER.error(USAGE);
          return null;
        }
        Collection<String> hosts = new ArrayList<>();
        for (int idx = 4; idx < args.length; ++idx) {
          hosts.add(args[idx]);
        }
        return new SSHEnvironmentFactory(hosts,
            args[2],
            FileSystems.getDefault().getPath(args[3]),
            ENV_DIR_PREFIX,
            Executors.newCachedThreadPool());
      default:
        throw new UnsupportedOperationException(String.format("Unsupported environment type: %s",
            args[1]));
    }
  }
}
