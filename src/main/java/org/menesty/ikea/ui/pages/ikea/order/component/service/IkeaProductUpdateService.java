package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

/**
 * Created by Menesty on
 * 1/15/16.
 * 12:35.
 */
public class IkeaProductUpdateService extends AbstractAsyncService<Boolean> {
  private ObjectProperty<IkeaProduct> ikeaProductProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<Boolean> createTask() {
    final IkeaProduct _ikeaProduct = ikeaProductProperty.get();

    return new Task<Boolean>() {
      @Override
      protected Boolean call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/product/update");

        return request.postData(_ikeaProduct, Boolean.class);
      }
    };
  }

  public void setIkeaProduct(IkeaProduct ikeaProduct) {
    ikeaProductProperty.set(ikeaProduct);
  }
}
