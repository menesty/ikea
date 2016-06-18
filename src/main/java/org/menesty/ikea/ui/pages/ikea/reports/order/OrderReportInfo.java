package org.menesty.ikea.ui.pages.ikea.reports.order;

import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.service.parser.FileParseStatistic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 5/24/16.
 * 15:53.
 */
public class OrderReportInfo {
  private List<File> files;
  private IkeaProcessOrder ikeaProcessOrder;
  private List<FileParseStatistic> fileParseStatistics;

  public List<FileParseStatistic> getFileParseStatistics() {
    return fileParseStatistics;
  }

  public void setFileParseStatistics(List<FileParseStatistic> fileParseStatistics) {
    this.fileParseStatistics = fileParseStatistics;
  }

  public IkeaProcessOrder getIkeaProcessOrder() {
    return ikeaProcessOrder;
  }

  public void setIkeaProcessOrder(IkeaProcessOrder ikeaProcessOrder) {
    this.ikeaProcessOrder = ikeaProcessOrder;
  }

  public List<File> getFiles() {
    return files;
  }

  public void setFiles(List<File> files) {
    this.files = files;
  }
}
