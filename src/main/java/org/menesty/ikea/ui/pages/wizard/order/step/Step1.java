package org.menesty.ikea.ui.pages.wizard.order.step;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.lib.dto.SiteOrderShortInfoDto;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.*;
import org.menesty.ikea.ui.controls.form.provider.AsyncFilterDataProvider;
import org.menesty.ikea.ui.controls.form.provider.FilterAsyncService;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/7/15.
 * 09:59.
 */
public class Step1 extends BaseWizardStep<DesktopOrderInfo> {

  private FileListField fileListField;
  private ComboBoxField<DesktopOrderInfo.SourceType> sourceTypeComboBoxField;
  private ComboBoxField<DesktopOrderInfo.OrderType> orderTypeComboBoxField;
  private ComboBoxField<Profile> clientComboBoxField;
  private ComboBoxField<IkeaProcessOrder> ikeaProcessOrderComboBoxField;
  private TextField orderNameField;
  private DoubleTextField marginField;
  private DoubleTextField sellMarginField;

  private SimpleListViewField<SiteOrderShortInfoDto> siteListField;

  private FormPane leftForm;


  public Step1(Stage stage) {
    SiteReadForProcessLoadService loadService = new SiteReadForProcessLoadService();
    leftForm = new FormPane();
    leftForm.setLabelWidth(130);

    sourceTypeComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.SOURCE_TYPE));
    leftForm.add(sourceTypeComboBoxField);
    sourceTypeComboBoxField.setAllowBlank(false);

    siteListField = new SimpleListViewField<>(I18n.UA.getString(I18nKeys.SITE_ORDERS), true);
    siteListField.setLabel((item) -> item.getClientName() + " : " + item.getAmount() + " PLN");
    siteListField.setMaxHeight(100);

    leftForm.add(siteListField);
    leftForm.setVisible(siteListField, false);

    sourceTypeComboBoxField.addSelectItemListener((observable, oldValue, newValue) -> {
      DesktopOrderInfo.SourceType sourceType = sourceTypeComboBoxField.getValue();

      if (sourceType == null || sourceType.getFileSourceType() == null) {
        leftForm.setVisible(fileListField, false);
        fileListField.setAllowBlank(true);

        if (DesktopOrderInfo.SourceType.SITE == sourceType) {
          leftForm.setVisible(siteListField, true);
          siteListField.setAllowBlank(false);


          leftForm.setVisible(clientComboBoxField, false);
          clientComboBoxField.setAllowBlank(true);

          loadService.restart();
        } else {
          leftForm.setVisible(siteListField, false);
          siteListField.setAllowBlank(true);


        }
      } else {
        leftForm.setVisible(fileListField, true);
        fileListField.setFileType(sourceType.getFileSourceType());
        fileListField.setAllowBlank(false);

        leftForm.setVisible(siteListField, false);
        leftForm.setVisible(clientComboBoxField, true);
        clientComboBoxField.setAllowBlank(false);

        siteListField.setAllowBlank(true);
      }
    });
    sourceTypeComboBoxField.setItems(DesktopOrderInfo.SourceType.values());

    fileListField = new FileListField(I18n.UA.getString(I18nKeys.FILES), stage);
    fileListField.setMaxHeight(100);
    fileListField.setAllowBlank(true);

    leftForm.add(fileListField);
    leftForm.setVisible(fileListField, false);

    clientComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.CLIENT));
    clientComboBoxField.setAllowBlank(false);
    clientComboBoxField.setEditable(true);
    clientComboBoxField.setItemLabel(item -> item.getFirstName().concat(" ").concat(item.getLastName()));
    clientComboBoxField.setLoader(new AsyncFilterDataProvider<>(new FilterAsyncService<List<Profile>>() {
      @Override
      public Task<List<Profile>> createTask(String queryString) {
        return new Task<List<Profile>>() {
          @Override
          protected List<Profile> call() throws Exception {
            Map<String, String> map = new HashMap<>();
            map.put("queryString", queryString);

            APIRequest apiRequest = HttpServiceUtil.get("/profiles/DESKTOP/", map);

            return apiRequest.getList(new TypeReference<List<Profile>>() {
            });
          }
        };
      }
    }));
    leftForm.add(clientComboBoxField);


    leftForm.add(orderTypeComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.ORDER_TYPE)));

    orderTypeComboBoxField.setAllowBlank(false);
    orderTypeComboBoxField.setItems(DesktopOrderInfo.OrderType.values());
    orderTypeComboBoxField.addSelectItemListener((observable, oldValue, newValue) -> {
      DesktopOrderInfo.OrderType orderType = orderTypeComboBoxField.getValue();

      if (DesktopOrderInfo.OrderType.EXISTED.equals(orderType)) {
        leftForm.setVisible(orderNameField, false);
        leftForm.setVisible(sellMarginField, false);

        leftForm.setVisible(ikeaProcessOrderComboBoxField, true);
        ikeaProcessOrderComboBoxField.setAllowBlank(false);
        orderNameField.setAllowBlank(true);
        sellMarginField.setAllowBlank(true);

      } else if (DesktopOrderInfo.OrderType.NEW.equals(orderType)) {
        leftForm.setVisible(orderNameField, true);
        leftForm.setVisible(sellMarginField, true);

        leftForm.setVisible(ikeaProcessOrderComboBoxField, false);


        ikeaProcessOrderComboBoxField.setAllowBlank(true);
        orderNameField.setAllowBlank(false);

        sellMarginField.setAllowBlank(false);

      } else {
        leftForm.setVisible(orderNameField, false);
        leftForm.setVisible(ikeaProcessOrderComboBoxField, false);
        leftForm.setVisible(sellMarginField, false);

        orderNameField.setAllowBlank(true);
        ikeaProcessOrderComboBoxField.setAllowBlank(true);
        sellMarginField.setAllowBlank(true);
      }
    });

    ikeaProcessOrderComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.ORDER_NAME));
    ikeaProcessOrderComboBoxField.setAllowBlank(true);

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
    leftForm.setVisible(ikeaProcessOrderComboBoxField, false);

    leftForm.add(orderNameField = new TextField(null, I18n.UA.getString(I18nKeys.ORDER_NAME)));
    leftForm.setVisible(orderNameField, false);

    leftForm.add(marginField = new DoubleTextField(I18n.UA.getString(I18nKeys.MARGIN_ORDER)));
    marginField.setAllowBlank(false);

    leftForm.add(sellMarginField = new DoubleTextField(I18n.UA.getString(I18nKeys.MARGIN_SELL)));
    sellMarginField.setAllowBlank(true);
    leftForm.setVisible(sellMarginField, false);

    /******************RIGHT FORM******************/
    StackPane main = new StackPane();
    LoadingPane loadingPane = new LoadingPane();


    loadService.setOnSucceededListener(siteListField::setItems);
    loadingPane.bindTask(loadService);

    main.getChildren().addAll(leftForm, loadingPane);

    setContent(main);
  }

  @Override
  public boolean isValid() {
    return leftForm.isValid();
  }

  @Override
  public boolean canSkip(DesktopOrderInfo param) {
    return false;
  }

  @Override
  public void collect(DesktopOrderInfo param) {
    param.setFiles(fileListField.getValues());
    param.setSourceType(sourceTypeComboBoxField.getValue());
    param.setClient(clientComboBoxField.getValue());
    param.setOrderMargin(BigDecimal.valueOf(marginField.getNumber()));
    param.setOrderType(orderTypeComboBoxField.getValue());
    param.setOrderName(orderNameField.getText());
    param.setSellMargin(BigDecimal.valueOf(sellMarginField.getNumber()));
    param.setIkeaProcessOrder(ikeaProcessOrderComboBoxField.getValue());

    if (DesktopOrderInfo.SourceType.SITE == param.getSourceType()) {
      param.setSiteOrderIds(siteListField.getSelected().stream().map(SiteOrderShortInfoDto::getId).collect(Collectors.toList()));
    } else {
      param.setSiteOrderIds(Collections.emptyList());
    }
  }

  @Override
  public void onActive(DesktopOrderInfo param) {
    fileListField.setValues(param.getFiles());
    sourceTypeComboBoxField.setValue(param.getSourceType());
    clientComboBoxField.setValue(param.getClient());
    marginField.setNumber(param.getOrderMargin().doubleValue());
    orderTypeComboBoxField.setValue(param.getOrderType());
    orderNameField.setText(param.getOrderName());
    sellMarginField.setNumber(param.getSellMargin().doubleValue());
  }
}

class SiteReadForProcessLoadService extends AbstractAsyncService<List<SiteOrderShortInfoDto>> {

  @Override
  protected Task<List<SiteOrderShortInfoDto>> createTask() {
    return new Task<List<SiteOrderShortInfoDto>>() {
      @Override
      protected List<SiteOrderShortInfoDto> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/site-order/ready");
        return request.getData(new TypeReference<List<SiteOrderShortInfoDto>>() {
        });
      }
    };
  }
}
