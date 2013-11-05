package org.menesty.ikea.ui.controls;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.search.OrderItemSearchBar;
import org.menesty.ikea.ui.controls.search.OrderItemSearchForm;
import org.menesty.ikea.ui.controls.table.OrderItemTableView;
import org.menesty.ikea.ui.pages.DialogCallback;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public OrderItemViewComponent(final Stage stage) {
        productEditDialog = new ProductDialog();

        orderItemTableView = new OrderItemTableView() {
            @Override
            public void onRowDoubleClick(final TableRow<OrderItem> row) {
                if (OrderItem.Type.Na == row.getItem().getType())
                    return;

                showPopupDialog(productEditDialog);
                productEditDialog.bind(row.getItem().getProductInfo(), new DialogCallback<ProductInfo>() {
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
        };

        setCenter(orderItemTableView);


        ToolBar toolBar = new ToolBar();
        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/export-32x32.png"));
        Button exportOrder = new Button("", imageView);
        exportOrder.setContentDisplay(ContentDisplay.RIGHT);
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

        imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/export-32x32.png"));
        exportToIkeaBtn = new Button("", imageView);
        exportToIkeaBtn.setContentDisplay(ContentDisplay.RIGHT);
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
            public void onSearch(OrderItemSearchForm orderItemSearchForm) {
                setItems(filter(orderItemSearchForm));
            }

        });

        setTop(controlBox);
        setBottom(statusPanel = new StatusPanel());

    }

    public abstract void save(ProductInfo productInfo);

    public abstract void hidePopupDialog();

    public abstract void showPopupDialog(ProductDialog productEditDialog);

    public abstract List<OrderItem> filter(OrderItemSearchForm orderItemSearchForm);

    public abstract void onExport(String filePath);

    public abstract void onExportToIkea();

    public void setItems(List<OrderItem> items) {
        orderItemTableView.setItems(FXCollections.observableArrayList(items));

        double total = 0d;
        for (OrderItem item : items)
            total += item.getTotal();

        statusPanel.setTotal(total);
    }

    public void disableIkeaExport(boolean disable) {
        exportToIkeaBtn.setDisable(disable);
    }

    class StatusPanel extends ToolBar {
        private Label totalLabel;

        public StatusPanel() {
            getItems().add(new Label("Total :"));
            getItems().add(totalLabel = new Label());

        }

        public void setTotal(double total) {
            totalLabel.setText(BigDecimal.valueOf(total).setScale(2, RoundingMode.CEILING).toString());
        }
    }
}
