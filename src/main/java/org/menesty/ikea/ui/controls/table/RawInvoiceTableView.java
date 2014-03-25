package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.NumberUtil;

/**
 * User: Menesty
 * Date: 10/13/13
 * Time: 6:01 PM
 */
public class RawInvoiceTableView extends TableView<RawInvoiceProductItem> {

    public RawInvoiceTableView() {
        super();
        initColumns();
    }

    private void initColumns() {
        {
            TableColumn<RawInvoiceProductItem, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem>indexColumn());
            getColumns().add(column);
        }
        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, String>column("prepareArtNumber"));
            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, String>column("name"));

            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>("Count");
            column.setMaxWidth(50);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RawInvoiceProductItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<RawInvoiceProductItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });

            getColumns().add(column);
        }


        {
            TableColumn<RawInvoiceProductItem, Double> column = new TableColumn<>("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, Double>column("price"));

            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, Double> column = new TableColumn<>("T Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<RawInvoiceProductItem, Double>column("total"));

            getColumns().add(column);
        }

        {
            TableColumn<RawInvoiceProductItem, String> column = new TableColumn<>();
            column.setMinWidth(40);
            column.setCellFactory(new Callback<TableColumn<RawInvoiceProductItem, String>, TableCell<RawInvoiceProductItem, String>>() {
                @Override
                public TableCell<RawInvoiceProductItem, String> call(final TableColumn<RawInvoiceProductItem, String> column) {
                    TableCell<RawInvoiceProductItem, String> cell = new TableCell<RawInvoiceProductItem, String>() {
                        @Override
                        protected void updateItem(String o, boolean empty) {
                            if (getTableRow() != null) {
                                RawInvoiceProductItem item = (RawInvoiceProductItem) getTableRow().getItem();

                                if (item != null && item.isSeparate()) {
                                    setGraphic(new ImageView(new Image("/styles/images/icon/wheelbarrow-16x16.png")));
                                    return;
                                }
                            }

                            setGraphic(new Label());
                        }
                    };
                    cell.setAlignment(Pos.CENTER);
                    return cell;
                }
            });
            getColumns().add(column);
        }

        setRowFactory(new Callback<TableView<RawInvoiceProductItem>, TableRow<RawInvoiceProductItem>>() {
            @Override
            public TableRow<RawInvoiceProductItem> call(final TableView<RawInvoiceProductItem> rawInvoiceProductItemTableView) {
                final TableRow<RawInvoiceProductItem> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2)
                            onRowDoubleClick(row);
                    }
                });
                row.itemProperty().addListener(new ChangeListener<RawInvoiceProductItem>() {
                    @Override
                    public void changed(ObservableValue<? extends RawInvoiceProductItem> observableValue, RawInvoiceProductItem rawInvoiceProductItem, RawInvoiceProductItem newValue) {
                        row.getStyleClass().remove("productNotVerified");
                        if (newValue != null)
                            if (!newValue.getProductInfo().isVerified())
                                row.getStyleClass().add("productNotVerified");
                    }
                });
                return row;
            }
        });
    }

    public void onRowDoubleClick(TableRow<RawInvoiceProductItem> row) {

    }
}
