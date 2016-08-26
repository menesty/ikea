package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

/**
 * Created by Menesty on
 * 6/21/16.
 * 18:42.
 */
public class DeleteStockItemService extends AbstractAsyncService<Boolean> {
  public enum StockAction {
    DELETE_RETURN_ITEM, DELETE_STOCK_CRASH_ITEM
  }

  private ObjectProperty<Object> objectObjectProperty = new SimpleObjectProperty<>();
  private ObjectProperty<StockAction> stockActionProperty = new SimpleObjectProperty<>();
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

  @Override
  protected Task<Boolean> createTask() {
    final Object _object = objectObjectProperty.get();
    final StockAction _stockAction = stockActionProperty.get();
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();

    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        String url;
        if (StockAction.DELETE_STOCK_CRASH_ITEM == _stockAction) {
          url = "/ikea-order/" + _ikeaProcessOrderId + "/stock/crash/delete";
          APIRequest request = HttpServiceUtil.get(url);

          return request.postData(_object, Boolean.class);
        } else {
          url = "/ikea-order/" + _ikeaProcessOrderId + "/return-back/" + _object + "/delete";
          APIRequest request = HttpServiceUtil.get(url);
          return request.getData(Boolean.class);
        }
      }
    };
  }

  public void setData(StockAction action, Object object) {
    objectObjectProperty.setValue(object);
    stockActionProperty.setValue(action);

  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
    ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
  }
}
