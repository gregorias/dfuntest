package org.nebulostore.dfuntesting;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment which implements configuration settings.
 * 
 * @author Grzegorz Milka
 */
public abstract class AbstractConfigurationEnvironment implements Environment {
  private final Map<String, Object> config_;
  
  public AbstractConfigurationEnvironment() {
    config_ = new HashMap<>();
  }

  @Override
  public Object getProperty(String key) {
    return config_.get(key);
  }


  @Override
  public void setProperty(String key, Object value) {
    config_.put(key, value);
  }
}