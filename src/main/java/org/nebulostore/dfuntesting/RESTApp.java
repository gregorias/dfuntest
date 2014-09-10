package org.nebulostore.dfuntesting;

import java.io.IOException;
import java.net.URL;

public abstract class RESTApp extends App {
  private final URL mUrl;

  public RESTApp(int id, String name, URL url) {
    super(id, name);
    mUrl = url;
  }

  public URL getURL() {
    return mUrl;
  }

  public abstract void sendGet(String query) throws IOException;
}
