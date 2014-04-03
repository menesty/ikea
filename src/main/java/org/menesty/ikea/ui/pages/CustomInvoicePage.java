package org.menesty.ikea.ui.pages;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.dialog.InvoicePdfDialog;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.NumberUtil;

import java.util.List;

public class CustomInvoicePage extends BasePage {

    private ToolBar invoiceItemToolBar;

    private InvoicePdfDialog invoicePdfDialog;

    private InvoiceItemDialog invoiceItemDialog;

    private LoadService loadService;

    private LoadInvoiceItemService loadInvoiceItemService;

    private TableView<InvoicePdf> invoicePdfTable;

    private TableView<InvoiceItem> invoiceItemTable;

    public CustomInvoicePage() {
        super("Invoice");

        invoicePdfDialog = new InvoicePdfDialog();
        invoiceItemDialog = new InvoiceItemDialog();

        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<InvoicePdf>>() {
            @Override
            public void onSucceeded(List<InvoicePdf> value) {
                invoicePdfTable.getItems().clear();
                invoicePdfTable.getItems().addAll(value);
            }
        });

        loadInvoiceItemService = new LoadInvoiceItemService();
        loadInvoiceItemService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<InvoiceItem>>() {
            @Override
            public void onSucceeded(List<InvoiceItem> value) {
                invoiceItemTable.getItems().clear();
                invoiceItemTable.getItems().addAll(value);
            }
        });
    }

    @Override
    public Node createView() {
        BorderPane main = new BorderPane();
        main.setCenter(createInvoicePdfPane());
        main.setBottom(createInvoiceItemPane());
        return main;
    }

    private BorderPane createInvoiceItemPane() {
        BorderPane pane = new BorderPane();
        pane.setPrefHeight(250);

        invoiceItemToolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    InvoiceItem item = new InvoiceItem();

                    item.invoicePdf = invoicePdfTable.getSelectionModel().getSelectedItem();
                    item.setZestav(true);
                    item.setVisible(true);
                    item.setCount(1);

                    invoiceItemDialog.bind(item, new EntityDialogCallback<InvoiceItem>() {
                        @Override
                        public void onSave(InvoiceItem invoiceItem, Object... params) {
                            ServiceFacade.getInvoiceItemService().save(invoiceItem);
                            loadInvoiceItemService.restart();

                            hidePopupDialog();
                        }

                        @Override
                        public void onCancel() {
                            hidePopupDialog();
                        }
                    });

                    showPopupDialog(invoiceItemDialog);
                }
            });

            invoiceItemToolBar.getItems().add(button);
        }

        invoiceItemTable = new TableView<>();

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(105);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, String>column("artNumber"));
            invoiceItemTable.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("S Name");
            column.setMinWidth(170);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, String>column("shortName"));
            invoiceItemTable.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Count");
            column.setMaxWidth(50);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });
            invoiceItemTable.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Total");
            column.setMaxWidth(55);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, Double>column("priceWatTotal"));
            invoiceItemTable.getColumns().add(column);
        }


        pane.setTop(invoiceItemToolBar);
        pane.setCenter(invoiceItemTable);

        return pane;
    }

    private BorderPane createInvoicePdfPane() {
        BorderPane pane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    invoicePdfDialog.bind(new InvoicePdf(), new EntityDialogCallback<InvoicePdf>() {
                        @Override
                        public void onSave(InvoicePdf invoicePdf, Object... params) {
                            ServiceFacade.getInvoicePdfService().save(invoicePdf);
                            hidePopupDialog();
                            loadingPane.bindTask(loadService);
                            loadService.restart();
                        }

                        @Override
                        public void onCancel() {
                            hidePopupDialog();
                        }
                    });

                    showPopupDialog(invoicePdfDialog);
                }
            });

            toolBar.getItems().add(button);
        }

        pane.setTop(toolBar);


        invoicePdfTable = new TableView<>();

        {
            TableColumn<InvoicePdf, String> column = new TableColumn<>("Name");
            column.setMinWidth(160);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf, String>column("name"));
            invoicePdfTable.getColumns().add(column);

        }

        {
            TableColumn<InvoicePdf, String> column = new TableColumn<>("Number");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf, String>column("invoiceNumber"));
            invoicePdfTable.getColumns().add(column);
        }

        {
            TableColumn<InvoicePdf, Double> column = new TableColumn<>();
            column.setText("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf, Double>column("price"));
            invoicePdfTable.getColumns().add(column);
        }

        {
            TableColumn<InvoicePdf, String> column = new TableColumn<>();
            column.setText("Created Date");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf>dateColumn("createdDate"));
            invoicePdfTable.getColumns().add(column);
        }

        invoicePdfTable.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                InvoicePdf invoicePdf = invoicePdfTable.getSelectionModel().getSelectedItem();

                if (invoicePdf != null) {
                    loadInvoiceItemService.setId(invoicePdf.getId());
                    loadingPane.bindTask(loadInvoiceItemService);
                    loadInvoiceItemService.restart();
                    invoiceItemToolBar.setDisable(invoicePdf.isSync());
                } else
                    invoiceItemToolBar.setDisable(true);
            }
        });

        pane.setCenter(invoicePdfTable);

        return pane;

    }

    @Override
    public void onActive(Object... params) {
        invoiceItemToolBar.setDisable(true);

        loadingPane.bindTask(loadService);
        loadService.restart();
    }

    @Override
    protected Node createIconContent() {
        return ImageFactory.createInvoice72Icon();
    }

    class LoadService extends AbstractAsyncService<List<InvoicePdf>> {

        @Override
        protected Task<List<InvoicePdf>> createTask() {
            return new Task<List<InvoicePdf>>() {
                @Override
                protected List<InvoicePdf> call() throws Exception {
                    return ServiceFacade.getInvoicePdfService().loadBy(null);
                }
            };
        }
    }

    class LoadInvoiceItemService extends AbstractAsyncService<List<InvoiceItem>> {
        private SimpleIntegerProperty idProperty = new SimpleIntegerProperty();

        @Override
        protected Task<List<InvoiceItem>> createTask() {
            final int _id = idProperty.get();

            return new Task<List<InvoiceItem>>() {
                @Override
                protected List<InvoiceItem> call() throws Exception {
                    return ServiceFacade.getInvoiceItemService().loadInvoicePdf(_id);
                }
            };
        }

        void setId(int id) {
            idProperty.set(id);
        }
    }
}


