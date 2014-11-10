package me.gregorias.dfuntest;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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
  public Object getProperty(String key) throws NoSuchElementException {
    if (mConfig.containsKey(key)) {
      return mConfig.get(key);
    } else {
      throw new NoSuchElementException("Key: " + key + " is not present.");
    }
  }


  @Override
  public void setProperty(String key, Object value) {
    mConfig.put(key, value);
  }
}