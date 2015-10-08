package org.menesty.ikea.ui.pages.ikea.order.component;

import com.google.common.base.Preconditions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.invoice.Invoice;
import org.menesty.ikea.lib.domain.ikea.invoice.InvoiceItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.dialog.invoice.IkeaInvoiceUploadDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.invoice.InvoiceAddEditDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Menesty on
 * 10/4/15.
 * 21:51.
 */
public class InvoiceViewComponent extends HBox {
    enum InvoiceAction {
        Delete, Add
    }

    public interface InvoiceActionListener {
        void onExport(List<Invoice> invoices);

        void onInvoiceDelete(Invoice invoice);

        void onInvoiceAdd(Invoice invoice);
    }

    private Long processOrderId;

    private TableView<Invoice> invoiceTableView;
    private TableView<InvoiceItem> invoiceItemTableView;

    private TotalStatusPanel invoiceItemStatusPanel;
    private InvoiceActionService invoiceActionService;

    public InvoiceViewComponent(DialogSupport dialogSupport, InvoiceActionListener invoiceActionListener) {
        Preconditions.checkNotNull(invoiceActionListener);

        getChildren().addAll(initLeftPanel(dialogSupport, invoiceActionListener), initRightPanel());
    }

    private Pane initLeftPanel(DialogSupport dialogSupport, final InvoiceActionListener invoiceActionListener) {
        BorderPane leftPanel = new BorderPane();
        LoadingPane loadingPane = new LoadingPane();

        StackPane mainPanel = new StackPane();
        mainPanel.getChildren().addAll(leftPanel, loadingPane);

        HBox.setHgrow(mainPanel, Priority.ALWAYS);

        invoiceTableView = new TableView<>();

        {
            TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.FILE_NAME));
            column.setPrefWidth(150);
            column.setCellValueFactory(ColumnUtil.column("fileName"));

