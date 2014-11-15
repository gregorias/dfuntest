package me.gregorias.dfuntest;

import java.util.Collection;

/**
 * Basic test scenario that tests functionality of given tested applications.
 *
 * @author Grzegorz Milka
 *
 * @param <AppT>
 */
public interface TestScript<AppT extends App> {
  /**
   * @param apps applications to test
   * @return result of the test
   */
  TestResult run(Collection<AppT> apps);
}
