package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.controls.table.component.CheckBoxTableColumn;
import org.menesty.ikea.util.ColumnUtil;

public abstract class InvoicePdfTableView extends BaseTableView<InvoicePdfTableView.InvoicePdfTableItem> {

    public InvoicePdfTableView() {
        {
            TableColumn<InvoicePdfTableItem, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem>indexColumn());
            getColumns().add(column);
        }


        TableColumn<InvoicePdfTableItem, Boolean> checked = new CheckBoxTableColumn<>();
        checked.setCellValueFactory(new PropertyValueFactory<InvoicePdfTableItem, Boolean>("checked"));

        TableColumn<InvoicePdfTableItem, String> name = new TableColumn<>("Name");
        name.setMinWidth(160);
        name.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem, String>column("invoicePdf.name"));

        TableColumn<InvoicePdfTableItem, Double> priceColumn = new TableColumn<>();
        priceColumn.setText("Price");
        priceColumn.setMinWidth(60);
        priceColumn.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem, Double>column("invoicePdf.price"));

        TableColumn<InvoicePdfTableItem, String> createdDate = new TableColumn<>();

        createdDate.setText("Upload Date");
        createdDate.setMinWidth(90);
        createdDate.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem>dateColumn("invoicePdf.createdDate"));


        TableColumn<InvoicePdfTableItem, String> invoiceNumber = new TableColumn<>("Number");
        invoiceNumber.setMinWidth(100);
        invoiceNumber.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem, String>column("invoicePdf.invoiceNumber"));
        getColumns().addAll(checked, name, priceColumn, invoiceNumber, createdDate);

        setEditable(true);
    }

    @Override
    protected void onRowRender(TableRow<InvoicePdfTableItem> row, final InvoicePdfTableItem newValue) {
        row.getStyleClass().remove("greenRow");
        row.setContextMenu(null);

        if (newValue != null && newValue.getInvoicePdf().isSync())
            row.getStyleClass().add("greenRow");

        if (newValue != null && !newValue.getInvoicePdf().isSync() && newValue.hasItems()) {
            ContextMenu menu = new ContextMenu();

            {
                MenuItem item = new MenuItem("Upload", ImageFactory.createUpload16Icon());
                item.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        onUpload(newValue.getInvoicePdf());
                    }
                });

                menu.getItems().add(item);
            }

            row.setContextMenu(menu);
        }
    }

    public abstract void onUpload(InvoicePdf invoicePdf);

    public abstract void onCheck(InvoicePdf invoicePdf, boolean newValue);

    public class InvoicePdfTableItem {

        private BooleanProperty checked;

        private final InvoicePdf invoicePdf;

        private Boolean items;

        public InvoicePdfTableItem(boolean checked, final InvoicePdf invoicePdf) {
            this.invoicePdf = invoicePdf;
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old, Boolean newValue) {
                    onCheck(InvoicePdfTableItem.this.invoicePdf, newValue);
                }
            });
        }

        public BooleanProperty checkedProperty() {
            return checked;
        }

        public InvoicePdf getInvoicePdf() {
            return invoicePdf;
        }


        public void setName(String name) {
            invoicePdf.setName(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (!(o instanceof InvoicePdfTableItem)) return false;

            InvoicePdfTableItem that = (InvoicePdfTableItem) o;

            return invoicePdf.equals(that.invoicePdf);
        }

        public Boolean hasItems() {
            return items;
        }

        public void setItems(boolean items) {
            this.items = items;
        }
    }
}
