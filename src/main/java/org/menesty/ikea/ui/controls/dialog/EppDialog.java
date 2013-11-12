package org.menesty.ikea.ui.controls.dialog;

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
import java.util.List;

public abstract class EppDialog extends BaseDialog {
    private TableView<InvoiceItem> tableView;
    private Stage stage;

    public EppDialog(Stage stage) {
        this.stage = stage;
        ToolBar toolBar = new ToolBar();

        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/epp-32x32.png"));
        Button addBtn = new Button("", imageView);
        addBtn.setContentDisplay(ContentDisplay.RIGHT);
        addBtn.setTooltip(new Tooltip("Export to EPP"));
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

            }
        });
        toolBar.getItems().add(addBtn);


        tableView = new TableView<>();
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

        BorderPane container = new BorderPane();
        container.setTop(toolBar);
        container.setCenter(tableView);

        getChildren().addAll(container, bottomBar);

        okBtn.setText("Export");
    }

    public void setItems(List<InvoiceItem> items) {
        tableView.setItems(FXCollections.observableArrayList(items));
    }

    @Override
    public void onOk() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Epp location");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Epp file (*.epp)", "*.epp");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null)
            export(tableView.getItems(), selectedFile.getAbsolutePath());

    }

    public abstract void export(List<InvoiceItem> items, String path);

}
