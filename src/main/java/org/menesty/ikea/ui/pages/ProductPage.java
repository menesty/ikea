package org.menesty.ikea.ui.pages;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.ProductService;
import org.menesty.ikea.ui.controls.MToolBar;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;
import org.menesty.ikea.ui.controls.search.ProductItemSearchBar;
import org.menesty.ikea.ui.controls.search.ProductItemSearchData;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;
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
        super(Pages.PRODUCTS.getTitle());
    }

    @Override
    protected void initialize() {
        productService = new ProductService();
    }

    @Override
    public Node createView() {
        tableView = new BaseTableView<ProductInfo>() {
            @Override
            protected void onRowDoubleClick(final TableRow<ProductInfo> row) {
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

            @Override
            protected void onRowRender(TableRow<ProductInfo> row, ProductInfo newValue) {
                row.getStyleClass().remove("productNotVerified");

                if (newValue != null && !newValue.isVerified())
                    row.getStyleClass().add("productNotVerified");
            }
        };

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>("Art # ");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<ProductInfo, String>column("artNumber"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>("O Art #");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<ProductInfo, String>column("originalArtNum"));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(ColumnUtil.<ProductInfo, String>column("name"));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>("Short name");
            column.setMinWidth(200);
            column.setCellValueFactory(ColumnUtil.<ProductInfo, String>column("shortName"));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, String> column = new TableColumn<>("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<ProductInfo>number("price"));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, ProductInfo.Group> column = new TableColumn<>("Group");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<ProductInfo, ProductInfo.Group>column("group"));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ProductInfo, Integer> column = new TableColumn<>("B count");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<ProductInfo, Integer>column("packageInfo.boxCount"));

            tableView.getColumns().add(column);
        }

        //==============================================
        {
            TableColumn<ProductInfo, String> column = new TableColumn<>("Weight");
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
            TableColumn<ProductInfo, String> column = new TableColumn<>("Height");
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
            TableColumn<ProductInfo, String> column = new TableColumn<>("Width");
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
            TableColumn<ProductInfo, String> column = new TableColumn<>("Length");
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

        filter(new ProductItemSearchData());

        ProductItemSearchBar searchBar = new ProductItemSearchBar() {
            @Override
            public void onSearch(ProductItemSearchData data) {
                filter(data);
            }
        };

        VBox toolBarBox = new VBox();
        ToolBar toolBar = new MToolBar();

        Button export = new Button(null, ImageFactory.createCsv32Icon());
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

        Button importBtn = new Button(null, ImageFactory.createImport32Icon());
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

        toolBar.getItems().addAll(export, importBtn);

        toolBarBox.getChildren().addAll(toolBar, searchBar);
        BorderPane container = new BorderPane();

        container.setTop(toolBarBox);
        container.setCenter(tableView);

        productEditDialog = new ProductDialog(getStage());

        return wrap(container);
    }

    private void filter(ProductItemSearchData data) {
        tableView.setItems(FXCollections.observableArrayList(productService.load(data)));
    }
}
