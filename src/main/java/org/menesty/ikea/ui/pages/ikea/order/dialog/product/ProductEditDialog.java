package org.menesty.ikea.ui.pages.ikea.order.dialog.product;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.IkeaPackageInfo;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.ikea.IkeaProductPart;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.EntityDialog;
import org.menesty.ikea.ui.controls.form.*;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.form.provider.AsyncFilterDataProvider;
import org.menesty.ikea.ui.controls.form.provider.FilterAsyncService;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.AddComboPartService;
import org.menesty.ikea.ui.pages.ikea.order.component.service.PackageInfoUpdateService;
import org.menesty.ikea.ui.pages.ikea.order.dialog.combo.OrderViewComboDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Menesty on
 * 11/28/15.
 * 02:10.
 */
public class ProductEditDialog extends EntityDialog<IkeaProduct> {
  private TabPane tabPane;
  private Tab comboPartsTab;
  private Tab productPackageInfoTab;
  private IkeaPackageInfoForm ikeaPackageInfoForm;

  private TableView<IkeaProductPart> productPartTableView;
  private LoadProductService loadProductService;
  private AddComboPartService addComboPartService;
  private PackageInfoUpdateService packageInfoUpdateService;

  public ProductEditDialog(Stage stage) {
    super(stage);
    setAllowAutoHide(false);

    tabPane = new TabPane();

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.PRODUCT_PROPERTIES));

      tab.setClosable(false);
      tab.setContent(getEntityForm());

      tabPane.getTabs().add(tab);
    }

    {
      comboPartsTab = new Tab(I18n.UA.getString(I18nKeys.COMBO_PARTS));
      comboPartsTab.setClosable(false);

      productPartTableView = new TableView<>();

      {
        TableColumn<IkeaProductPart, Number> column = new TableColumn<>();
        column.setMaxWidth(40);
        column.setCellValueFactory(ColumnUtil.<IkeaProductPart>indexColumn());

        productPartTableView.getColumns().add(column);
      }

      {
        TableColumn<IkeaProductPart, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
        column.setMinWidth(130);
        column.setCellFactory(ArtNumberCell::new);
        column.getStyleClass().add("align-right");
        column.setCellValueFactory(ColumnUtil.<IkeaProductPart, String>column("product.artNumber"));

        productPartTableView.getColumns().add(column);
      }

      {
        TableColumn<IkeaProductPart, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
        column.setMinWidth(60);
        column.getStyleClass().add("align-center");
        column.setCellValueFactory(ColumnUtil.<IkeaProductPart>number("count"));
        productPartTableView.getColumns().add(column);
      }

      HBox hBox = new HBox(4);

      ComboBoxField<IkeaProduct> artNumberSearchComboField = new ComboBoxField<>(null);

      artNumberSearchComboField.setEditable(true);
      artNumberSearchComboField.setAllowBlank(false);
      artNumberSearchComboField.setItemLabel(IkeaProduct::getArtNumber);
      artNumberSearchComboField.setLoader(new AsyncFilterDataProvider<>(new FilterAsyncService<List<IkeaProduct>>() {
        @Override
        public Task<List<IkeaProduct>> createTask(String queryString) {
          return new Task<List<IkeaProduct>>() {
            @Override
            protected List<IkeaProduct> call() throws Exception {
              Map<String, String> map = new HashMap<>();
              map.put("queryString", queryString);

              APIRequest apiRequest = HttpServiceUtil.get("/product/search", map);
              return apiRequest.getList(new TypeReference<List<IkeaProduct>>() {
              });
            }
          };
        }
      }), 7);

      NumberTextField partCount = new NumberTextField(null, false);
      partCount.setMinValue(BigDecimal.ONE);
      partCount.setAllowDouble(false);
      Button addButton;

      hBox.getChildren().addAll(artNumberSearchComboField, partCount, addButton = new Button(null, ImageFactory.createAdd16Icon()));

      addButton.setOnAction(event -> {
        if (artNumberSearchComboField.isValid() | partCount.isValid()) {
          addComboPartService.setData(new OrderViewComboDialog.ComboSelectResult(entityValue.getId(), artNumberSearchComboField.getValue().getArtNumber(), partCount.getNumber().intValue()));
          addComboPartService.restart();
        }
      });

      VBox vBox = new VBox(4);
      vBox.getChildren().addAll(productPartTableView, hBox);


      loadProductService = new LoadProductService();


      addComboPartService = new AddComboPartService();
      addComboPartService.setOnSucceededListener(value -> {
        artNumberSearchComboField.setValue(null);
        partCount.setNumber(null);
        productPartTableView.getItems().setAll(value);
      });

      packageInfoUpdateService = new PackageInfoUpdateService();

      loadingPane.bindTask(loadProductService, addComboPartService, packageInfoUpdateService);

      comboPartsTab.setContent(vBox);
    }

    {
      productPackageInfoTab = new Tab(I18n.UA.getString(I18nKeys.PACKAGE_INFO));
      productPackageInfoTab.setClosable(false);
      ikeaPackageInfoForm = new IkeaPackageInfoForm();

      productPackageInfoTab.setContent(ikeaPackageInfoForm);
    }

    addRow(tabPane, bottomBar);
  }

  @Override
  public void bind(IkeaProduct entityValue, EntityDialogCallback<IkeaProduct> callback) {
    setCallback(callback);
    ikeaPackageInfoForm.setPackageInfos(Collections.emptyList());
    loadProductService.setProductId(entityValue.getId());
    loadProductService.setOnSucceededListener(value ->
    {
      super.bind(value, callback);
      ikeaPackageInfoForm.setPackageInfos(value.getIkeaPackageInfos());
      ikeaPackageInfoForm.setDisable(value.getIkeaPackageInfos().isEmpty());
    });
    loadProductService.restart();
  }

  @Override
  protected EntityForm<IkeaProduct> createForm() {
    return new IkeaProductForm();
  }

  class IkeaPackageInfoForm extends EntityForm<IkeaPackageInfo> {
    private LabelField boxCountField;
    private LabelField boxNumberField;

    private NumberTextField lengthField;
    private NumberTextField heightField;
    private NumberTextField wightField;
    private NumberTextField weightField;
    private CheckBox deletedField;
    private ComboBoxField<IkeaPackageInfo> ikeaPackageInfoComboBoxField;

    public IkeaPackageInfoForm() {
      setPadding(new Insets(3, 0, 15, 0));
      add(ikeaPackageInfoComboBoxField = new ComboBoxField<IkeaPackageInfo>(I18n.UA.getString(I18nKeys.PACKAGE_INFO_LIST)) {
        @Override
        public void reset() {
          //skip
        }
      });
      ikeaPackageInfoComboBoxField.setItemLabel(item -> item.getBoxNumber() + "");
      ikeaPackageInfoComboBoxField.addSelectItemListener((observable, oldValue, newValue) -> {
        if (newValue != null) {
          reset();
          populate(newValue);
        }
      });

      add(boxCountField = new LabelField(I18n.UA.getString(I18nKeys.BOX_COUNT)));
      add(boxNumberField = new LabelField(I18n.UA.getString(I18nKeys.BOX_NUMBER)));

      add(lengthField = new NumberTextField(I18n.UA.getString(I18nKeys.LENGTH_CM), false));
      add(heightField = new NumberTextField(I18n.UA.getString(I18nKeys.HEIGHT_CM), false));
      add(wightField = new NumberTextField(I18n.UA.getString(I18nKeys.WIGHT_CM), false));

      lengthField.setAllowDouble(false);
      heightField.setAllowDouble(false);
      wightField.setAllowDouble(false);

      add(weightField = new NumberTextField(I18n.UA.getString(I18nKeys.WEIGHT_KG), false));

      weightField.setAllowDouble(true);

      add(new WrapField<CheckBox>(I18n.UA.getString(I18nKeys.NOT_ACTIVE), deletedField = new CheckBox()) {

        @Override
        public boolean isValid() {
          List<IkeaPackageInfo> items = new ArrayList<>(ikeaPackageInfoComboBoxField.getItems());
          items.remove(ikeaPackageInfoComboBoxField.getValue());

          Optional<IkeaPackageInfo> deleted = items.stream().filter(ikeaPackageInfo -> !ikeaPackageInfo.isDeleted()).findFirst();

          return deleted.isPresent() || !deletedField.isSelected();
        }

        @Override
        public void reset() {

        }
      });

      Button save = new Button(I18n.UA.getString(I18nKeys.UPDATE_PACKAGE_INFO), ImageFactory.createEdit16Icon());
      save.setOnAction(event -> {
        if (isValid()) {
          IkeaPackageInfo packageInfo = collect(ikeaPackageInfoComboBoxField.getValue());

          packageInfoUpdateService.setPackageInfo(packageInfo);
          packageInfoUpdateService.restart();
        }
      });
      addRow(save, 2);
    }

    public void setPackageInfos(List<IkeaPackageInfo> packageInfos) {
      ikeaPackageInfoComboBoxField.setItems(packageInfos);

      if (!packageInfos.isEmpty()) {
        populate(packageInfos.get(0));
        ikeaPackageInfoComboBoxField.setValue(packageInfos.get(0));
      }
    }

    @Override
    protected IkeaPackageInfo collect(IkeaPackageInfo entity) {
      entity.setWidth(wightField.getNumber().intValue());
      entity.setLength(lengthField.getNumber().intValue());
      entity.setHeight(heightField.getNumber().intValue());
      entity.setWeight(weightField.getNumber().multiply(new BigDecimal(1000)).intValue());
      entity.setDeleted(deletedField.isSelected());

      return entity;
    }

    @Override
    protected void populate(IkeaPackageInfo entity) {
      boxCountField.setText(entity.getBoxCount() + "");
      boxNumberField.setText(entity.getBoxNumber() + "");
      lengthField.setNumber(BigDecimal.valueOf(entity.getLength()));
      heightField.setNumber(BigDecimal.valueOf(entity.getHeight()));
      wightField.setNumber(BigDecimal.valueOf(entity.getWidth()));

      if (entity.getWidth() != 0) {
        weightField.setNumber(BigDecimal.valueOf(entity.getWidth()).divide(BigDecimal.valueOf(1000), 3, BigDecimal.ROUND_HALF_UP));
      } else {
        weightField.setNumber(BigDecimal.ZERO);
      }

      deletedField.setSelected(entity.isDeleted());
    }
  }

  class IkeaProductForm extends EntityForm<IkeaProduct> {
    private TextField artNumberField;
    private NumberTextField priceField;
    private TextField uaNameField;
    private TextField shortNameField;
    private ComboBoxField<Product.Group> productGroupField;

    public IkeaProductForm() {
      add(artNumberField = new TextField(null, I18n.UA.getString(I18nKeys.ART_NUMBER)));
      artNumberField.setEditable(false);

      add(uaNameField = new TextField(null, I18n.UA.getString(I18nKeys.UA_NAME), false));
      add(shortNameField = new TextField(null, I18n.UA.getString(I18nKeys.SHORT_NAME), false));
      add(priceField = new NumberTextField(null, I18n.UA.getString(I18nKeys.PRICE), false));

      add(productGroupField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.GROUP_NAME)));
      productGroupField.setAllowBlank(false);

      List<Product.Group> groups = new ArrayList<>(Arrays.asList(Product.Group.values()));

      groups.remove(Product.Group.Combo);
      groups.remove(Product.Group.Unknown);

      productGroupField.setItems(groups);
      setPadding(new Insets(5, 0, 10, 0));

    }

    @Override
    protected IkeaProduct collect(IkeaProduct entity) {
      entity.setPrice(priceField.getNumber());
      entity.setShortName(shortNameField.getText());
      entity.setUaName(uaNameField.getText());

      if (!entity.getGroup().equals(Product.Group.Combo)) {
        entity.setGroup(productGroupField.getValue());
      }

      return entity;
    }

    @Override
    protected void populate(IkeaProduct entity) {
      tabPane.getSelectionModel().select(0);

      artNumberField.setText(entity.getArtNumber());
      uaNameField.setText(entity.getUaName());
      shortNameField.setText(entity.getShortName());
      priceField.setNumber(entity.getPrice());

      if (entity.getGroup().equals(Product.Group.Combo)) {
        productGroupField.setDisable(true);
        productGroupField.setValue(Product.Group.Combo);
        tabPane.getTabs().add(comboPartsTab);
        tabPane.getTabs().remove(productPackageInfoTab);

        productPartTableView.getItems().setAll(entity.getIkeaProductParts());
      } else {
        productGroupField.setDisable(false);
        productGroupField.setValue(entity.getGroup());
        tabPane.getTabs().remove(comboPartsTab);
        tabPane.getTabs().add(productPackageInfoTab);

      }
    }
  }

  class LoadProductService extends AbstractAsyncService<IkeaProduct> {
    private LongProperty productIdProperty = new SimpleLongProperty();

    @Override
    protected Task<IkeaProduct> createTask() {
      final Long _productId = productIdProperty.get();
      return new Task<IkeaProduct>() {
        @Override
        protected IkeaProduct call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/product/" + _productId);
          return request.getData(IkeaProduct.class);
        }
      };
    }

    public void setProductId(Long productId) {
      this.productIdProperty.setValue(productId);
    }
  }
}
