package org.nebulostore.dfuntesting;

import java.util.Collection;

/**
 * Basic test scenario that tests given functionality of given tested applications.
 * 
 * @author Grzegorz Milka
 *
 * @param <TestedApp>
 */
public interface TestScript<TestedApp extends App> {
	TestResult run(Collection<TestedApp> apps);
}
