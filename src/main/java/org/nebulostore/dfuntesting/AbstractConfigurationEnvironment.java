package org.nebulostore.dfuntesting;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment which implements configuration settings.
 * 
 * @author Grzegorz Milka
 */
public abstract class AbstractConfigurationEnvironment implements Environment {
  private final Map<String, Object> mConfig;
  
  public AbstractConfigurationEnvironment() {
    mConfig = new HashMap<>();
  }

  @Override
  public Object getProperty(String key) {
    return mConfig.get(key);
  }


  @Override
  public void setProperty(String key, Object value) {
    mConfig.put(key, value);
  }
}