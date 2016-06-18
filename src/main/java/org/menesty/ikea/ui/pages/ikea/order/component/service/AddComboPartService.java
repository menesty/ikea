package org.menesty.ikea.ui.pages.ikea.order.component.service;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.IkeaProductPart;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.pages.ikea.order.dialog.combo.OrderViewComboDialog;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 11/28/15.
 * 03:48.
 */
public class AddComboPartService extends AbstractAsyncService<List<IkeaProductPart>> {
  private LongProperty comboIdProperty = new SimpleLongProperty();
  private StringProperty partArtNumberProperty = new SimpleStringProperty();
  private IntegerProperty countProperty = new SimpleIntegerProperty();

  @Override
  protected Task<List<IkeaProductPart>> createTask() {
    final Long _comboId = comboIdProperty.get();
    final String _partArtNumber = partArtNumberProperty.get();
    final int _count = countProperty.get();

    return new Task<List<IkeaProductPart>>() {
      @Override
      protected List<IkeaProductPart> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/combo/" + _comboId + "/add-part/" + _partArtNumber + "/" + _count);
        return request.getList(new TypeReference<List<IkeaProductPart>>() {
        });
      }
    };
  }

  public void setData(OrderViewComboDialog.ComboSelectResult comboSelectResult) {
    comboIdProperty.setValue(comboSelectResult.getComboId());
    partArtNumberProperty.setValue(comboSelectResult.getPartArtNumber());
    countProperty.setValue(comboSelectResult.getCount());
  }
}
