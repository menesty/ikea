package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.util.ColumnUtil;

public abstract class InvoicePdfTableView extends TableView<InvoicePdfTableView.InvoicePdfTableItem> {

    public InvoicePdfTableView() {
        {
            TableColumn<InvoicePdfTableItem, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            getColumns().add(column);
        }


        TableColumn<InvoicePdfTableItem, Boolean> checked = new TableColumn<>();
        checked.setMaxWidth(40);
        checked.setResizable(false);
        checked.setCellValueFactory(new PropertyValueFactory<InvoicePdfTableItem, Boolean>("checked"));
        checked.setCellFactory(new Callback<TableColumn<InvoicePdfTableItem, Boolean>, TableCell<InvoicePdfTableItem, Boolean>>() {
            public TableCell<InvoicePdfTableItem, Boolean> call(TableColumn<InvoicePdfTableItem, Boolean> p) {
                CheckBoxTableCell<InvoicePdfTableItem, Boolean> checkBoxTableCell = new CheckBoxTableCell<>();
                checkBoxTableCell.setAlignment(Pos.CENTER);
                return checkBoxTableCell;
            }
        });

        TableColumn<InvoicePdfTableItem, String> name = new TableColumn<>("Name");
        name.setMinWidth(160);
        name.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem, String>column("invoicePdf.name"));
        name.setCellFactory(TextFieldTableCell.<InvoicePdfTableItem>forTableColumn());
        name.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoicePdfTableItem, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<InvoicePdfTableItem, String> t) {
                InvoicePdfTableItem tableItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                tableItem.setName(t.getNewValue());
                onSave(tableItem.getInvoicePdf());

            }
        });

        TableColumn<InvoicePdfTableItem, Double> priceColumn = new TableColumn<>();
        priceColumn.setText("Price");
        priceColumn.setMinWidth(60);
        priceColumn.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem, Double>column("invoicePdf.price"));

        TableColumn<InvoicePdfTableItem, String> createdDate = new TableColumn<>();

        createdDate.setText("Upload Date");
        createdDate.setMinWidth(90);
        createdDate.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem>dateColumn("invoicePdf.createdDate"));

        setEditable(true);

        TableColumn<InvoicePdfTableItem, String> invoiceNumber = new TableColumn<>("Number");
        invoiceNumber.setMinWidth(100);
        invoiceNumber.setCellValueFactory(ColumnUtil.<InvoicePdfTableItem, String>column("invoicePdf.invoiceNumber"));
        getColumns().addAll(checked, name, priceColumn, invoiceNumber, createdDate);
    }

    public abstract void onSave(InvoicePdf invoicePdf);

    public abstract void onCheck(InvoicePdf invoicePdf, boolean newValue);

    public class InvoicePdfTableItem {

        private BooleanProperty checked;

        private final InvoicePdf invoicePdf;

        public InvoicePdfTableItem(boolean checked, InvoicePdf invoicePdf) {
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
    }


}
