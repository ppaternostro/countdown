package com.pasquasoft.tools.countdown;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Properties;

public class Util
{
  private static final String REGEX_TIME_MASK = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
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

  public static boolean isValidDate(String dateStr)
  {
    try
    {
      FORMATTER.parse(dateStr);
    }
    catch (DateTimeParseException | NullPointerException e)
    {
      return false;
    }

    return true;
  }

  public static boolean isValidTime(String timeStr)
  {
    return Objects.nonNull(timeStr) && timeStr.matches(REGEX_TIME_MASK);
  }
}
