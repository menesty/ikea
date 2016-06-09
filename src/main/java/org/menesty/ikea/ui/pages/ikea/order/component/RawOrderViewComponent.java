package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.dto.ProductPriceMismatch;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.form.ComboBoxField;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.IkeaProductUpdateService;
import org.menesty.ikea.ui.pages.ikea.order.dialog.export.IkeaSiteExportDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.product.ProductEditDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.NumberUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/30/15.
 * 18:38.
 */
public class RawOrderViewComponent extends BorderPane {
  private List<IkeaClientOrderItemDto> data;
  private BaseTableView<IkeaOrderItem> tableView;
  private StatusPanel statusPanel;
  private ProductEditDialog productEditDialog;
  private IkeaProductUpdateService ikeaProductUpdateService;

  public void setData(List<IkeaClientOrderItemDto> data) {
    this.data = data;
    updateView();
  }

  public List<IkeaClientOrderItemDto> getData() {
    return data;
  }

  private final TextField artNumberField;
  private ComboBoxField<Profile> profileComboBoxField;
  private ComboBoxField<Product.Group> productGroup;
  private CheckBox specialCheckBoxField;

  public RawOrderViewComponent(final DialogSupport dialogSupport) {
    StackPane main = new StackPane();

    VBox controlBox = new VBox();
    ToolBar actionToolBar = new ToolBar();
    {
      Button button = new Button(null, ImageFactory.createIkea32Icon());
      button.setTooltip(new Tooltip("Export CustomerOrder to IKEA"));
      button.setOnAction(event -> {
        IkeaSiteExportDialog dialog = getExportDialog(dialogSupport);
        dialog.bind(data);
        dialogSupport.showPopupDialog(dialog);

      });
      actionToolBar.getItems().add(button);
    }

    controlBox.getChildren().add(actionToolBar);
    ToolBar filterToolBar = new ToolBar();

    {
      artNumberField = new TextField();
      artNumberField.setDelay(1);
      artNumberField.setOnDelayAction(actionEvent -> filter());
      artNumberField.setPromptText("Product ID #");

      filterToolBar.getItems().add(artNumberField);
    }

    {
      profileComboBoxField = new ComboBoxField<>(null);
      profileComboBoxField.setItemLabel(item -> item.getFirstName() + " " + item.getLastName());
      profileComboBoxField.selectedItemProperty().addListener(observable -> filter());
      profileComboBoxField.setTooltip(I18n.UA.getString(I18nKeys.FILTER_BY_PROFILE));

      filterToolBar.getItems().add(profileComboBoxField);
    }

    {
      productGroup = new ComboBoxField<>(null);
      productGroup.selectedItemProperty().addListener(observable -> filter());
      productGroup.setTooltip(I18n.UA.getString(I18nKeys.FILTER_BY_GROUP));
      productGroup.setItems(Product.Group.values());

      filterToolBar.getItems().add(productGroup);
    }

    {
      Label label = new Label("Colors :");
      label.setMinHeight(24);
      specialCheckBoxField = new CheckBox();
      filterToolBar.getItems().addAll(label, specialCheckBoxField);
      specialCheckBoxField.selectedProperty().addListener(observable -> filter());
    }
    {
      Button button = new Button(null, ImageFactory.createClear16Icon());
      button.setOnAction(event -> {
        profileComboBoxField.setValue(null);
        productGroup.setValue(null);
        artNumberField.setText(null);
      });
      filterToolBar.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.CLEAR)));
      filterToolBar.getItems().add(button);
    }

    controlBox.getChildren().add(filterToolBar);

    setTop(controlBox);

    tableView = new BaseTableView<>();

    LoadingPane loadingPanel = new LoadingPane();

    ikeaProductUpdateService = new IkeaProductUpdateService();

    loadingPanel.bindTask(ikeaProductUpdateService);

    tableView.setRowDoubleClickListener(row -> showProductDialog(dialogSupport, row.getItem()));
    tableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);
      row.getStyleClass().remove("productNotVerified");

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.EDIT));
          menuItem.setOnAction(event -> showProductDialog(dialogSupport, newValue));

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);

        if (!newValue.getProduct().getProductProperties().isValidated()) {
          row.getStyleClass().add("productNotVerified");
        }
      }
    });

    {
      TableColumn<IkeaOrderItem, Number> column = new TableColumn<>();
      column.setMaxWidth(40);
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem>indexColumn());

      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(130);
      column.setCellFactory(ArtNumberCell::new);
      column.getStyleClass().add("align-right");
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, String>column("product.artNumber"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
      column.setPrefWidth(250);
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, String>column("product.shortName"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
      column.setMinWidth(60);
      column.getStyleClass().add("align-center");
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem>number("count"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setMinWidth(60);
      column.getStyleClass().add("align-right");
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem>number("price"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaOrderItem, Product.Group> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRODUCT_GROUP));
      column.setMinWidth(130);
      column.getStyleClass().add("align-right");
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Product.Group>column("product.group"));
      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaOrderItem, Product.Group> column = new TableColumn<>();
      column.setMinWidth(30);
      column.getStyleClass().add("align-right");
      column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Product.Group>column("processedByLogistic"));
      tableView.getColumns().add(column);
    }

    main.getChildren().addAll(tableView, loadingPanel);

    setCenter(main);

    statusPanel = new StatusPanel();
    setBottom(statusPanel);
  }

  public IkeaSiteExportDialog getExportDialog(DialogSupport dialogSupport) {
    IkeaSiteExportDialog exportDialog = new IkeaSiteExportDialog(dialogSupport.getStage());
    exportDialog.setDefaultAction(dialog -> {
      dialog.setDefaultAction(null);
      dialogSupport.hidePopupDialog();
    });

    return exportDialog;
  }

  private void showProductDialog(DialogSupport dialogSupport, IkeaOrderItem ikeaOrderItem) {
    ProductEditDialog productEditDialog = getProductEditDialog(dialogSupport.getStage());
    productEditDialog.bind(ikeaOrderItem.getProduct(), new EntityDialogCallback<IkeaProduct>() {
      @Override
      public void onSave(IkeaProduct ikeaProduct, Object... params) {
        ikeaOrderItem.setProduct(ikeaProduct);

        ikeaProduct.getProductProperties().setValidated(true);

        tableView.update(ikeaOrderItem);

        ikeaProductUpdateService.setIkeaProduct(ikeaProduct);
        ikeaProductUpdateService.restart();

        dialogSupport.hidePopupDialog();
      }

      @Override
      public void onCancel() {
        dialogSupport.hidePopupDialog();
      }
    });

    dialogSupport.showPopupDialog(productEditDialog);
  }

  private void filter() {
    Profile profile = profileComboBoxField.getValue();

    List<IkeaOrderItem> items;

    if (profile != null) {
      items = data.stream()
          .filter(ikeaClientOrderItemDto -> ikeaClientOrderItemDto.getProfile().equals(profile))
          .map(IkeaClientOrderItemDto::getIkeaOrderItems)
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      items = data.stream()
          .map(IkeaClientOrderItemDto::getIkeaOrderItems)
          .flatMap(Collection::stream).collect(Collectors.toList());
    }


    items = groupItem(items);

    if (productGroup.getValue() != null) {
      items = items.parallelStream()
          .filter(ikeaOrderItem -> ikeaOrderItem.getProduct().getGroup().equals(productGroup.getValue()))
          .collect(Collectors.toList());
    }

    if (specialCheckBoxField.isSelected()) {
      items = items.parallelStream()
          .filter(IkeaOrderItem::isSpecial)
          .collect(Collectors.toList());
    }

    if (artNumberField.getText() != null) {
      items = items.parallelStream()
          .filter(ikeaOrderItem -> ikeaOrderItem.getProduct().getArtNumber().startsWith(artNumberField.getText()))
          .collect(Collectors.toList());
    }

    BigDecimal total = items.stream().map(IkeaOrderItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    statusPanel.setTotal(total);

    List<ProductPriceMismatch> priceMismatches;

    if (profile != null) {
      priceMismatches = data.stream()
          .filter(ikeaClientOrderItemDto -> ikeaClientOrderItemDto.getProfile().equals(profile))
          .map(icoid -> icoid.getExtraData().getProductPriceMismatches()).flatMap(Collection::stream).collect(Collectors.toList());
    } else {
      priceMismatches = data.stream().map(icoid -> icoid.getExtraData().getProductPriceMismatches()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    BigDecimal diff = priceMismatches.stream().map(ProductPriceMismatch::getDiff).reduce(BigDecimal.ZERO, BigDecimal::add);
    statusPanel.setDiffAmount(diff);

    BigDecimal weightTotal = items.stream().map(ikeaOrderItem -> ikeaOrderItem.getCount().multiply(new BigDecimal(ikeaOrderItem.getProduct().getWeight())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    statusPanel.setWeight(weightTotal.intValue());

    tableView.getItems().clear();
    tableView.getItems().addAll(items);
  }

  public static List<IkeaOrderItem> groupItem(List<IkeaOrderItem> allItems) {
    Map<String, List<IkeaOrderItem>> map = allItems.stream().collect(Collectors.groupingBy(item -> item.getProduct().getArtNumber()));

    List<IkeaOrderItem> itemList = new ArrayList<>();

    map.values().stream().forEach(ikeaOrderItems -> {
      if (ikeaOrderItems.size() == 1) {
        itemList.add(ikeaOrderItems.get(0));
      } else if (ikeaOrderItems.size() > 1) {
        //filter special
        List<IkeaOrderItem> specials = ikeaOrderItems.stream().filter(IkeaOrderItem::isSpecial).collect(Collectors.toList());

        if (!specials.isEmpty()) {
          itemList.addAll(specials);
        }

        List<IkeaOrderItem> normal = ikeaOrderItems.stream().filter(ikeaOrderItem -> !ikeaOrderItem.isSpecial()).collect(Collectors.toList());

        if (!normal.isEmpty()) {

          IkeaOrderItem first = normal.get(0);

          IkeaOrderItem item = new IkeaOrderItem();
          item.setCount(BigDecimal.ZERO);
          item.setProduct(first.getProduct());
          item.setPrice(first.getPrice());

          normal.stream().reduce(item, (item1, item2) -> item1.addCount(item2.getCount()));

          itemList.add(item);
        }

      }
    });

    return itemList;
  }

  private void updateView() {
    List<Profile> clients = data.stream().map(IkeaClientOrderItemDto::getProfile).distinct().collect(Collectors.toList());

    profileComboBoxField.setItems(clients);
    profileComboBoxField.setValue(null);

    filter();
  }

  private ProductEditDialog getProductEditDialog(Stage stage) {
    if (productEditDialog == null) {
      productEditDialog = new ProductEditDialog(stage);
    }

    return productEditDialog;
  }

  class StatusPanel extends TotalStatusPanel {
    private Label warningLabel;
    private Label diffAmount;
    private Label diffAmountLabel;
    private Label weightLabel;
    private Label weight;

    public StatusPanel() {
      getItems().add(warningLabel = new Label());
      warningLabel.setGraphic(ImageFactory.createWarning16Icon());
      warningLabel.setVisible(false);
      Region space = new Region();
      HBox.setHgrow(space, Priority.ALWAYS);

      Region space2 = new Region();
      HBox.setHgrow(space2, Priority.ALWAYS);
      getItems().addAll(space,
          weightLabel = new Label(I18n.UA.getString(I18nKeys.WEIGHT_KG) + " : "),
          weight = new Label(),
          space2, diffAmountLabel = new Label(I18n.UA.getString(I18nKeys.DIFF_AMOUNT) + " : "), diffAmount = new Label());
      setDiffAmount(BigDecimal.ZERO);
    }

    public void setWeight(int weight) {
      if (weight != 0) {
        this.weight.setText(NumberUtil.convertToKg(weight) + "");
      } else {
        this.weight.setText("");
      }
    }

    public void setDiffAmount(BigDecimal total) {
      if (BigDecimal.ZERO.equals(total)) {
        diffAmountLabel.setVisible(false);
        diffAmount.setVisible(false);
      } else {
        diffAmountLabel.setVisible(true);
        diffAmount.setVisible(true);

        diffAmountLabel.getStyleClass().clear();
        diffAmount.getStyleClass().clear();

        if (total.compareTo(BigDecimal.ZERO) > 0) {
          diffAmount.getStyleClass().add("text-green");
          diffAmountLabel.getStyleClass().add("text-green");
        } else {
          diffAmount.getStyleClass().add("text-red");
          diffAmountLabel.getStyleClass().add("text-red");
        }

        diffAmount.setText(NumberFormat.getNumberInstance().format(NumberUtil.round(total.doubleValue())));
      }
    }
  }
}
