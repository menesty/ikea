package org.menesty.ikea.ui.pages.wizard.order.step;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.lib.dto.OrderItemDetails;
import org.menesty.ikea.lib.dto.ProductPriceMismatch;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Menesty on
 * 9/11/15.
 * 19:40.
 */
public class Step4PriceMismatch extends BaseWizardStep<DesktopOrderInfo> {
    private TableView<ProductPriceMismatch> productPriceMismatchTableView;
    private TableView<String> noAvailableTableView;
    private AbstractExportAsyncService<List<ProductPriceMismatch>> xlsMismatchExportAsyncService;
    private AbstractExportAsyncService<List<String>> xlsnoAvailableExportAsyncService;

    public Step4PriceMismatch(DialogSupport dialogSupport) {
        HBox mainPane = new HBox();
        mainPane.setSpacing(8);

        BorderPane priceMismatchPane = initMismatchPane(dialogSupport);
        BorderPane notAvailablePanePane = initNotAvailablePane(dialogSupport);

        HBox.setHgrow(priceMismatchPane, Priority.ALWAYS);
        HBox.setHgrow(notAvailablePanePane, Priority.ALWAYS);

        mainPane.getChildren().addAll(priceMismatchPane, notAvailablePanePane);

        setContent(mainPane);
    }

    private BorderPane initNotAvailablePane(DialogSupport dialogSupport) {
        BorderPane mainPane = new BorderPane();
        noAvailableTableView = new TableView<>();
        {
            TableColumn<String, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setMinWidth(200);
            column.setCellValueFactory(param -> {
                if (param == null) {
                    return null;
                }

                return new SimpleStringProperty(param.getValue());
            });
            noAvailableTableView.getColumns().add(column);
        }

        ToolBar toolBar = new ToolBar();

        {
            Button button = new Button(null, ImageFactory.createXlsExport32Icon());
            button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.XLS_EXPORT)));
            button.setOnAction(event -> {
                File selectedFile = FileChooserUtil.getXls().showSaveDialog(dialogSupport.getStage());

                if (selectedFile != null) {
                    xlsnoAvailableExportAsyncService.setFile(selectedFile);
                    xlsnoAvailableExportAsyncService.setParam(noAvailableTableView.getItems());
                    xlsnoAvailableExportAsyncService.reset();
                }
            });

            toolBar.getItems().add(button);
        }

        mainPane.setCenter(noAvailableTableView);
        mainPane.setTop(toolBar);

        xlsnoAvailableExportAsyncService = new AbstractExportAsyncService<List<String>>() {
            @Override
            protected void export(File file, List<String> param) {
                ServiceFacade.getXlsExportService().exportNotAvailable(file, param);
            }
        };

        return mainPane;
    }

    private BorderPane initMismatchPane(DialogSupport dialogSupport) {
        BorderPane mainPane = new BorderPane();

        productPriceMismatchTableView = new TableView<>();

        {
            TableColumn<ProductPriceMismatch, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setMinWidth(170);
            column.setCellValueFactory(ColumnUtil.<ProductPriceMismatch, String>column("artNumber"));

            productPriceMismatchTableView.getColumns().add(column);
        }

        {
            TableColumn<ProductPriceMismatch, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
            column.setCellValueFactory(ColumnUtil.<ProductPriceMismatch, Number>column("orderPrice"));
            column.setMinWidth(100);

            productPriceMismatchTableView.getColumns().add(column);

        }

        {
            TableColumn<ProductPriceMismatch, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SITE_PRICE));
            column.setCellValueFactory(ColumnUtil.<ProductPriceMismatch, Number>column("sitePrice"));
            column.setMinWidth(100);

            productPriceMismatchTableView.getColumns().add(column);
        }

        ToolBar toolBar = new ToolBar();

        {
            Button button = new Button(null, ImageFactory.createXlsExport32Icon());
            button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.XLS_EXPORT)));
            button.setOnAction(event -> {
                File selectedFile = FileChooserUtil.getXls().showSaveDialog(dialogSupport.getStage());

                if (selectedFile != null) {
                    xlsMismatchExportAsyncService.setFile(selectedFile);
                    xlsMismatchExportAsyncService.setParam(productPriceMismatchTableView.getItems());
                    xlsMismatchExportAsyncService.reset();
                }
            });

            toolBar.getItems().add(button);
        }

        mainPane.setTop(toolBar);
        mainPane.setCenter(productPriceMismatchTableView);
        xlsMismatchExportAsyncService = new AbstractExportAsyncService<List<ProductPriceMismatch>>() {
            @Override
            protected void export(File file, List<ProductPriceMismatch> param) {
                ServiceFacade.getXlsExportService().exportProductPriceMismatch(file, param);
            }
        };

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
        productPriceMismatchTableView.getItems().clear();
        productPriceMismatchTableView.getItems().addAll(param.getOrderItemDetails().getProductPriceMismatches());

        noAvailableTableView.getItems().clear();
        noAvailableTableView.getItems().addAll(param.getOrderItemDetails().getNotAvailable());
    }
}


