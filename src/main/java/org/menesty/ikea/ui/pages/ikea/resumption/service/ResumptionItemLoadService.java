package org.menesty.ikea.ui.pages.ikea.resumption.service;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.ResumptionItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 3/10/16.
 * 08:24.
 */
public class ResumptionItemLoadService extends AbstractAsyncService<List<ResumptionItem>> {
  private LongProperty resumptionIdProperty = new SimpleLongProperty();

  @Override
  protected Task<List<ResumptionItem>> createTask() {
    final Long _resumptionId = resumptionIdProperty.get();

    return new Task<List<ResumptionItem>>() {
      @Override
      protected List<ResumptionItem> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/resumption/" + _resumptionId + "/items");

        return request.getData(new TypeReference<List<ResumptionItem>>() {
        });
      }
    };
  }

  public void setResumptionId(Long resumptionId) {
    resumptionIdProperty.setValue(resumptionId);
  }
}