class InvoiceItemDialog extends BaseDialog {

    private InvoiceItem currentItem;

    private EntityDialogCallback<InvoiceItem> callback;

    private InvoiceItemForm form;

    public InvoiceItemDialog() {
        addRow(createTitle("Create invoice"));

        addRow(form = new InvoiceItemForm(), bottomBar);
        okBtn.setText("Save");
    }

    @Override
    public void onOk() {
        currentItem.setPrice(form.getPrice());
        currentItem.basePrice = form.getPrice();
        currentItem.setName(form.getName());
        currentItem.setShortName(form.getShortName());
        currentItem.setOriginArtNumber(form.getOriginalArtNumber());
        currentItem.setArtNumber(form.getOriginalArtNumber());

        if (callback != null)
            callback.onSave(currentItem);
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    public void bind(InvoiceItem invoiceItem, EntityDialogCallback<InvoiceItem> callback) {
        currentItem = invoiceItem;
        this.callback = callback;

        form.reset();
        form.setPrice(invoiceItem.getPrice());
        form.setName(invoiceItem.getName());
        form.setShortName(invoiceItem.getShortName());

    }

    class InvoiceItemForm extends RowPanel {
        private TextField originalArtNumber;

        private DoubleTextField basePrice;

        private TextField shortName;

        private TextField name;


        public InvoiceItemForm() {
            addRow("Art Number", originalArtNumber = new TextField());
            addRow("Name", name = new TextField());
            addRow("Short name", shortName = new TextField());
            addRow("Price", basePrice = new DoubleTextField());
        }

        void setShortName(String shortName) {
            this.shortName.setText(shortName);
        }

        String getShortName() {
            return shortName.getText();
        }

        void setOriginalArtNumber(String originArtNumber) {
            this.originalArtNumber.setText(originArtNumber);
        }

        String getOriginalArtNumber() {
            return originalArtNumber.getText();
        }

        void setPrice(double price) {
            basePrice.setNumber(price);
        }

        double getPrice() {
            return basePrice.getNumber();
        }

        void setName(String name) {
            this.name.setText(name);
        }

        String getName() {
            return name.getText();
        }

        void reset() {
            originalArtNumber.clear();
            basePrice.clear();
            shortName.clear();
            name.clear();
        }
    }
}