package org.menesty.ikea.ui.pages.ikea.order.component;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.dto.ProductPriceMismatch;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.form.ComboBoxField;
import org.menesty.ikea.ui.pages.ikea.order.dialog.export.IkeaSiteExportDialog;
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
    private TableView<IkeaOrderItem> tableView;
    private StatusPanel statusPanel;

    public void setData(List<IkeaClientOrderItemDto> data) {
        this.data = data;
        updateView();
    }

    public List<IkeaClientOrderItemDto> getData() {
        return data;
    }

    private ComboBoxField<Profile> profileComboBoxField;

    public RawOrderViewComponent(final DialogSupport dialogSupport) {
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
            profileComboBoxField = new ComboBoxField<>(null);
            profileComboBoxField.setItemLabel(item -> item.getFirstName() + " " + item.getLastName());
            profileComboBoxField.selectedItemProperty().addListener(observable -> filter());
            profileComboBoxField.setTooltip(I18n.UA.getString(I18nKeys.FILTER_BY));

            filterToolBar.getItems().add(profileComboBoxField);
        }
        {
            Button button = new Button(null, ImageFactory.createClear16Icon());
            button.setOnAction(event -> profileComboBoxField.setValue(null));
            filterToolBar.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.CLEAR)));
            filterToolBar.getItems().add(button);
        }

        controlBox.getChildren().add(filterToolBar);

        setTop(controlBox);

        tableView = new TableView<>();

        {
            TableColumn<IkeaOrderItem, Number> column = new TableColumn<>();
            column.setMaxWidth(40);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem>indexColumn());

            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setMinWidth(130);
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
            column.setMinWidth(120);
            column.getStyleClass().add("align-right");
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Product.Group>column("product.group"));
            tableView.getColumns().add(column);
        }

        setCenter(tableView);

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
                IkeaOrderItem first = ikeaOrderItems.get(0);

                IkeaOrderItem item = new IkeaOrderItem();
                item.setCount(BigDecimal.ZERO);
                item.setProduct(first.getProduct());
                item.setPrice(first.getPrice());

                ikeaOrderItems.stream().reduce(item, (item1, item2) -> item1.addCount(item2.getCount()));

                itemList.add(item);
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

    class StatusPanel extends TotalStatusPanel {
        private Label warningLabel;
        private Label diffAmount;
        private Label diffAmountLabel;

        public StatusPanel() {
            getItems().add(warningLabel = new Label());
            warningLabel.setGraphic(ImageFactory.createWarning16Icon());
            warningLabel.setVisible(false);
            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);
            getItems().addAll(space, diffAmountLabel = new Label(I18n.UA.getString(I18nKeys.DIFF_AMOUNT) + " : "), diffAmount = new Label());
            setDiffAmount(BigDecimal.ZERO);
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
