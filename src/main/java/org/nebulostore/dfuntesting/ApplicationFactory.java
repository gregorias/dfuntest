package org.nebulostore.dfuntesting;

public interface ApplicationFactory<TestedApp extends App> {
  TestedApp newApp(Environment env);
}
