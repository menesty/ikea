package org.menesty.ikea.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Created by Menesty on
 * 10/8/15.
 * 16:05.
 */
public class DateUtil {
  public static Date toDate(LocalDate localDate) {
    Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.of("+0"));
    return Date.from(instant);
  }

  public static String format(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(ColumnUtil.DEFAULT_DATE_FORMAT);
    return sdf.format(date);
  }

  public static LocalDate toLocalDate(Date date) {
    if (date == null) {
      return null;
    }

    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
