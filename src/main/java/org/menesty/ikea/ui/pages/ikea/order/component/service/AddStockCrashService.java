package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockCrashItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

/**
 * Created by Menesty on
 * 6/18/16.
 * 15:16.
 */
public class AddStockCrashService extends AbstractAsyncService<Boolean> {
  private ObjectProperty<StockCrashItem> stockCrashItemProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<Boolean> createTask() {
    final StockCrashItem _stockCrashItem = stockCrashItemProperty.get();

    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/" + _stockCrashItem.getIkeaProcessedOrderId() + "/stock/crash/add");

        return request.postData(_stockCrashItem, Boolean.class);
      }
    };
  }

  public void setData(StockCrashItem item) {
    stockCrashItemProperty.setValue(item);
  }
}
