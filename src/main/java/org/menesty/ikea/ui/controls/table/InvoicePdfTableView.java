package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
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
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.pages.OrderListPage;

public abstract class InvoicePdfTableView extends TableView<InvoicePdfTableView.InvoicePdfTableItem> {

    public InvoicePdfTableView() {
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

        TableColumn<InvoicePdfTableItem, String> name = new TableColumn<>();

        name.setText("Name");
        name.setMinWidth(200);
        name.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, String> item) {
                return new PathProperty<>(item.getValue(), "invoicePdf.name");
            }
        });
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
        priceColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, Double> item) {
                return new PathProperty<>(item.getValue(), "invoicePdf.price");
            }
        });

        TableColumn<InvoicePdfTableItem, String> createdDate = new TableColumn<>();

        createdDate.setText("Created Date");
        createdDate.setMinWidth(200);
        createdDate.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<InvoicePdfTableItem, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoicePdfTableItem, String> item) {
                        return new SimpleStringProperty(OrderListPage.DATE_FORMAT.format(item.getValue().getInvoicePdf().getCreatedDate()));
                    }
                });

        setEditable(true);
        getColumns().addAll(checked, name, priceColumn, createdDate);
    }

    public abstract void onSave(InvoicePdf invoicePdf);

    public abstract void onCheck(InvoicePdf invoicePdf);

    public class InvoicePdfTableItem {

        private BooleanProperty checked;

        private final InvoicePdf invoicePdf;

        public InvoicePdfTableItem(boolean checked, InvoicePdf invoicePdf) {
            this.invoicePdf = invoicePdf;
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    onCheck(InvoicePdfTableItem.this.invoicePdf);
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
