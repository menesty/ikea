package org.menesty.ikea.ui.controls.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.OrderItem;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.dialog.OrderItemDialog;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.search.OrderItemSearchBar;
import org.menesty.ikea.ui.controls.search.OrderItemSearchData;
import org.menesty.ikea.ui.controls.table.OrderItemTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

/**
 * User: Menesty
 * Date: 11/5/13
 * Time: 8:18 AM
 */
public abstract class OrderItemViewComponent extends BorderPane {

    private final Button exportToIkeaBtn;

    private OrderItemTableView orderItemTableView;

    private ProductDialog productEditDialog;

    private StatusPanel statusPanel;

    private OrderItemDialog orderItemDialog;

    private Button editBtn;

    public OrderItemViewComponent(final Stage stage) {
        productEditDialog = new ProductDialog();

        orderItemDialog = new OrderItemDialog();

        orderItemTableView = new OrderItemTableView() {
            @Override
            public void onRowDoubleClick(final TableRow<OrderItem> row) {
                if (OrderItem.Type.Na == row.getItem().getType() || row.getItem().isInvalidFetch())
                    return;

                showPopupDialog(productEditDialog);
                productEditDialog.bind(row.getItem().getProductInfo(), new EntityDialogCallback<ProductInfo>() {
                    @Override
                    public void onSave(ProductInfo productInfo, Object[] params) {
                        save(productInfo);

                        if (!(Boolean) params[0])
                            hidePopupDialog();

                        row.setItem(null);
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }

            public void onFetchAction(final TableRow<OrderItem> row) {
                reloadProduct(row.getItem(), new EventHandler<Event>() {
                    @Override
                    public void handle(Event event) {
                        row.setItem(null);
                    }
                });
            }
        };

        setCenter(orderItemTableView);


        ToolBar toolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    orderItemDialog.bind(new OrderItem(), new EntityDialogCallback<OrderItem>() {
                        @Override
                        public void onSave(OrderItem orderItem, Object... params) {
                            save(orderItem);
                            hidePopupDialog();
                        }

                        @Override
                        public void onCancel() {
                            hidePopupDialog();
                        }
                    });

                    showPopupDialog(orderItemDialog);
                }
            });
            toolBar.getItems().addAll(button);
        }
        {
            editBtn = new Button(null, ImageFactory.createEdit32Icon());
            editBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    orderItemDialog.bind(orderItemTableView.getSelectionModel().getSelectedItem(), new EntityDialogCallback<OrderItem>() {
                        @Override
                        public void onSave(OrderItem orderItem, Object... params) {
                            save(orderItem);
                            hidePopupDialog();
                        }

                        @Override
                        public void onCancel() {
                            hidePopupDialog();
                        }
                    });

                    showPopupDialog(orderItemDialog);
                }
            });
            editBtn.setDisable(true);
            toolBar.getItems().addAll(editBtn);
        }

        toolBar.getItems().addAll(new Separator());
        Button exportOrder = new Button(null, ImageFactory.createXls32Icon());
        exportOrder.setTooltip(new Tooltip("Export to XLS"));
        exportOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zip file (*.zip)", "*.zip");
                fileChooser.getExtensionFilters().add(extFilter);
                //Show save file dialog
                File file = fileChooser.showSaveDialog(stage);

                if (file != null)
                    onExport(file.getAbsolutePath());
            }
        });
        toolBar.getItems().add(exportOrder);

        ImageView imageView = new ImageView(new Image("/styles/images/icon/ikea-32x32.png"));
        exportToIkeaBtn = new Button("", imageView);
        exportToIkeaBtn.setContentDisplay(ContentDisplay.RIGHT);
        exportToIkeaBtn.setTooltip(new Tooltip("Export CustomerOrder to IKEA"));
        exportToIkeaBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                onExportToIkea();
            }
        });
        toolBar.getItems().add(exportToIkeaBtn);


        VBox controlBox = new VBox();
        controlBox.getChildren().add(toolBar);
        controlBox.getChildren().add(new OrderItemSearchBar() {
            @Override
            public void onSearch(OrderItemSearchData orderItemSearchForm) {
                setItems(filter(orderItemSearchForm));
            }

        });

        orderItemTableView.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                editBtn.setDisable(orderItemTableView.getSelectionModel().getSelectedItem() == null);
            }
        });
        setTop(controlBox);
        setBottom(statusPanel = new StatusPanel());

    }

    protected abstract void reloadProduct(OrderItem orderItem, EventHandler<Event> onSucceeded);

    protected abstract void save(ProductInfo productInfo);

    protected abstract void save(OrderItem orderItem);

    protected abstract void hidePopupDialog();

    protected abstract void showPopupDialog(BaseDialog productEditDialog);

    protected abstract List<OrderItem> filter(OrderItemSearchData orderItemSearchForm);

    protected abstract void onExport(String filePath);

    protected abstract void onExportToIkea();

    public void setItems(List<OrderItem> items) {
        orderItemTableView.setItems(FXCollections.observableArrayList(items));
        priceCalculate();

    }

    public void disableIkeaExport(boolean disable) {
        exportToIkeaBtn.setDisable(disable);
    }

    private void priceCalculate() {
        BigDecimal orderTotal = BigDecimal.ZERO;
        BigDecimal orderNaTotal = BigDecimal.ZERO;
        BigDecimal productTotal = BigDecimal.ZERO;

        for (OrderItem item : orderItemTableView.getItems()) {
            orderTotal = orderTotal.add(item.getTotal());

            if (OrderItem.Type.Na == item.getType())
                orderNaTotal = orderNaTotal.add(item.getTotal());
            else if (item.getProductInfo() != null)
                productTotal = productTotal.add(BigDecimal.valueOf(item.getProductInfo().getPrice()).multiply(BigDecimal.valueOf(item.getCount())).setScale(2, BigDecimal.ROUND_CEILING));

        }

        BigDecimal diff = orderTotal.subtract(orderNaTotal);

        statusPanel.setTotal(orderTotal.doubleValue());
        statusPanel.showPriceWarning(diff.compareTo(productTotal) != 0);
        statusPanel.setProductTotalPrice(productTotal.doubleValue());
    }

    public void updateItem(OrderItem orderItem) {
        if (!orderItemTableView.getItems().contains(orderItem))
            orderItemTableView.getItems().add(orderItem);

        priceCalculate();

    }

    class StatusPanel extends TotalStatusPanel {
        private Label warningLabel;
        private Label productTotalPrice;

        public StatusPanel() {
            getItems().add(warningLabel = new Label());
            warningLabel.setGraphic(new ImageView(new Image("/styles/images/icon/warning-16x16.png")));
            warningLabel.setVisible(false);
            warningLabel.setTooltip(new Tooltip("Total CustomerOrder price is different then IKEA site product"));
            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);
            getItems().addAll(space, new Label("Current Total Price"), productTotalPrice = new Label());
        }

        public void showPriceWarning(boolean show) {
            warningLabel.setVisible(show);
        }

        public void setProductTotalPrice(double total) {
            productTotalPrice.setText(NumberFormat.getNumberInstance().format(NumberUtil.round(total)));
        }
    }
}
