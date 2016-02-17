package org.menesty.ikea.util;

/**
 * Created by Menesty on
 * 2/17/16.
 * 13:57.
 */
public class PaginationUtil {
  public static int getPageCount(int itemCount, int pageSize) {
    return itemCount / pageSize + (itemCount % pageSize == 0 ? 0 : 1);
  }

  public static int getPageNumber(int pageIndex) {
    return pageIndex + 1;
  }
}
