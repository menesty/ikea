package org.menesty.ikea.ui.controls.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.menesty.ikea.domain.StorageComboLack;
import org.menesty.ikea.domain.StorageComboPartLack;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.table.ProductBrowseColumn;

import java.util.List;


/**
 * Created by Menesty on 1/11/14.
 */
public class StorageLackComboComponent extends HBox {
    TableView<StorageComboLack> tableView;
    TableView<StorageComboPartLack> tableItemView;

    public StorageLackComboComponent() {
        tableView = new TableView<>();
        {
            ProductBrowseColumn<StorageComboLack> column = new ProductBrowseColumn<>();
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.originalArtNum");
                }
            });
            tableView.getColumns().add(column);
        }
        {
            TableColumn<StorageComboLack, String> column = new TableColumn<>("Product Id");
            column.setPrefWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.artNumber");
                }
            });
            tableView.getColumns().add(column);
        }
        {
            TableColumn<StorageComboLack, String> column = new TableColumn<>("Name");
            column.setPrefWidth(300);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.shortName");
                }
            });

            tableView.getColumns().add(column);
        }
        {
            TableColumn<StorageComboLack, Double> column = new TableColumn<>("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboLack, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<StorageComboLack, Double> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.price");
                }
            });

            tableView.getColumns().add(column);
        }

        tableView.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                StorageComboLack item = tableView.getSelectionModel().getSelectedItem();
                if (item == null)
                    tableItemView.getItems().clear();
                else
                    tableItemView.setItems(FXCollections.observableList(item.storageComboLacks));
            }
        });


        HBox.setHgrow(tableView, Priority.ALWAYS);

        tableItemView = new TableView<>();
        {
            ProductBrowseColumn<StorageComboPartLack> column = new ProductBrowseColumn<>();
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboPartLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboPartLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.originalArtNum");
                }
            });
            tableItemView.getColumns().add(column);
        }
        {
            TableColumn<StorageComboPartLack, String> column = new TableColumn<>("Product Id");
            column.setPrefWidth(90);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboPartLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboPartLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.artNumber");
                }
            });
            tableItemView.getColumns().add(column);
        }

        {
            TableColumn<StorageComboPartLack, String> column = new TableColumn<>("Name");
            column.setPrefWidth(190);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboPartLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboPartLack, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.shortName");
                }
            });

            tableItemView.getColumns().add(column);
        }
        {
            TableColumn<StorageComboPartLack, Double> column = new TableColumn<>("Price");
            column.setMinWidth(30);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboPartLack, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<StorageComboPartLack, Double> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.price");
                }
            });

            tableItemView.getColumns().add(column);
        }

        {
            TableColumn<StorageComboPartLack, Double> column = new TableColumn<>("Lack Price");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboPartLack, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<StorageComboPartLack, Double> item) {
                    return new PathProperty<>(item.getValue(), "totalLackPrice");
                }
            });

            tableItemView.getColumns().add(column);
        }

        {
            TableColumn<StorageComboPartLack, String> column = new TableColumn<>("Need/Lack");
            column.setMinWidth(40);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StorageComboPartLack, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<StorageComboPartLack, String> item) {
                    return new SimpleStringProperty(item.getValue().getTotal() + "/" + item.getValue().getLackCount());
                }
            });

            tableItemView.getColumns().add(column);
        }


        HBox.setHgrow(tableItemView, Priority.ALWAYS);

        getChildren().addAll(tableView, tableItemView);
    }

    public void setItems(List<StorageComboLack> comboLacks) {
        tableView.setItems(FXCollections.observableList(comboLacks));
    }
}