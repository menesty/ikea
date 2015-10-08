package org.menesty.ikea.ui.pages.wizard.order.step;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.dto.OrderItemDetails;
import org.menesty.ikea.lib.dto.ProductPriceMismatch;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.parser.RawItem;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.ui.pages.wizard.order.step.service.ProductInfoAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 9/11/15.
 * 19:40.
 */
public class Step4PriceMismatch extends BaseWizardStep<DesktopOrderInfo> {
    private TableView<ProductPriceMismatch> productPriceMismatchTableView;
    private BaseTableView<String> noAvailableTableView;
    private AbstractExportAsyncService<ProcessResult> xlsResultExportAsyncService;
    private ProductInfoAsyncService productInfoAsyncService;
    private DesktopOrderInfo desktopOrderInfo;

    public Step4PriceMismatch(DialogSupport dialogSupport) {
        HBox mainPane = new HBox();
        mainPane.setSpacing(8);

        BorderPane priceMismatchPane = initMismatchPane();
        Node notAvailablePanePane = initNotAvailablePane();

        HBox.setHgrow(priceMismatchPane, Priority.ALWAYS);
        HBox.setHgrow(notAvailablePanePane, Priority.ALWAYS);

        mainPane.getChildren().addAll(priceMismatchPane, notAvailablePanePane);

        xlsResultExportAsyncService = new AbstractExportAsyncService<ProcessResult>() {
            @Override
            protected void export(File file, ProcessResult param) {
                ServiceFacade.getXlsExportService().exportProductPriceMismatchNotAvailable(file, param.getNotAvailable(), param.getProductPriceMismatches());
            }
        };

        ToolBar toolBar = new ToolBar();

        {
            Button button = new Button(null, ImageFactory.createXlsExport32Icon());
            button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.XLS_EXPORT)));
            button.setOnAction(event -> {
                File selectedFile = FileChooserUtil.getXls().showSaveDialog(dialogSupport.getStage());

                if (selectedFile != null) {
                    xlsResultExportAsyncService.setFile(selectedFile);
                    xlsResultExportAsyncService.setParam(new ProcessResult(productPriceMismatchTableView.getItems(), noAvailableTableView.getItems()));
                    xlsResultExportAsyncService.restart();
                }
            });

            toolBar.getItems().add(button);
        }

        BorderPane borderPanel = new BorderPane();
        borderPanel.setTop(toolBar);

        borderPanel.setCenter(mainPane);

        setContent(borderPanel);
    }

    private StackPane initNotAvailablePane() {
        StackPane stackPane = new StackPane();
        noAvailableTableView = new BaseTableView<>();

        {
            TableColumn<String, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setMinWidth(200);
            column.setCellValueFactory(param -> {
                if (param == null) {
                    return null;
                }

                return new SimpleStringProperty(param.getValue());
            });

            column.setCellFactory(ArtNumberCell::new);
            noAvailableTableView.getColumns().add(column);
        }

        noAvailableTableView.setRowRenderListener((row, newValue) -> {
            row.setContextMenu(null);

            if (newValue != null) {

                ContextMenu contextMenu = new ContextMenu();

                {
                    MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.PRODUCT_FETCH), ImageFactory.createFetch16Icon());
                    menuItem.setOnAction(actionEvent -> reloadProduct(row.getItem()));
                    contextMenu.getItems().add(menuItem);
                }

                row.setContextMenu(contextMenu);
            }
        });

        LoadingPane noAvailableLoadingPane = new LoadingPane();

        productInfoAsyncService = new ProductInfoAsyncService();
        productInfoAsyncService.setOnSucceededListener(value -> {
            if (!value.getIkeaOrderItems().isEmpty()) {
                IkeaOrderItem item = value.getIkeaOrderItems().get(0);
                noAvailableTableView.getItems().remove(item.getProduct().getArtNumber());
                desktopOrderInfo.getOrderItemDetails().getIkeaOrderItems().add(item);
            }
        });
        noAvailableLoadingPane.bindTask(productInfoAsyncService);


        stackPane.getChildren().addAll(noAvailableTableView, noAvailableLoadingPane);

        return stackPane;
    }

    private void reloadProduct(String artNumber) {
        productInfoAsyncService.setRawItems(Collections.singletonList(new RawItem(artNumber)));
        productInfoAsyncService.restart();
    }

    private BorderPane initMismatchPane() {
        BorderPane mainPane = new BorderPane();

        productPriceMismatchTableView = new TableView<>();

        {
            TableColumn<ProductPriceMismatch, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setMinWidth(170);
            column.setCellValueFactory(ColumnUtil.column("artNumber"));
            column.setCellFactory(ArtNumberCell::new);

            productPriceMismatchTableView.getColumns().add(column);
        }

        {
            TableColumn<ProductPriceMismatch, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
            column.setCellValueFactory(ColumnUtil.column("orderPrice"));
            column.setMinWidth(100);

            productPriceMismatchTableView.getColumns().add(column);

        }

        {
            TableColumn<ProductPriceMismatch, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SITE_PRICE));
            column.setCellValueFactory(ColumnUtil.column("sitePrice"));
            column.setMinWidth(100);

            productPriceMismatchTableView.getColumns().add(column);
        }

        mainPane.setCenter(productPriceMismatchTableView);


        return mainPane;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean canSkip(DesktopOrderInfo param) {
        OrderItemDetails orderItemDetails = param.getOrderItemDetails();
        return orderItemDetails.getProductPriceMismatches().isEmpty() && orderItemDetails.getNotAvailable().isEmpty();
    }

    @Override
    public void collect(DesktopOrderInfo param) {

    }

    @Override
    public void onActive(DesktopOrderInfo param) {
        this.desktopOrderInfo = param;
        productPriceMismatchTableView.getItems().clear();
        productPriceMismatchTableView.getItems().addAll(param.getOrderItemDetails().getProductPriceMismatches());

        noAvailableTableView.getItems().clear();
        noAvailableTableView.getItems().addAll(param.getOrderItemDetails().getNotAvailable());
    }

    class ProcessResult {
        private final List<ProductPriceMismatch> productPriceMismatches;
        private final List<String> notAvailable;

        public ProcessResult(List<ProductPriceMismatch> productPriceMismatches, List<String> notAvailable) {
            this.productPriceMismatches = productPriceMismatches;
            this.notAvailable = notAvailable;
        }

        public List<ProductPriceMismatch> getProductPriceMismatches() {
            return productPriceMismatches;
        }

        public List<String> getNotAvailable() {
            return notAvailable;
        }
    }
}
