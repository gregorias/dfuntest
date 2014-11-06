package me.gregorias.dfuntest;

public interface ApplicationFactory<TestedApp extends App> {
  /**
   * Create new application proxy.
   *
   * @param env Environment in which the application runs.
   * @return Application
   */
  TestedApp newApp(Environment env);
}
