package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StorageCalculationResultDto;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 6/16/16.
 * 10:57.
 */
public class StorageCalculationService extends AbstractAsyncService<StorageCalculationResultDto> {
  private LongProperty ikeaProcessOrderIdProperty = new SimpleLongProperty();
  private ObjectProperty<List<Long>> profileIdsProperty = new SimpleObjectProperty<>();
  private BooleanProperty unloadingProperty = new SimpleBooleanProperty();

  @Override
  protected Task<StorageCalculationResultDto> createTask() {
    final Long _ikeaProcessOrderId = ikeaProcessOrderIdProperty.get();
    final List<Long> _profileIds = profileIdsProperty.get();
    final boolean _unloading = unloadingProperty.get();

    return new Task<StorageCalculationResultDto>() {
      @Override
      protected StorageCalculationResultDto call() throws Exception {
        APIRequest apiRequest;

        if (!_unloading) {
          apiRequest = HttpServiceUtil.get("/ikea-order/storage/calculate/" + _ikeaProcessOrderId);
        } else {
          apiRequest = HttpServiceUtil.get("/ikea-order/storage/unload/" + _ikeaProcessOrderId);
        }

        return apiRequest.postData(_profileIds, StorageCalculationResultDto.class);
      }
    };
  }

  public void setUploading(boolean unloading) {
    unloadingProperty.set(unloading);
  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId, List<Long> profileIds, boolean unloading) {
    ikeaProcessOrderIdProperty.set(ikeaProcessOrderId);
    profileIdsProperty.set(profileIds);
    setUploading(unloading);
  }
}
