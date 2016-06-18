package org.menesty.ikea.ui.pages.ikea.reports.order.step;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.FileSourceType;
import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.ui.controls.form.ComboBoxField;
import org.menesty.ikea.ui.controls.form.FileListField;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.provider.AsyncFilterDataProvider;
import org.menesty.ikea.ui.controls.form.provider.FilterAsyncService;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.ikea.reports.order.OrderReportInfo;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Menesty on
 * 9/7/15.
 * 09:59.
 */
public class Step1 extends BaseWizardStep<OrderReportInfo> {

  private FileListField fileListField;
  private ComboBoxField<IkeaProcessOrder> ikeaProcessOrderComboBoxField;

  private FormPane leftForm;

  public Step1(Stage stage) {
    leftForm = new FormPane();
    leftForm.setLabelWidth(120);

    fileListField = new FileListField(I18n.UA.getString(I18nKeys.FILES), stage);
    fileListField.setMaxHeight(150);
    fileListField.setFileType(FileSourceType.PDF);
    fileListField.setAllowBlank(false);

    leftForm.add(fileListField);

    ikeaProcessOrderComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.ORDER_NAME));
    ikeaProcessOrderComboBoxField.setAllowBlank(false);

    ikeaProcessOrderComboBoxField.setEditable(true);
    ikeaProcessOrderComboBoxField.setItemLabel(IkeaProcessOrder::getName);
    ikeaProcessOrderComboBoxField.setLoader(new AsyncFilterDataProvider<>(new FilterAsyncService<List<IkeaProcessOrder>>() {
      @Override
      public Task<List<IkeaProcessOrder>> createTask(String queryString) {
        return new Task<List<IkeaProcessOrder>>() {
          @Override
          protected List<IkeaProcessOrder> call() throws Exception {
            Map<String, String> map = new HashMap<>();
            map.put("queryString", queryString);

            APIRequest apiRequest = HttpServiceUtil.get("/ikea-process-order/", map);

            return apiRequest.getList(new TypeReference<List<IkeaProcessOrder>>() {
            });
          }
        };
      }
    }));
    leftForm.add(ikeaProcessOrderComboBoxField);


    setContent(leftForm);
  }

  @Override
  public boolean isValid() {
    return leftForm.isValid();
  }

  @Override
  public boolean canSkip(OrderReportInfo param) {
    return false;
  }

  @Override
  public void collect(OrderReportInfo param) {
    param.setFiles(fileListField.getValues());
    param.setIkeaProcessOrder(ikeaProcessOrderComboBoxField.getValue());
  }

  @Override
  public void onActive(OrderReportInfo param) {
    fileListField.setValues(param.getFiles());
    ikeaProcessOrderComboBoxField.setValue(param.getIkeaProcessOrder());
  }
}
