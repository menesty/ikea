package org.menesty.ikea.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Menesty on
 * 2/26/16.
 * 13:21.
 */
public class DateFormatter {
  private final SimpleDateFormat sdf;

  public DateFormatter(String format) {
    sdf = new SimpleDateFormat(format);
  }

  public String format(Date date) {
    if (date != null) {
      return sdf.format(date);
    }

    return "";
  }
}
