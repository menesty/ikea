package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

/**
 * Created by Menesty on
 * 6/16/16.
 * 10:57.
 */
public class IkeaProcessOrderResetStateService extends AbstractAsyncService<Boolean> {
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

  @Override
  protected Task<Boolean> createTask() {
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/warehouse/" + _ikeaProcessOrderId + "/reset");
        return request.getData(Boolean.class);
      }
    };
  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
    this.ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
  }
}
