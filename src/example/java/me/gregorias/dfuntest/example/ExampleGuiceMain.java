package me.gregorias.dfuntest.example;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import me.gregorias.dfuntest.ApplicationFactory;
import me.gregorias.dfuntest.Environment;
import me.gregorias.dfuntest.EnvironmentFactory;
import me.gregorias.dfuntest.EnvironmentPreparator;
import me.gregorias.dfuntest.LocalEnvironmentFactory;
import me.gregorias.dfuntest.MultiTestRunner;
import me.gregorias.dfuntest.SSHEnvironmentFactory;
import me.gregorias.dfuntest.TestResult;
import me.gregorias.dfuntest.TestRunner;
import me.gregorias.dfuntest.TestScript;
import me.gregorias.dfuntest.testrunnerbuilders.GuiceTestRunnerModule;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * <p>
 * Main class for running dfuntest for PingApplication. It uses Guice and automatic configuration
 * parsing to smooth out dependency injection.
 * </p>
 *
 * <p>
 * It expects user to specify which environment type to use and hierarchical specification of
 * parameters. Names of paramaters directly correspond to Guice's Named tag value.
 * </p>
 *
 * <p>
 * This class provides default values for most paramaters, but they are overridden by config file
 * specification on conflict. Config file values are overriden by command line arguments on
 * conflict.
 * </p>
 *
 * <p>
 * Example usage:
 *
 * {@code ExampleGuiceMain --env-factory local --config-file resource/config/exampledfuntest.xml
 * --config LocalEnvironmentFactory.environmentCount=10}
 * </p>
 *
 * <p>
 * For help:
 *
 * {@code ExampleGuiceMain --help}
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
public class ExampleGuiceMain {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleGuiceMain.class);

  private static final String CONFIG_OPTION = "config";
  private static final String CONFIG_FILE_OPTION = "config-file";
  private static final String ENV_FACTORY_OPTION = "env-factory";
  private static final String HELP_OPTION = "help";

  private static final String REPORT_PATH_PREFIX = "report_";
  private static final String ENV_DIR_PREFIX = "Example";

  private static final Map<String, String> DEFAULT_PROPERTIES = newDefaultProperties();

  private static Class<? extends EnvironmentFactory<Environment>> mEnvironmentFactoryClass;

  public static void main(String[] args) {
    mEnvironmentFactoryClass = null;
    Map<String, String> properties = new HashMap<>(DEFAULT_PROPERTIES);
    Map<String, String> argumentProperties;
    try {
      argumentProperties = parseAndProcessArguments(args);
    } catch (ConfigurationException | IllegalArgumentException | ParseException e) {
      System.exit(1);
      return;
    }

    if (argumentProperties == null) {
      return;
    }

    properties.putAll(argumentProperties);

    if (mEnvironmentFactoryClass == null) {
      LOGGER.error("main(): EnvironmentFactory has not been set.");
      System.exit(1);
      return;
    }

    GuiceTestRunnerModule<Environment, ExampleApp> guiceBaseModule = new GuiceTestRunnerModule<>();
    guiceBaseModule.addProperties(properties);

    ExampleGuiceModule guiceExampleModule = new ExampleGuiceModule();

    Injector injector = Guice.createInjector(guiceBaseModule, guiceExampleModule);
    TestRunner testRunner = injector.getInstance(TestRunner.class);

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

  private static class ExampleGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(new AppFactoryTypeLiteral()).to(ExampleAppFactory.class).in(Singleton.class);
      bind(new EnvironmentPreparatorTypeLiteral()).to(ExampleEnvironmentPreparator.class)
          .in(Singleton.class);
      bind(new EnvironmentFactoryTypeLiteral()).to(mEnvironmentFactoryClass).in(Singleton.class);
      Multibinder<TestScript<ExampleApp>> multiBinder = Multibinder.newSetBinder(binder(),
          new TestScriptTypeLiteral(), Names.named(MultiTestRunner.SCRIPTS_ARGUMENT_NAME));
      multiBinder.addBinding().toInstance(new ExampleSanityTestScript());
      multiBinder.addBinding().toInstance(new ExamplePingGetIDTestScript());
      multiBinder.addBinding().toInstance(new ExampleDistributedPingTestScript());
      bind(TestRunner.class).to(new MultiTestRunnerTypeLiteral()).in(Singleton.class);
    }

    @Provides
    @Named(SSHEnvironmentFactory.EXECUTOR_ARGUMENT_NAME)
    @Singleton
    @SuppressWarnings("unused")
    Executor provideSSHEnvironmentFactoryExecutor() {
      return Executors.newCachedThreadPool();
    }


    private static class AppFactoryTypeLiteral
        extends TypeLiteral<ApplicationFactory<Environment, ExampleApp>> {
    }

    private static class EnvironmentFactoryTypeLiteral
        extends TypeLiteral<EnvironmentFactory<Environment>> {
    }

    private static class EnvironmentPreparatorTypeLiteral
        extends TypeLiteral<EnvironmentPreparator<Environment>> {
    }

    private static class TestScriptTypeLiteral extends TypeLiteral<TestScript<ExampleApp>> {
    }
  }

  private static class MultiTestRunnerTypeLiteral extends
      TypeLiteral<MultiTestRunner<Environment, ExampleApp>> {
  }

  private static String calculateCurrentTimeStamp() {
    return new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date());
  }

  private static Path calculateReportPath() {
    return FileSystems.getDefault().getPath(REPORT_PATH_PREFIX + calculateCurrentTimeStamp());
  }

  private static Options createOptions() {
    Options options = new Options();

    OptionBuilder.withLongOpt(CONFIG_OPTION);
    OptionBuilder.hasArgs();
    OptionBuilder.withValueSeparator(' ');
    OptionBuilder.withDescription("Configure initial dependencies. Arguments should be a list of"
        + " the form: a.b=value1 a.c=value2.");
    Option configOption = OptionBuilder.create();

    OptionBuilder.withLongOpt(CONFIG_FILE_OPTION);
    OptionBuilder.hasArg();
    OptionBuilder.withDescription("XML configuration filename.");
    Option configFileOption = OptionBuilder.create();

    OptionBuilder.withLongOpt(ENV_FACTORY_OPTION);
    OptionBuilder.hasArg();
    OptionBuilder.isRequired();
    OptionBuilder.withDescription("Environment factory name. Can be either local or ssh.");

    Option envFactoryOption = OptionBuilder.create();

    options.addOption(configOption);
    options.addOption(configFileOption);
    options.addOption(envFactoryOption);
    options.addOption("h", HELP_OPTION, false, "Print help.");
    return options;
  }

  private static Map<String, String> newDefaultProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put(LocalEnvironmentFactory.DIR_PREFIX_ARGUMENT_NAME, ENV_DIR_PREFIX);
    properties.put(SSHEnvironmentFactory.REMOTE_DIR_ARGUMENT_NAME, ENV_DIR_PREFIX);
    properties.put(MultiTestRunner.SHOULD_PREPARE_ARGUMENT_NAME, "true");
    properties.put(MultiTestRunner.SHOULD_CLEAN_ARGUMENT_NAME, "true");
    properties.put(MultiTestRunner.REPORT_PATH_ARGUMENT_NAME, calculateReportPath().toString());
    return properties;
  }

  private static Map<String,String> parseAndProcessArguments(String[] args)
      throws ConfigurationException, ParseException {
    Map<String, String> properties = new HashMap<>();
    CommandLineParser parser = new BasicParser();
    Options options = createOptions();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      LOGGER.error("parseAndProcessArguments(): ParseException caught parsing arguments.", e);
      throw e;
    }

    if (cmd.hasOption(HELP_OPTION)) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("ExampleGuiceMain", options);
      return null;
    }

    if (cmd.hasOption(CONFIG_FILE_OPTION)) {
      String argValue = cmd.getOptionValue(CONFIG_FILE_OPTION);
      HierarchicalConfiguration config;
      try {
        config = new XMLConfiguration(argValue);
      } catch (ConfigurationException e) {
        LOGGER.error("parseAndProcessArguments(): ConfigurationException caught when reading"
            + " configuration file.", e);
        throw e;
      }
      properties.putAll(constructPropertiesFromRootsChildren(config));
    }

    if (cmd.hasOption(CONFIG_OPTION)) {
      String[] argValues = cmd.getOptionValues(CONFIG_OPTION);
      for (String arg : argValues) {
        String[] keyAndValue = arg.split("=", 2);
        String key = keyAndValue[0];
        String value = "";
        if (keyAndValue.length == 2) {
          value = keyAndValue[1];
        }
        properties.put(key, value);
      }
    }

    if (cmd.hasOption(ENV_FACTORY_OPTION)) {
      String argValue = cmd.getOptionValue(ENV_FACTORY_OPTION);
      switch (argValue) {
        case "local":
          mEnvironmentFactoryClass = LocalEnvironmentFactory.class;
          break;
        case "ssh":
          mEnvironmentFactoryClass = SSHEnvironmentFactory.class;
          break;
        default:
          String errorMsg = "Unknown environment factory " + argValue;
          LOGGER.error("parseAndProcessArguments(): {}", errorMsg);
          throw new IllegalArgumentException(errorMsg);
      }
    }
    return properties;
  }

  private static Map<String, String> constructPropertiesFromRootsChildren(
      HierarchicalConfiguration config) {
    Map<String, String> properties = new HashMap<>();
    List<ConfigurationNode> rootsChildrenList = config.getRoot().getChildren();
    for (ConfigurationNode child : rootsChildrenList) {
      HierarchicalConfiguration subConfig = config.configurationAt(child.getName());
      properties.putAll(GuiceTestRunnerModule.configurationToProperties(subConfig));
    }
    return properties;
  }
}
