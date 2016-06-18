package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.dto.ikea.order.NewOrderItemInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 6/14/16.
 * 16:08.
 */
public class AddToOrderItemService extends AbstractAsyncService<Void> {
  private ObjectProperty<List<NewOrderItemInfo>> choiceCountResultProperty = new SimpleObjectProperty<>();
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();

  @Override
  protected Task<Void> createTask() {
    final List<NewOrderItemInfo> _newOrderItemInfo = choiceCountResultProperty.get();
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/ikea-order/order/item/add/" + _ikeaProcessOrderId);
        request.postData(_newOrderItemInfo);
        return null;
      }
    };
  }

  public void setChoiceCountResult(Long ikeaProcessOrderId, List<NewOrderItemInfo> newOrderItemInfos) {
    choiceCountResultProperty.setValue(newOrderItemInfos);
    ikeaProcessOrderIdProperty.setValue(ikeaProcessOrderId);
  }
}
