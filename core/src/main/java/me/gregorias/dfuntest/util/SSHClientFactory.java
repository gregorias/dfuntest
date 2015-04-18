package me.gregorias.dfuntest.util;

import net.schmizz.sshj.SSHClient;

public class SSHClientFactory {
  private static SSHClientFactory SSH_CLIENT_FACTORY = null;

  public static synchronized SSHClientFactory getSSHClientFactory() {
    if (SSH_CLIENT_FACTORY == null) {
      SSH_CLIENT_FACTORY = new SSHClientFactory();
    }
    return SSH_CLIENT_FACTORY;
  }

  public SSHClient newSSHClient() {
    return new SSHClient();
  }
}
