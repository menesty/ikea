package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 6/16/16.
 * 11:00.
 */
public class ReturnItemService extends AbstractAsyncService<Boolean> {
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();
  private LongProperty invoiceItemIdProperty = new SimpleLongProperty();
  private ObjectProperty<BigDecimal> itemCount = new SimpleObjectProperty<>();

  @Override
  protected Task<Boolean> createTask() {
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    final Long _invoiceItemId = invoiceItemIdProperty.get();
    final BigDecimal _count = itemCount.get();

    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/" + _ikeaProcessOrderId + "/invoice/item/" + _invoiceItemId + "/return-back/" + _count.toString());
        return request.getData(Boolean.class);
      }
    };
  }

  public void setData(Long ikeaProcessOrderId, Long invoiceItemId, BigDecimal count) {
    ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
    invoiceItemIdProperty.setValue(invoiceItemId);
    itemCount.setValue(count);
  }
}
