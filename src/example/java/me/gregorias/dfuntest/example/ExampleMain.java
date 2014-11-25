package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.Environment;
import me.gregorias.dfuntest.EnvironmentFactory;
import me.gregorias.dfuntest.LocalEnvironmentFactory;
import me.gregorias.dfuntest.RunnerBuilder;
import me.gregorias.dfuntest.SSHEnvironmentFactory;
import me.gregorias.dfuntest.TestResult;
import me.gregorias.dfuntest.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;

/**
 * Main class for running dfuntest for PingApplication. This class shows how a typical preparation
 * of dfuntest should look and involves:
 * parsing input (here through command line arguments, but can also be done through for example xml
 * config file);
 * depending on input settings preparing appriopiate EnvironmentFactory, EnvironmentPreparator,
 * ApplicationFactory and choosing TestScript;
 * running the TestRunner.
 *
 * The main should be run with following arguments:
 * TEST_NAME - either: sanity, pinggetid, distributedping,
 * INITIAL_PORT - initial port number assigned to first application,
 * Either:
 *   local ENV_COUNT - run tests locally with ENV_COUNT environments
 *   ssh USERNAME PRIVATE_KEY_PATH HOSTS... - run tests on given ssh hosts which are accessible for
 *     USERNAME user with given private key
 *
 * This Main assumes that in current working directory there is dfuntest-example.jar with
 * dfuntest.example package and lib directory with all necessary dependencies.
 *
 * Run "./gradlew build cAD" and copy build/libs/dfuntest.jar to lib/ to ensure all
 * dependencies are in place.
 */
public class ExampleMain {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMain.class);
  private static final String USAGE = "Usage: ExampleMain (sanity | pinggetid | distributedping) "
          +  "INITIAL_PORT (local ENV_COUNT | ssh USERNAME PRIVATE_KEY_PATH HOSTS...)";
  private static final Path REPORT_PATH = FileSystems.getDefault().getPath("report");
  private static final String ENV_DIR_PREFIX = "Example";

  public static void main(String[] args) {
    if (args.length < 4) {
      LOGGER.error(USAGE);
      System.exit(1);
    }

    String testType = args[0];
    int initialPort = Integer.parseInt(args[1]);
    EnvironmentFactory<Environment> environmentFactory = null;
    try {
      environmentFactory = initializeEnvironmentFactory(args);
    } catch (UnknownHostException e) {
      LOGGER.error("main(): Could not resolve a host address.", e);
      System.exit(1);
    }
    if (environmentFactory == null) {
      System.exit(1);
    }
    RunnerBuilder<Environment, ExampleApp> builder = new RunnerBuilder<>();

    builder.setEnvironmentFactory(environmentFactory);
    builder.setEnvironmentPreparator(new ExampleEnvironmentPreparator(initialPort));
    builder.setApplicationFactory(new ExampleAppFactory());
    switch (testType) {
      case "sanity":
        builder.addTestScript(new ExampleSanityTestScript());
        break;
      case "pinggetid":
        builder.addTestScript(new ExamplePingGetIDTestScript());
        break;
      case "distributedping":
        builder.addTestScript(new ExampleDistributedPingTestScript());
        break;
      default:
        LOGGER.error("USAGE");
        System.exit(1);
        return;
    }
    builder.setShouldPrepareEnvironments(true);
    builder.setShouldCleanEnvironments(true);
    builder.setReportPath(REPORT_PATH);

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

  private static EnvironmentFactory<Environment> initializeEnvironmentFactory(String[] args)
      throws UnknownHostException {
    switch (args[2]) {
      case "local":
        int envCount = Integer.parseInt(args[3]);
        return new LocalEnvironmentFactory(envCount, ENV_DIR_PREFIX);
      case "ssh":
        if (args.length < 6) {
          LOGGER.error(USAGE);
          return null;
        }
        Collection<InetAddress> hosts = new ArrayList<>();
        for (int idx = 5; idx < args.length; ++idx) {
          hosts.add(InetAddress.getByName(args[idx]));
        }
        return new SSHEnvironmentFactory(hosts,
            args[3],
            FileSystems.getDefault().getPath(args[4]),
            ENV_DIR_PREFIX,
            Executors.newCachedThreadPool());
      default:
        throw new UnsupportedOperationException(String.format("Unsupported environment type: %s",
            args[0]));
    }
  }
}
