package org.menesty.ikea.ui.pages;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.ProductService;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * User: Menesty
 * Date: 10/15/13
 * Time: 2:23 PM
 */
public class ProductPage extends BasePage {
    private ProductService productService;

    private ProductDialog productEditDialog;

    public ProductPage() {
        super("Products");
        this.productService = new ProductService();
    }

    @Override
    public Node createView() {
        TableView<ProductInfo> tableView = new TableView<>();
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    return new PathProperty<>(item.getValue(), "artNumber");
                }
            });
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("O Art #");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    return new PathProperty<>(item.getValue(), "originalArtNum");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    return new PathProperty<>(item.getValue(), "name");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Short name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    return new PathProperty<>(item.getValue(), "shortName");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, Double> column = new TableColumn<>();
            column.setText("Price");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<ProductInfo, Double> item) {
                    return new PathProperty<>(item.getValue(), "price");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, ProductInfo.Group> column = new TableColumn<>();
            column.setText("Group");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, ProductInfo.Group>, ObservableValue<ProductInfo.Group>>() {
                @Override
                public ObservableValue<ProductInfo.Group> call(TableColumn.CellDataFeatures<ProductInfo, ProductInfo.Group> item) {
                    return new PathProperty<>(item.getValue(), "group");
                }
            });

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, Integer> column = new TableColumn<>();
            column.setText("Box count");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ProductInfo, Integer> item) {
                    return new PathProperty<>(item.getValue(), "packageInfo.boxCount");
                }
            });

            tableView.getColumns().add(column);
        }

        //==============================================
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Weight");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    int value = item.getValue().getPackageInfo().getWeight();
                    return preparePackInfo(value, 1000, "kg");
                }
            });

            tableView.getColumns().add(column);
        }
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Height");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    int value = item.getValue().getPackageInfo().getHeight();
                    return preparePackInfo(value, 10, "cm");
                }
            });

            tableView.getColumns().add(column);
        }
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Width");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    int value = item.getValue().getPackageInfo().getWidth();
                    return preparePackInfo(value, 10, "cm");
                }
            });

            tableView.getColumns().add(column);
        }
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Length");
            column.setMinWidth(60);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductInfo, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductInfo, String> item) {
                    int value = item.getValue().getPackageInfo().getLength();
                    return preparePackInfo(value, 10, "cm");
                }
            });

            tableView.getColumns().add(column);
        }
        tableView.setRowFactory(new Callback<TableView<ProductInfo>, TableRow<ProductInfo>>() {
            @Override
            public TableRow<ProductInfo> call(final TableView<ProductInfo> tableView) {
                final TableRow<ProductInfo> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2) {
                            showPopupDialog(productEditDialog);
                            productEditDialog.bind(row.getItem());
                        }
                    }
                });
                row.itemProperty().addListener(new ChangeListener<ProductInfo>() {
                    @Override
                    public void changed(ObservableValue<? extends ProductInfo> observableValue, ProductInfo oldValue, ProductInfo newValue) {
                        if (newValue != null)
                            if (!newValue.isVerified())
                                row.getStyleClass().add("productNotVerified");
                            else
                                row.getStyleClass().remove("productNotVerified");
                    }
                });
                return row;
            }
        });

        tableView.setItems(FXCollections.observableArrayList(productService.load()));
        StackPane pane = createRoot();
        pane.getChildren().add(0, tableView);
        productEditDialog = new ProductDialog() {

            @Override
            public void onSave(ProductInfo productInfo, boolean isCombo) {
                productService.save(productInfo);
                if (!isCombo)
                    hidePopupDialog();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                hidePopupDialog();
            }
        };
        return pane;
    }

    private SimpleStringProperty preparePackInfo(int value, int dive, String prefix) {
        return new SimpleStringProperty((value != 0 ? BigDecimal.valueOf((double) value / dive).setScale(2, RoundingMode.CEILING).doubleValue() + "" : "0") + " " + prefix);
    }
}