            invoiceTableView.getColumns().add(column);
        }

        {
            TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.INVOICE_NAME));
            column.setPrefWidth(100);
            column.setCellValueFactory(ColumnUtil.column("invoiceName"));

            invoiceTableView.getColumns().add(column);
        }

        {
            TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PARAGON_NUMBER));
            column.setCellValueFactory(ColumnUtil.column("paragonNumber"));
            column.setPrefWidth(100);
            invoiceTableView.getColumns().add(column);
        }

        {
            TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SELL_DATE));
            column.setCellValueFactory(ColumnUtil.dateColumn("sellDate"));
            column.setPrefWidth(110);
            invoiceTableView.getColumns().add(column);
        }

        {
            TableColumn<Invoice, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.AMOUNT));
            column.setCellValueFactory(ColumnUtil.number("payed"));
            column.setPrefWidth(60);
            invoiceTableView.getColumns().add(column);
        }

        invoiceTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            invoiceItemTableView.getItems().clear();
            invoiceItemStatusPanel.setTotal(BigDecimal.ZERO);

            if (newValue != null && newValue.getInvoiceItems() != null) {
                invoiceItemTableView.getItems().addAll(newValue.getInvoiceItems());
                invoiceItemStatusPanel.setTotal(newValue.getInvoiceItems().stream().map(InvoiceItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        });


        ToolBar toolBar = new ToolBar();

        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());

            button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.ADD)));
            button.setOnAction(event -> {
                InvoiceAddEditDialog dialog = getInvoiceAddEditDialog(dialogSupport);

                Invoice invoice = new Invoice();
                invoice.setIkeaProcessOrderId(processOrderId);

                dialog.bind(invoice, new EntityDialogCallback<Invoice>() {
                    @Override
                    public void onSave(Invoice invoice, Object... params) {

                    }

                    @Override
                    public void onCancel() {
                        dialogSupport.hidePopupDialog();
                    }
                });

                dialogSupport.showPopupDialog(dialog);
            });

            toolBar.getItems().add(button);
        }

        {
            Button button = new Button(null, ImageFactory.createPdf32Icon());
            button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.INVOICES_UPLOAD_PDF)));
            button.setOnAction(event -> {
                IkeaInvoiceUploadDialog dialog = getInvoiceUploadDialog(dialogSupport);
                dialog.bind(processOrderId, invoiceActionListener);
                dialogSupport.showPopupDialog(dialog);
            });

            toolBar.getItems().add(button);
        }

        {
            Button button = new Button(null, ImageFactory.createDelete32Icon());

            button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.DELETE)));
            button.setOnAction(event -> {
                Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.DELETE_TITLE), I18n.UA.getString(I18nKeys.DELETE_CONFIRMATION_MESSAGE), new DialogCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onYes() {
                        invoiceActionService.setInvoice(invoiceTableView.getSelectionModel().getSelectedItem(), InvoiceAction.Delete);
                        invoiceActionService.restart();
                    }
                });

            });
            button.setDisable(true);

            invoiceTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));

            toolBar.getItems().add(button);
        }

        leftPanel.setTop(toolBar);

        invoiceActionService = new InvoiceActionService();
        invoiceActionService.setOnSucceededListener(result -> {
            if (InvoiceAction.Delete == result.getAction()) {
                invoiceActionListener.onInvoiceDelete(result.getInvoice());
            } else if (InvoiceAction.Add == result.getAction()) {
                invoiceActionListener.onInvoiceAdd(result.getInvoice());
            }
        });
        loadingPane.bindTask(invoiceActionService);

        leftPanel.setCenter(invoiceTableView);

        return mainPanel;
    }

    private BorderPane initRightPanel() {
        BorderPane rightPanel = new BorderPane();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        invoiceItemTableView = new TableView<>();

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
            column.setPrefWidth(125);
            column.setCellValueFactory(ColumnUtil.column("artNumber"));
            column.setCellFactory(ArtNumberCell::new);

            invoiceItemTableView.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
            column.setPrefWidth(170);
            column.setCellValueFactory(ColumnUtil.column("shortName"));

            invoiceItemTableView.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));
            column.setPrefWidth(50);
            column.setCellValueFactory(ColumnUtil.number("count"));

            invoiceItemTableView.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
            column.setPrefWidth(60);
            column.setCellValueFactory(ColumnUtil.number("price"));

            invoiceItemTableView.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WAT));
            column.setPrefWidth(40);
            column.setCellValueFactory(ColumnUtil.number("wat"));

            invoiceItemTableView.getColumns().add(column);
        }

        rightPanel.setCenter(invoiceItemTableView);
        rightPanel.setBottom(invoiceItemStatusPanel = new TotalStatusPanel());

        return rightPanel;
    }

    private IkeaInvoiceUploadDialog getInvoiceUploadDialog(DialogSupport dialogSupport) {
        IkeaInvoiceUploadDialog invoiceUploadDialog = new IkeaInvoiceUploadDialog(dialogSupport.getStage());
        invoiceUploadDialog.setDefaultAction(baseDialog -> {
                    baseDialog.setDefaultAction(null);
                    dialogSupport.hidePopupDialog();
                }

        );

        return invoiceUploadDialog;
    }

    private InvoiceAddEditDialog getInvoiceAddEditDialog(DialogSupport dialogSupport) {
        return new InvoiceAddEditDialog(dialogSupport.getStage());
    }

    public void setProcessOrderId(Long processOrderId) {
        this.processOrderId = processOrderId;
    }

    public void setInvoices(List<Invoice> invoices) {
        invoiceTableView.getItems().clear();
        invoiceTableView.getItems().addAll(invoices);
    }

    class InvoiceActionService extends AbstractAsyncService<InvoiceActionResult> {
        private ObjectProperty<Invoice> invoiceProperty = new SimpleObjectProperty<>();
        private ObjectProperty<InvoiceAction> invoiceActionProperty = new SimpleObjectProperty<>();

        @Override
        protected Task<InvoiceActionResult> createTask() {
            final Invoice _invoice = invoiceProperty.get();
            final InvoiceAction _invoiceAction = invoiceActionProperty.get();

            return new Task<InvoiceActionResult>() {
                @Override
                protected InvoiceActionResult call() throws Exception {
                    if (InvoiceAction.Delete == _invoiceAction) {
                        APIRequest apiRequest = HttpServiceUtil.get("/ikea-order/invoice/delete/" + _invoice.getId());
                        apiRequest.get();
                    } else if (InvoiceAction.Add == _invoiceAction) {

                    }
                    return new InvoiceActionResult(_invoice, _invoiceAction);
                }
            };
        }

        public void setInvoice(Invoice invoice, InvoiceAction action) {
            Preconditions.checkNotNull(invoice);
            Preconditions.checkNotNull(action);

            invoiceProperty.set(invoice);
            invoiceActionProperty.set(action);
        }
    }

    class InvoiceActionResult {
        private final Invoice invoice;
        private final InvoiceAction action;


        InvoiceActionResult(Invoice invoice, InvoiceAction action) {
            this.invoice = invoice;
            this.action = action;
        }

        public Invoice getInvoice() {
            return invoice;
        }

        public InvoiceAction getAction() {
            return action;
        }
    }
}
