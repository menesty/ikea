package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.table.DoubleEditableTableCell;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

public abstract class EppDialog extends BaseDialog {

    private final Button delBtn;

    private TableView<InvoiceItem> tableView;

    private StatusPanel statusPanel;

    private Stage stage;

    private BigDecimal invoicePrice;

    private List<TableRow<InvoiceItem>> rows = new ArrayList<>();

    public EppDialog(Stage stage) {
        this.stage = stage;
        setMaxSize(500, USE_PREF_SIZE);
        ToolBar toolBar = new ToolBar();
        final InvalidationListener invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                statusPanel.setCurrentTotal(calculatePrice(tableView.getItems()).doubleValue());
            }
        };
        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/plus-32x32.png"));

        Button addBtn = new Button("", imageView);
        addBtn.setContentDisplay(ContentDisplay.RIGHT);
        addBtn.setTooltip(new Tooltip("Add Row"));
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                InvoiceItem item = new InvoiceItem();
                item.setArtNumber("New");
                item.setName("New");
                item.setShortName("New");
                tableView.getItems().add(item);
            }
        });
        toolBar.getItems().add(addBtn);

        imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/minus-32x32.png"));
        delBtn = new Button("", imageView);
        delBtn.setContentDisplay(ContentDisplay.RIGHT);
        delBtn.setTooltip(new Tooltip("Delete Row"));
        delBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                tableView.getItems().remove(tableView.getSelectionModel().getSelectedItem());
                invalidationListener.invalidated(null);
            }
        });
        delBtn.setDisable(true);
        toolBar.getItems().add(delBtn);


        imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/balance-32x32.png"));
        Button balanceBtn = new Button(null, imageView);
        balanceBtn.setContentDisplay(ContentDisplay.RIGHT);
        balanceBtn.setTooltip(new Tooltip("Auto balance Price"));
        balanceBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                List<InvoiceItem> items = new ArrayList<>(tableView.getItems());
                Iterator<InvoiceItem> iterator = items.iterator();

                BigDecimal zestavPrice = BigDecimal.ZERO;

                while (iterator.hasNext()) {
                    InvoiceItem item = iterator.next();

                    if (item.isZestav()) {
                        iterator.remove();
                        zestavPrice = zestavPrice.add(item.getTotalWatPrice());
                    }
                }

                Collections.sort(items, Collections.reverseOrder(new Comparator<InvoiceItem>() {
                    @Override
                    public int compare(InvoiceItem o1, InvoiceItem o2) {
                        Double cof1 = o1.getPrice() / o1.getWeight();
                        Double cof2 = o2.getPrice() / o2.getWeight();
                        return cof1.compareTo(cof2);
                    }
                }));

                BigDecimal currentPrice = calculatePrice(items);
                BigDecimal diff = invoicePrice.subtract(currentPrice.add(zestavPrice));

                BigDecimal addToEach = BigDecimal.valueOf(NumberUtil.round(diff.doubleValue() / items.size()));

                for (InvoiceItem item : items) {
                    BigDecimal addToOne = BigDecimal.valueOf(NumberUtil.round(addToEach.doubleValue() / item.getCount()));
                    item.setPrice(BigDecimal.valueOf(item.getPriceWat()).add(addToOne).doubleValue());
                }


                currentPrice = calculatePrice(items).add(zestavPrice);
                diff = invoicePrice.subtract(currentPrice);
                if (diff.doubleValue() != 0) {
                    InvoiceItem lastItem = items.get(items.size() - 1);
                    lastItem.setPrice(BigDecimal.valueOf(lastItem.getPriceWat()).add(diff).doubleValue());
                }

                for (TableRow<InvoiceItem> row : rows)
                    row.setItem(null);

                invalidationListener.invalidated(null);

            }
        });
        toolBar.getItems().add(balanceBtn);

        tableView = new TableView<>();
        {
            TableColumn<InvoiceItem, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Number>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<InvoiceItem, Number> item) {
                    return new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
                }
            });
            tableView.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(100);
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
                    item.setArtNumber(t.getNewValue());

                }
            });
            tableView.getColumns().add(column);
        }

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("S Name");
            column.setMinWidth(150);
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
                    item.setShortName(t.getNewValue());

                }
            });

            tableView.getColumns().add(column);
        }

        Callback<TableColumn<InvoiceItem, Double>, TableCell<InvoiceItem, Double>> doubleFactory = new Callback<TableColumn<InvoiceItem, Double>, TableCell<InvoiceItem, Double>>() {
            @Override
            public TableCell<InvoiceItem, Double> call(TableColumn p) {
                return new DoubleEditableTableCell<>();
            }
        };

        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "count");
                }
            });
            column.setCellFactory(doubleFactory);
            tableView.getColumns().add(column);
        }


        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Price");
            column.setMinWidth(60);
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
                    item.setPrice(t.getNewValue());

                }
            });
            tableView.getColumns().add(column);
        }

        tableView.setEditable(true);
        tableView.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                delBtn.setDisable(tableView.getSelectionModel().getSelectedItem() == null);
            }
        });

        tableView.setRowFactory(new Callback<TableView<InvoiceItem>, TableRow<InvoiceItem>>() {
            @Override
            public TableRow<InvoiceItem> call(TableView<InvoiceItem> invoiceItemTableView) {
                TableRow<InvoiceItem> row = new TableRow<>();
                rows.add(row);
                return row;
            }
        });
        tableView.itemsProperty().addListener(invalidationListener);

        tableView.editingCellProperty().addListener(invalidationListener);

        BorderPane container = new BorderPane();
        container.setTop(toolBar);
        container.setCenter(tableView);
        container.setBottom(statusPanel = new StatusPanel());

        getChildren().addAll(container, bottomBar);

        okBtn.setText("Export");
    }

    private BigDecimal calculatePrice(List<InvoiceItem> items) {
        BigDecimal price = BigDecimal.ZERO;

        for (InvoiceItem item : items)
            price = price.add(item.getTotalWatPrice());

        return price;

    }

    public void setItems(List<InvoiceItem> items, BigDecimal invoicePrice) {
        tableView.setItems(FXCollections.observableArrayList(items));
        statusPanel.setTotal(invoicePrice.doubleValue());
        this.invoicePrice = invoicePrice;
    }

    @Override
    public void onOk() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Epp location");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Epp file (*.epp)", "*.epp");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            List<InvoiceItem> list = tableView.getItems();
            int index = 0;

            for (InvoiceItem invoiceItem : list)
                invoiceItem.setIndex(++index);

            export(tableView.getItems(), selectedFile.getAbsolutePath());
        }

    }

    public abstract void export(List<InvoiceItem> items, String path);


    class StatusPanel extends TotalStatusPanel {
        private Label currentTotal;

        public StatusPanel() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getItems().add(spacer);
            getItems().add(new Label("Current  :"));
            getItems().add(currentTotal = new Label());
        }

        public void setCurrentTotal(double total) {
            currentTotal.setText(NumberFormat.getNumberInstance().format(total));
        }
    }

}
