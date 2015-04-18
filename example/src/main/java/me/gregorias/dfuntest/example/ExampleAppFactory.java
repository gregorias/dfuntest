package me.gregorias.dfuntest.example;

import me.gregorias.dfuntest.ApplicationFactory;
import me.gregorias.dfuntest.Environment;

/**
 * AppFactory which creates apps based on environment.
 */
public class ExampleAppFactory implements ApplicationFactory<Environment, ExampleApp> {
  @Override
  public ExampleApp newApp(Environment env) {
    return new ExampleApp(env.getId(), Integer.toString(env.getId()), env);
  }
}
