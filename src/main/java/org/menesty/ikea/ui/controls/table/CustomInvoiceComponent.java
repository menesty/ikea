package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.util.ColumnUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Created by Menesty on 4/6/14.
 */
public abstract class CustomInvoiceComponent extends BorderPane {
    private final LoadService loadService;
    private final ToolBar invoiceItemToolBar;

    private TableView<InvoiceItem> invoiceItemTable;

    private InvoiceItemDialog invoiceItemDialog;

    private InvoicePdf invoicePdf;

    public CustomInvoiceComponent() {
        invoiceItemDialog = new InvoiceItemDialog();

        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<InvoiceItem>>() {
            @Override
            public void onSucceeded(List<InvoiceItem> value) {
                setItems(value);
            }
        });

        final EntityDialogCallback<InvoiceItem> saveHandler = new EntityDialogCallback<InvoiceItem>() {
            @Override
            public void onSave(InvoiceItem invoiceItem, Object... params) {
                IkeaApplication.get().hidePopupDialog();

                startWork();
                ServiceFacade.getInvoiceItemService().save(invoiceItem);
                InvoicePdf invoicePdf = getInvoicePdf();
                List<InvoiceItem> items = ServiceFacade.getInvoiceItemService().loadBy(invoicePdf);
                //recalculate invoice price
                BigDecimal price = BigDecimal.ZERO;

                for (InvoiceItem item : items)
                    price = price.add(item.getTotalWatPrice()).setScale(2, RoundingMode.CEILING);

                invoicePdf.setPrice(price.doubleValue());
                ServiceFacade.getInvoicePdfService().save(invoicePdf);
                update(invoicePdf);

                setItems(items);
                endWork();
            }

            @Override
            public void onCancel() {
                IkeaApplication.get().hidePopupDialog();
            }
        };

        invoiceItemToolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    InvoiceItem item = new InvoiceItem();

                    item.invoicePdf = getInvoicePdf();
                    item.setZestav(true);
                    item.setVisible(true);
                    item.setCount(1);

                    invoiceItemDialog.bind(item, saveHandler);

                    IkeaApplication.get().showPopupDialog(invoiceItemDialog);
                }
            });

            invoiceItemToolBar.getItems().add(button);
        }

        invoiceItemTable = new BaseTableView<InvoiceItem>() {
            @Override
            protected void onRowDoubleClick(TableRow<InvoiceItem> row) {
                if (row.getItem() == null)
                    return;

                invoiceItemDialog.bind(row.getItem(), saveHandler);

                IkeaApplication.get().showPopupDialog(invoiceItemDialog);
            }
        };

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
            column.setCellValueFactory(ColumnUtil.<InvoiceItem>number("count"));
            invoiceItemTable.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Total");
            column.setMaxWidth(55);
            column.setCellValueFactory(ColumnUtil.<InvoiceItem, Double>column("priceWatTotal"));
            invoiceItemTable.getColumns().add(column);
        }


        setTop(invoiceItemToolBar);
        setCenter(invoiceItemTable);

    }

    protected InvoicePdf getInvoicePdf() {
        return invoicePdf;
    }

    protected void startWork() {
    }

    protected void endWork() {
    }

    protected abstract void beforeLoad(Worker<?> task);

    protected abstract void update(InvoicePdf invoicePdf);

    public void setInvoicePdf(InvoicePdf invoicePdf) {
        this.invoicePdf = invoicePdf;

        if (invoicePdf != null) {
            loadService.setId(invoicePdf.getId());
            beforeLoad(loadService);
            loadService.restart();
            invoiceItemToolBar.setDisable(invoicePdf.isSync());
        } else {
            invoiceItemToolBar.setDisable(true);
            invoiceItemTable.getItems().clear();
        }
    }

    class InvoiceItemDialog extends BaseDialog {

        private InvoiceItem currentItem;

        private EntityDialogCallback<InvoiceItem> callback;

        private InvoiceItemForm form;

        public InvoiceItemDialog() {
            setTitle("Create invoice");

            addRow(form = new InvoiceItemForm(), bottomBar);
            okBtn.setText("Save");
        }

        @Override
        public void onOk() {
            currentItem.setPrice(form.getPrice());
            currentItem.basePrice = form.getPrice();
            currentItem.setName(form.getName());
            currentItem.setShortName(form.getShortName());

            if (currentItem.getId() == null)
                currentItem.setOriginArtNumber(UUID.randomUUID().toString());

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
            form.setOriginalArtNumber(invoiceItem.getOriginArtNumber());

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

    public void setItems(List<InvoiceItem> items) {
        invoiceItemTable.getItems().clear();
        invoiceItemTable.getItems().addAll(items);
    }

    public List<InvoiceItem> getItems() {
        return invoiceItemTable.getItems();
    }

    class LoadService extends AbstractAsyncService<List<InvoiceItem>> {
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
