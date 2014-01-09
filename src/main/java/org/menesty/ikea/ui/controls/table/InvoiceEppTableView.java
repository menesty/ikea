package org.menesty.ikea.ui.controls.table;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.table.DoubleEditableTableCell;
import org.menesty.ikea.util.NumberUtil;

/**
 * Created by Menesty on 12/22/13.
 */
public abstract class InvoiceEppTableView extends TableView<InvoiceItem> {

    public InvoiceEppTableView() {
        {
            TableColumn<InvoiceItem, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellFactory(new Callback<TableColumn<InvoiceItem, Number>, TableCell<InvoiceItem, Number>>() {
                @Override
                public TableCell<InvoiceItem, Number> call(TableColumn<InvoiceItem, Number> invoiceItemNumberTableColumn) {
                    TableCell<InvoiceItem, Number> tableCell = new TableCell<InvoiceItem, Number>() {
                        private Label numberLabel;
                        private HBox content;

                        @Override
                        protected void updateItem(Number number, boolean empty) {
                            super.updateItem(number, empty);
                            if (empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                if (numberLabel == null) {
                                    content = new HBox();
                                    content.setPrefWidth(Double.MAX_VALUE);
                                    numberLabel = new Label();

                                    ImageView imageView = ImageFactory.createWeb16Icon();
                                    HBox.setMargin(imageView, new Insets(0, 0, 0, 2));
                                    imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent mouseEvent) {
                                            InvoiceItem item = getTableView().getItems().get(getIndex());
                                            ProductDialog.browse(item.getOriginArtNumber());
                                        }
                                    });
                                    Region space = new Region();
                                    HBox.setHgrow(space, Priority.ALWAYS);
                                    content.getChildren().addAll(numberLabel, space, imageView);
                                }
                                content.setMinWidth(getWidth() - getGraphicTextGap() * 2);
                                setGraphic(content);
                                numberLabel.setText(number + "");
                            }


                        }

                    };

                    return tableCell;
                }
            });
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<InvoiceItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(110);
            column.setCellFactory(TextFieldTableCell.<InvoiceItem>forTableColumn());
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<InvoiceItem, String> t) {
                    InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    if (!item.getArtNumber().equals(t.getNewValue())) {
                        item.setArtNumber(t.getNewValue());
                        onEdit(item);
                    }

                }
            });

            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("S Name");
            column.setMinWidth(170);
            column.setCellFactory(TextFieldTableCell.<InvoiceItem>forTableColumn());
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new PathProperty<>(item.getValue(), "shortName");
                }
            });
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<InvoiceItem, String> t) {
                    InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    if (!item.getShortName().equals(t.getNewValue())) {
                        item.setShortName(t.getNewValue());
                        onEdit(item);
                    }

                }
            });

            getColumns().add(column);
        }

        Callback<TableColumn<InvoiceItem, Double>, TableCell<InvoiceItem, Double>> doubleFactory = new Callback<TableColumn<InvoiceItem, Double>, TableCell<InvoiceItem, Double>>() {
            @Override
            public TableCell<InvoiceItem, Double> call(TableColumn p) {
                return new DoubleEditableTableCell<>();
            }
        };

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Count");
            column.setMaxWidth(50);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });
            //column.setCellFactory(doubleFactory);
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Price");
            column.setMaxWidth(70);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "priceWat");
                }
            });
            column.setCellFactory(doubleFactory);
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, Double>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<InvoiceItem, Double> t) {
                    InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    if (item.getPriceWat() != t.getNewValue()) {
                        item.setPrice(t.getNewValue());
                        onEdit(item);
                    }

                }
            });
            getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Weight");
            column.setMaxWidth(55);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "weight");
                }
            });
            getColumns().add(column);
        }

        ContextMenu contextMenu = new ContextMenu();
        {
            MenuItem menuItem = new MenuItem("Browse", ImageFactory.createWeb16Icon());
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    InvoiceItem item = getSelectionModel().getSelectedItem();
                    if (item != null)
                        ProductDialog.browse(item.getArtNumber());
                }
            });
            contextMenu.getItems().add(menuItem);
        }
        setContextMenu(contextMenu);
        setEditable(true);
    }

    public abstract void onEdit(InvoiceItem item);
}
