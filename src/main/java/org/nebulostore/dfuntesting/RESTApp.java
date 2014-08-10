package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.net.URL;

public abstract class RESTApp extends App {
  private final URL url_;

  public RESTApp(int id, String name, URL url) {
    super(id, name);
    url_ = url;
  }
  
  public URL getURL() {
    return url_;
  }
  
  public abstract void sendGet(String query) throws IOException;
}
