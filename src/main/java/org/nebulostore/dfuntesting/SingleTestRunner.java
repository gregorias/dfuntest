package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import org.nebulostore.dfuntesting.TestResult.Type;
import org.slf4j.Logger;

/**
 * Basic implementation of TestRunner which runs one test script on given environments and apps.
 * 
 * @author Grzegorz Milka
 *
 * @param <TestedApp>
 */
public class SingleTestRunner<TestedApp extends App> implements TestRunner {
	private final Logger logger_;
	private final TestScript<TestedApp> script_;

	private final EnvironmentFactory environmentFactory_;

	private final EnvironmentPreparator environmentPreparator_;

	private final ApplicationFactory<TestedApp> applicationFactory_;

	public SingleTestRunner(TestScript<TestedApp> script,
			Logger logger,
			EnvironmentFactory environmentFactory,
			EnvironmentPreparator environmentPreparator,
			ApplicationFactory<TestedApp> applicationFactory) {
		script_ = script;

		logger_ = logger;

		environmentFactory_ = environmentFactory;

		environmentPreparator_ = environmentPreparator;

		applicationFactory_ = applicationFactory;
	}

	@Override
	public TestResult run() {
		logger_.info("run(): Starting preparation for test script {}.", script_.toString());
		logger_.info("run(): Creating environments.");
		Collection<Environment> envs;
    try {
      envs = environmentFactory_.createEnvironments();
    } catch (IOException e) {
      logger_.error("run(): Could not create environments.", e);
			return new TestResult(Type.FAILURE, "Could not create environments.");
    }
		try {
      logger_.info("run(): Preparing environments.");
			environmentPreparator_.prepareEnvironments(envs);
			logger_.info("run(): Environments prepared: ", envs.size());
		} catch (ExecutionException e) {
			logger_.error("run(): Could not prepare environments.", e);
			environmentFactory_.destroyEnvironments(envs);
			return new TestResult(Type.FAILURE, "Could not prepare environments.");
		}

		Collection<TestedApp> apps = new LinkedList<>();
		for (Environment env: envs) {
			apps.add(applicationFactory_.newApp(env));
		}

		TestResult result = script_.run(apps);
		
		environmentPreparator_.collectOutputAndLogFiles(envs);
		environmentPreparator_.cleanEnvironments(envs);
		environmentFactory_.destroyEnvironments(envs);

		return result;
	}
}
