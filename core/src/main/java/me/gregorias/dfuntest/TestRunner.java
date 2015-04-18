package me.gregorias.dfuntest;

/**
 * A point of entry for running tests. This objects combines other classes that
 * prepare environment, run test ({@link TestScript}) and save the results.
 *
 * @author Grzegorz Milka
 */
public interface TestRunner {
  TestResult run();
}
