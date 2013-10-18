package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.controls.form.IntegerTextField;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 5:36 PM
 */
public class ProductDialog extends BaseDialog {

    private final ProductForm productForm;
    private final TableView<ProductPart> subProductTableView;

    private TextField artNumber;

    private TextField originalArtNumber;

    private TextField name;

    private TextField shortName;

    private DoubleTextField price;

    private ComboBox<ProductInfo.Group> group;

    private IntegerTextField weight;

    private IntegerTextField boxCount;
    private IntegerTextField height;
    private IntegerTextField width;
    private IntegerTextField length;
    private ProductInfo currentProductInfo;


    public ProductDialog() {
        getChildren().add(createTitle("Create new order from customer"));
        okBtn.setText("Save");

        TabPane options = new TabPane();
        options.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        options.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        Tab productMain = new Tab();
        productMain.setText("Product Info");
        productMain.setContent(productForm = new ProductForm());


        Tab packageInfoTab = new Tab();
        packageInfoTab.setText("Package Information");
        packageInfoTab.setContent(new PackageInfoForm());
        options.getTabs().addAll(productMain, packageInfoTab);


        subProductTableView = new TableView<>();
        subProductTableView.setMaxHeight(150);
        subProductTableView.setPrefHeight(150);
        {
            TableColumn<ProductPart, Integer> column = new TableColumn<>();
            column.setText("Count");
            column.setMinWidth(50);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductPart, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<ProductPart, Integer> item) {
                    return new PathProperty<>(item.getValue(), "count");
                }
            });

            subProductTableView.getColumns().add(column);
        }
        {
            TableColumn<ProductPart, String> column = new TableColumn<>();
            column.setText("Art # ");
            column.setMinWidth(100);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductPart, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductPart, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.artNumber");
                }
            });
            subProductTableView.getColumns().add(column);
        }
        {
            TableColumn<ProductPart, String> column = new TableColumn<>();
            column.setText("Name");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProductPart, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ProductPart, String> item) {
                    return new PathProperty<>(item.getValue(), "productInfo.name");
                }
            });

            subProductTableView.getColumns().add(column);
        }
        subProductTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProductPart>() {
            @Override
            public void changed(ObservableValue<? extends ProductPart> observableValue, ProductPart old, ProductPart newValue) {
                if (newValue != null)
                    bind(newValue);
            }
        });


        subProductTableView.setVisible(false);
        getChildren().addAll(options, subProductTableView, bottomBar);
    }

    private void bind(ProductPart productPart) {
        unbind();
        currentProductInfo = productPart.getProductInfo();
        bindProperties();
    }

    private class ProductForm extends FormPanel {
        public ProductForm() {
            addRow("Art Number", artNumber = new TextField());
            addRow("Original art Number", originalArtNumber = new TextField());
            addRow("Name", name = new TextField());
            addRow("Short name", shortName = new TextField());
            addRow("Price", price = new DoubleTextField());


            group = new ComboBox<>();
            group.setId("uneditable-combobox");
            group.setPromptText("Select group");
            group.setItems(FXCollections.observableArrayList(ProductInfo.Group.values()));
            group.setMinWidth(200);
            addRow("Group", group);

        }
    }

    private class PackageInfoForm extends FormPanel {
        public PackageInfoForm() {
            addRow("Box Count", boxCount = new IntegerTextField());
            addRow("Weight", weight = new IntegerTextField());
            addRow("Height", height = new IntegerTextField());
            addRow("Width", width = new IntegerTextField());
            addRow("Length", length = new IntegerTextField());
        }
    }

    public void bind(ProductInfo productInfo) {
        unbind();
        currentProductInfo = productInfo;
        bindProperties();

        if (productInfo.getParts() != null) {
            subProductTableView.setItems(FXCollections.observableArrayList(productInfo.getParts()));
            subProductTableView.getItems().add(0, new ProductPart(0, currentProductInfo));
        }
        subProductTableView.setVisible(productInfo.getParts() != null);
    }

    private void bindProperties() {
        artNumber.textProperty().bind(new PathProperty<ProductInfo, String>(currentProductInfo, "artNumber"));
        originalArtNumber.textProperty().bind(new PathProperty<ProductInfo, String>(currentProductInfo, "originalArtNum"));
        name.textProperty().bind(new PathProperty<ProductInfo, String>(currentProductInfo, "name"));
        shortName.textProperty().bind(new PathProperty<ProductInfo, String>(currentProductInfo, "shortName"));
        price.numberProperty().bind(new PathProperty<ProductInfo, Number>(currentProductInfo, "price"));
        group.getSelectionModel().select(currentProductInfo.getGroup());
        weight.numberProperty().bind(new PathProperty<ProductInfo, Number>(currentProductInfo, "packageInfo.weight"));
        boxCount.numberProperty().bind(new PathProperty<ProductInfo, Number>(currentProductInfo, "packageInfo.boxCount"));
        height.numberProperty().bind(new PathProperty<ProductInfo, Number>(currentProductInfo, "packageInfo.height"));
        width.numberProperty().bind(new PathProperty<ProductInfo, Number>(currentProductInfo, "packageInfo.width"));
        length.numberProperty().bind(new PathProperty<ProductInfo, Number>(currentProductInfo, "packageInfo.length"));
    }

    public void unbind() {
        artNumber.textProperty().unbind();
        originalArtNumber.textProperty().unbind();
        name.textProperty().unbind();
        shortName.textProperty().unbind();
        price.numberProperty().unbind();
        weight.numberProperty().unbind();
        boxCount.numberProperty().unbind();
        height.numberProperty().unbind();
        width.numberProperty().unbind();
        length.numberProperty().unbind();
    }

    public void onCancel() {
        unbind();
    }

    @Override
    public void onOk() {
        currentProductInfo.setGroup(group.getSelectionModel().getSelectedItem());
        currentProductInfo.setVerified(true);
        currentProductInfo.setArtNumber(artNumber.getText());
        currentProductInfo.setOriginalArtNum(originalArtNumber.getText());
        currentProductInfo.setName(name.getText());
        currentProductInfo.setShortName(shortName.getText());
        currentProductInfo.setPrice(price.getNumber());

        currentProductInfo.getPackageInfo().setWeight(weight.getNumber());
        currentProductInfo.getPackageInfo().setBoxCount(boxCount.getNumber());
        currentProductInfo.getPackageInfo().setHeight(height.getNumber());
        currentProductInfo.getPackageInfo().setWidth(width.getNumber());
        currentProductInfo.getPackageInfo().setLength(length.getNumber());

        unbind();
        onSave(currentProductInfo, subProductTableView.isVisible());
    }

    public void onSave(ProductInfo productInfo, boolean isCombo) {

    }
}
