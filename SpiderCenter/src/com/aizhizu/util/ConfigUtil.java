package com.aizhizu.util;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ConfigUtil
{
  private static Configuration config = null;
  private static String configFile = "./data.properties";

  static { if (config == null)
      try {
        config = new PropertiesConfiguration(configFile);
      } catch (ConfigurationException e) {
        e.printStackTrace();
      }
  }

  @SuppressWarnings("unchecked")
public static void Init()
  {
    System.out.println("[ConfigCenter Init Complete]");
    Iterator<String> iterator = config.getKeys();
    String keys = "";
    while (iterator.hasNext()) {
      String key = (String)iterator.next();
      keys = keys + key + ", ";
    }
    System.out.println(keys);
  }

  public static String getString(String arg0) {
    return config.getString(arg0);
  }

  public static int getInt(String arg0) {
    return config.getInt(arg0);
  }

  public static boolean getBoolean(String arg0) {
    return config.getBoolean(arg0);
  }
}