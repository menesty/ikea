package org.menesty.ikea.ui.pages.wizard.order.step;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.IkeaShop;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.*;
import org.menesty.ikea.ui.controls.form.provider.AsyncFilterDataProvider;
import org.menesty.ikea.ui.controls.form.provider.CachedAsyncDataProvider;
import org.menesty.ikea.ui.controls.form.provider.DataProvider;
import org.menesty.ikea.ui.controls.form.provider.FilterAsyncService;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TextField orderNameField;
    private DoubleTextField marginField;
    private DoubleTextField sellMarginField;


    private ListViewField usersField;
    private ListEditField<IkeaShop, IkeaShop> ikeaShopView;

    private FormPane leftForm;
    private FormPane rightForm;

    private CachedAsyncDataProvider<IkeaShop> ikeaListCachedAsyncDataProvider;

    public Step1(Stage stage) {
        HBox formPane = new HBox();


        leftForm = new FormPane();
        leftForm.setLabelWidth(120);

        sourceTypeComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.SOURCE_TYPE));
        leftForm.add(sourceTypeComboBoxField);
        sourceTypeComboBoxField.setAllowBlank(false);

        sourceTypeComboBoxField.addSelectItemListener((observable, oldValue, newValue) -> {
            DesktopOrderInfo.SourceType sourceType = sourceTypeComboBoxField.getValue();

            if (sourceType == null || sourceType.getFileSourceType() == null) {
                leftForm.setVisible(fileListField, false);
                fileListField.setAllowBlank(true);
            } else {
                leftForm.setVisible(fileListField, true);
                fileListField.setFileType(sourceType.getFileSourceType());
                fileListField.setAllowBlank(false);
            }
        });
        sourceTypeComboBoxField.setItems(DesktopOrderInfo.SourceType.values());

        fileListField = new FileListField(I18n.UA.getString(I18nKeys.FILES), stage);
        fileListField.setMaxHeight(100);
        fileListField.setAllowBlank(true);

        leftForm.add(fileListField);
        leftForm.setVisible(fileListField, false);

        clientComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.CLIENT));
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

                        APIRequest apiRequest = HttpServiceUtil.get("/profiles/WEB/", map);

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
                orderNameField.setAllowBlank(true);

            } else if (DesktopOrderInfo.OrderType.NEW.equals(orderType)) {
                leftForm.setVisible(orderNameField, true);
                orderNameField.setAllowBlank(false);
            } else {
                leftForm.setVisible(orderNameField, false);
                orderNameField.setAllowBlank(true);
            }
        });

        leftForm.add(orderNameField = new TextField(null, I18n.UA.getString(I18nKeys.ORDER_NAME)));
        leftForm.setVisible(orderNameField, false);

        leftForm.add(marginField = new DoubleTextField(I18n.UA.getString(I18nKeys.MARGIN_ORDER)));
        marginField.setAllowBlank(false);

        leftForm.add(sellMarginField = new DoubleTextField(I18n.UA.getString(I18nKeys.MARGIN_SELL)));
        sellMarginField.setAllowBlank(false);

        /******************RIGHT FORM******************/
        rightForm = new FormPane();
        rightForm.setLabelWidth(100);

        rightForm.add(usersField = new ListViewField(I18n.UA.getString(I18nKeys.USERS), true));
        rightForm.add(ikeaShopView = new ListEditField<>(I18n.UA.getString(I18nKeys.SHOPS), true));
        ikeaShopView.setItemLabel(IkeaShop::getName);

        ikeaListCachedAsyncDataProvider = new CachedAsyncDataProvider<>(new AbstractAsyncService<List<IkeaShop>>() {
            @Override
            protected Task<List<IkeaShop>> createTask() {
                return new Task<List<IkeaShop>>() {
                    @Override
                    protected List<IkeaShop> call() throws Exception {
                        APIRequest apiRequest = HttpServiceUtil.get("/ikea-shops");

                        return apiRequest.getList(new TypeReference<List<IkeaShop>>() {
                        });
                    }
                };
            }
        });
        ikeaShopView.getLoadingPane().bindTask(ikeaListCachedAsyncDataProvider.getService());
        ikeaListCachedAsyncDataProvider.getData(new DataProvider.CallBack<IkeaShop>() {
            @Override
            public void onData(List<IkeaShop> data) {
                ikeaShopView.setChoiceList(data);
            }

            @Override
            public void onError() {

            }
        });

        HBox.setHgrow(leftForm, Priority.ALWAYS);
        HBox.setHgrow(rightForm, Priority.ALWAYS);

        formPane.getChildren().addAll(leftForm, rightForm);

        setContent(formPane);
    }

    @Override
    public boolean isValid() {
        return leftForm.isValid() & rightForm.isValid();
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
        param.setUsers(usersField.getValues());
        param.setIkeaShops(ikeaShopView.getValues());
        param.setSellMargin(BigDecimal.valueOf(sellMarginField.getNumber()));
    }

    @Override
    public void onActive(DesktopOrderInfo param) {
        fileListField.setValues(param.getFiles());
        sourceTypeComboBoxField.setValue(param.getSourceType());
        clientComboBoxField.setValue(param.getClient());
        marginField.setNumber(param.getOrderMargin().doubleValue());
        orderTypeComboBoxField.setValue(param.getOrderType());
        orderNameField.setText(param.getOrderName());
        usersField.setValue(param.getUsers());
        ikeaShopView.setValue(param.getIkeaShops());
        sellMarginField.setNumber(param.getSellMargin().doubleValue());
    }
}
