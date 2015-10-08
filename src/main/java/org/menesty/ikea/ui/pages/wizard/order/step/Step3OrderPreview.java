package org.menesty.ikea.ui.pages.wizard.order.step;

import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.dto.OrderItemDetails;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.wizard.order.step.dialog.AddIkeaProductDialog;
import org.menesty.ikea.util.ColumnUtil;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Created by Menesty on
 * 9/10/15.
 * 18:34.
 */
public class Step3OrderPreview extends BaseWizardStep<DesktopOrderInfo> {
    private BaseTableView<IkeaOrderItem> tableView;
    private AddIkeaProductDialog addIkeaProductDialog;
    private OrderItemDetails orderItemDetails;
    private TotalStatusPanel totalStatusPanel;

    public Step3OrderPreview(DialogSupport dialogSupport) {
        addIkeaProductDialog = new AddIkeaProductDialog(dialogSupport.getStage());
        BorderPane mainPane = new BorderPane();

        tableView = new BaseTableView<>();

        {
            TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setMinWidth(170);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, String>column("product.artNumber"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaOrderItem, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Number>column("count"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaOrderItem, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Number>column("price"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaOrderItem, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
            column.setMinWidth(120);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Number>column("amount"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaOrderItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
            column.setMinWidth(200);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, String>column("product.shortName"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaOrderItem, Product.Group> column = new TableColumn<>(I18n.UA.getString(I18nKeys.GROUP_NAME));
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<IkeaOrderItem, Product.Group>column("product.group"));
            tableView.getColumns().add(column);
        }


        mainPane.setCenter(tableView);

        ToolBar toolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(event -> {
                addIkeaProductDialog.bind(null, new EntityDialogCallback<IkeaOrderItem>() {
                    @Override
                    public void onSave(IkeaOrderItem addIkeaOrderItem, Object... params) {
                        Optional<IkeaOrderItem> filter = orderItemDetails.getIkeaOrderItems()
                                .parallelStream()
                                .filter(ikeaOrderItem -> ikeaOrderItem.getProduct().getArtNumber().equals(addIkeaOrderItem.getProduct().getArtNumber()))
                                .findFirst();

                        if (filter.isPresent()) {
                            filter.get().addCount(addIkeaOrderItem.getCount());
                            tableView.update(filter.get());
                        } else {
                            orderItemDetails.getIkeaOrderItems().add(0, addIkeaOrderItem);
                            tableView.getItems().add(0, addIkeaOrderItem);
                        }

                        dialogSupport.hidePopupDialog();
                    }

                    @Override
                    public void onCancel() {
                        dialogSupport.hidePopupDialog();
                    }
                });
                dialogSupport.showPopupDialog(addIkeaProductDialog);
            });

            toolBar.getItems().add(button);
        }
        {
            Button button = new Button(null, ImageFactory.createDelete32Icon());
            button.setDisable(true);
            button.setOnAction(event -> {
                IkeaOrderItem ikeaOrderItem = tableView.getSelectionModel().getSelectedItem();

                if (ikeaOrderItem != null) {
                    tableView.getItems().remove(ikeaOrderItem);
                    orderItemDetails.getIkeaOrderItems().remove(ikeaOrderItem);
                }
            });
            tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));
            toolBar.getItems().add(button);
        }

        mainPane.setTop(toolBar);
        mainPane.setBottom(totalStatusPanel = new TotalStatusPanel());

        setContent(mainPane);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean canSkip(DesktopOrderInfo param) {
        return false;
    }

    @Override
    public void collect(DesktopOrderInfo param) {

    }

    @Override
    public void onActive(DesktopOrderInfo param) {
        tableView.getItems().clear();
        orderItemDetails = param.getOrderItemDetails();
        tableView.getItems().addAll(orderItemDetails.getIkeaOrderItems());

        BigDecimal total = orderItemDetails.getIkeaOrderItems().stream()
                .map(IkeaOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalStatusPanel.setTotal(total);
    }
}
