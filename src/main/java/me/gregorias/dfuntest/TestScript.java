package me.gregorias.dfuntest;

import java.util.Collection;

/**
 * Basic test scenario that tests functionality of given tested applications.
 *
 * @author Grzegorz Milka
 *
 * @param <TestedApp>
 */
public interface TestScript<TestedApp extends App> {
  /**
   * @param apps applications to test
   * @return result of the test
   */
  TestResult run(Collection<TestedApp> apps);
}
