package me.moxun.dreamcatcher.connector.server;


import me.moxun.dreamcatcher.connector.util.ProcessUtil;

public class AddressNameHelper {
  private static final String PREFIX = "dreamcatcher";

  public static String createCustomAddress(String suffix) {
    return
        PREFIX +
        ProcessUtil.getProcessName() +
        suffix;
  }
}
