package org.menesty.ikea.ui.pages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.service.ProductService;
import org.menesty.ikea.ui.controls.MToolBar;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.search.ProductItemSearchBar;
import org.menesty.ikea.ui.controls.search.ProductItemSearchData;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;

/**
 * User: Menesty
 * Date: 10/15/13
 * Time: 2:23 PM
 */
public class ProductPage extends BasePage {
    private ProductService productService;

    private ProductDialog productEditDialog;

    private TableView<ProductInfo> tableView;

    public ProductPage() {
        super("Products");
        this.productService = new ProductService();
    }

    @Override
    public Node createView() {
        tableView = new TableView<>();
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>();
            column.setText("Art # ");
            column.setMinWidth(80);
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
            column.setMinWidth(80);
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
            column.setMinWidth(60);
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
            column.setText("B count");
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
                    return NumberUtil.preparePackInfo(value, 1000, "kg");
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
                    return NumberUtil.preparePackInfo(value, 10, "cm");
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
                    return NumberUtil.preparePackInfo(value, 10, "cm");
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
                    return NumberUtil.preparePackInfo(value, 10, "cm");
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
                            productEditDialog.bind(row.getItem(), new EntityDialogCallback<ProductInfo>() {
                                @Override
                                public void onSave(ProductInfo productInfo, Object[] params) {
                                    productService.save(productInfo);
                                    if (!(Boolean) params[0])
                                        hidePopupDialog();
                                    row.setItem(null);
                                }

                                @Override
                                public void onCancel() {
                                    hidePopupDialog();
                                }
                            });
                        }
                    }
                });
                row.itemProperty().addListener(new ChangeListener<ProductInfo>() {
                    @Override
                    public void changed(ObservableValue<? extends ProductInfo> observableValue, ProductInfo oldValue, ProductInfo newValue) {
                        row.getStyleClass().remove("productNotVerified");
                        if (newValue != null && !newValue.isVerified())
                            row.getStyleClass().add("productNotVerified");

                    }
                });
                return row;
            }
        });

        filter(new ProductItemSearchData());

        ProductItemSearchBar searchBar = new ProductItemSearchBar() {
            @Override
            public void onSearch(ProductItemSearchData data) {
                filter(data);
            }
        };


        VBox toolBarBox = new VBox();
        ToolBar toolBar = new MToolBar();

        ImageView imageView = new ImageView(new Image("/styles/images/icon/csv-32x32.png"));
        Button export = new Button(null, imageView);
        export.setContentDisplay(ContentDisplay.RIGHT);
        export.setTooltip(new Tooltip("Export Filled products"));
        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Csv file (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(getStage());

                if (file != null)
                    productService.export(file.getAbsolutePath());
            }
        });

        imageView = new ImageView(new Image("/styles/images/icon/import-32x32.png"));
        Button importBtn = new Button(null, imageView);
        importBtn.setContentDisplay(ContentDisplay.RIGHT);
        importBtn.setTooltip(new Tooltip("Import/Update Products"));
        importBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Csv file (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(getStage());

                if (file != null)
                    productService.importProduct(file.getAbsolutePath());
            }
        });

        toolBar.getItems().addAll(export,importBtn);

        toolBarBox.getChildren().addAll(toolBar, searchBar);
        BorderPane container = new BorderPane();

        container.setTop(toolBarBox);
        container.setCenter(tableView);

        StackPane pane = createRoot();
        pane.getChildren().add(0, container);
        productEditDialog = new ProductDialog();
        return pane;
    }

    private void filter(ProductItemSearchData data) {
        tableView.setItems(FXCollections.observableArrayList(productService.load(data)));
    }



    @Override
    protected Node createIconContent() {
        return new ImageView(new javafx.scene.image.Image("/styles/images/icon/products-72x72.png"));
    }
}
