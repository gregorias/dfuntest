package me.gregorias.dfuntest;


/**
 * Factory which creates application working on given environment.
 *
 * @param <EnvironmentT>
 * @param <AppT>
 */
public interface ApplicationFactory<
    EnvironmentT extends Environment, AppT extends App<EnvironmentT>> {
  /**
   * Create new application proxy.
   *
   * @param env Environment in which the application runs.
   * @return Application
   */
  AppT newApp(EnvironmentT env);
}
