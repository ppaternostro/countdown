package com.pasquasoft.tools.countdown;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

public class Util
{
  private static final String REGEX_TIME_MASK = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy")
      .withResolverStyle(ResolverStyle.STRICT);

  public static boolean isValidDate(String dateStr)
  {
    try
    {
      LocalDate.parse(dateStr, FORMATTER);
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
