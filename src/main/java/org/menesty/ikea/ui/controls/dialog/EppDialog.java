package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;

public abstract class EppDialog extends BaseDialog {

    private final Button delBtn;

    private TableView<InvoiceItem> tableView;

    private StatusPanel statusPanel;

    private Stage stage;

    public EppDialog(Stage stage) {
        this.stage = stage;
        setMaxSize(500, USE_PREF_SIZE);
        ToolBar toolBar = new ToolBar();

        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/plus-32x32.png"));

        Button addBtn = new Button("", imageView);
        addBtn.setContentDisplay(ContentDisplay.RIGHT);
        addBtn.setTooltip(new Tooltip("Add Row"));
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                tableView.getItems().add(new InvoiceItem());
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
            }
        });
        delBtn.setDisable(true);
        toolBar.getItems().add(delBtn);


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

        {
            TableColumn<InvoiceItem, String> column = new TableColumn<>("Count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<InvoiceItem, String> item) {
                    return new SimpleStringProperty(NumberUtil.toString(item.getValue().getCount()));
                }
            });

            tableView.getColumns().add(column);
        }


        {
            TableColumn<InvoiceItem, Double> column = new TableColumn<>("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InvoiceItem, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<InvoiceItem, Double> item) {
                    return new PathProperty<>(item.getValue(), "price");
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

        tableView.itemsProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                double price = 0;

                for (InvoiceItem item : tableView.getItems())
                    price += NumberUtil.round(item.getPrice() * item.getCount());

                statusPanel.setCurrentTotal(NumberUtil.round(price));

            }
        });

        BorderPane container = new BorderPane();
        container.setTop(toolBar);
        container.setCenter(tableView);
        container.setBottom(statusPanel = new StatusPanel());

        getChildren().addAll(container, bottomBar);

        okBtn.setText("Export");
    }

    public void setItems(List<InvoiceItem> items, double invoicePrice) {
        tableView.setItems(FXCollections.observableArrayList(items));
        statusPanel.setTotal(invoicePrice);
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


    class StatusPanel extends ToolBar {
        private Label totalLabel;
        private Label currentTotal;

        public StatusPanel() {
            getItems().add(new Label("Total :"));
            getItems().add(totalLabel = new Label());
            getItems().add(currentTotal = new Label());
        }

        public void setTotal(double total) {
            totalLabel.setText(NumberFormat.getNumberInstance().format(total));
        }

        public void setCurrentTotal(double total) {
            currentTotal.setText(NumberFormat.getNumberInstance().format(total));
        }
    }

}
