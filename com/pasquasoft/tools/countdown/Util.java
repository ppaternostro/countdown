package com.pasquasoft.tools.countdown;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Util
{
  private static String PROP_FILE = "countdown.properties";

  public static void saveProperties(Properties prop) throws IOException
  {
    FileOutputStream fos = null;

    try
    {
      fos = new FileOutputStream(PROP_FILE);
      prop.store(fos, null);
    }
    finally
    {
      try
      {
        if (fos != null)
        {
          fos.close();
        }
      }
      catch (IOException ioe)
      {
        // No blood, no foul!
      }
    }
  }

  public static Properties readProperties()
  {
    Properties prop = new Properties();
    FileInputStream fis = null;

    try
    {
      fis = new FileInputStream(PROP_FILE);
      prop.load(fis);
    }
    catch (IOException ioe)
    {
      // Ignore as properties file may not yet exist.
    }
    finally
    {
      try
      {
        if (fis != null)
        {
          fis.close();
        }
      }
      catch (IOException ioe)
      {
        // No blood, no foul!
      }
    }

    return prop;
  }
}
