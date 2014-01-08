package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.controls.form.IntegerTextField;
import org.menesty.ikea.ui.controls.form.ProductIdField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.menesty.ikea.util.NumberUtil.*;


/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 5:36 PM
 */
public class ProductDialog extends BaseDialog {

    private final ProductForm productForm;

    private final PackageInfoForm packageInfoForm;

    private final TableView<ProductPart> subProductTableView;


    private ProductInfo currentProductInfo;

    private EntityDialogCallback<ProductInfo> callback;


    private final TabPane options;


    public ProductDialog() {
        getChildren().add(createTitle("Create new order from customer"));
        okBtn.setText("Save");

        options = new TabPane();
        options.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        options.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab productMain = new Tab();
        productMain.setText("Product Info");
        productMain.setContent(productForm = new ProductForm());

        Tab packageInfoTab = new Tab();
        packageInfoTab.setText("Package Information");
        packageInfoForm = new PackageInfoForm();
        packageInfoTab.setContent(packageInfoForm.getForm());
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

        getChildren().addAll(options, bottomBar);
    }

    private void bind(ProductPart productPart) {
        currentProductInfo = productPart.getProductInfo();
        bindProperties();
    }

    private class ProductForm extends FormPanel {

        private Label shorNameCount;

        private ProductIdField productIdField;

        private TextField originalArtNumber;

        private TextField name;

        private TextField shortName;

        private DoubleTextField price;

        private ComboBox<ProductInfo.Group> group;

        private TextField uaName;

        public ProductForm() {


            addRow("Art Number", productIdField = new ProductIdField());
            addRow("O Art Number", originalArtNumber = new TextField());

            originalArtNumber.setEditable(false);
            productIdField.setEditable(false);

            addRow("Name", name = new TextField());

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.BASELINE_CENTER);
            shortName = new TextField();
            shortName.textProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    shorNameCount.setText(shortName.getText().length() + "");
                }
            });
            HBox.setHgrow(shortName, Priority.ALWAYS);
            hbox.getChildren().addAll(shortName, shorNameCount = new Label());
            shorNameCount.setMaxWidth(20);
            HBox.setMargin(shorNameCount, new Insets(0, 0, 0, 2));


            addRow("Short name", hbox);
            addRow("UA name", uaName = new TextField());
            addRow("Price", price = new DoubleTextField());


            group = new ComboBox<>();
            group.setId("uneditable-combobox");
            group.setPromptText("Select group");
            group.setItems(FXCollections.observableArrayList(ProductInfo.Group.values()));
            group.setMinWidth(200);
            addRow("Group", group);

        }

        public String getName() {
            return name.getText();
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setArtNumber(String artNumber) {
            this.productIdField.setProductId(artNumber);
        }

        public void setOriginalArtNumber(String originalArtNumber) {
            this.originalArtNumber.setText(originalArtNumber);
        }

        public void setShortName(String shortName) {
            this.shortName.setText(shortName);
            shorNameCount.setText(shortName.length() + "");
        }

        public String getShortName() {
            return shortName.getText();
        }

        public void setPrice(double price) {
            this.price.setNumber(price);
        }

        public double getPrice() {
            return price.getNumber();
        }

        public void setGroup(ProductInfo.Group group) {
            this.group.getSelectionModel().select(group);
        }

        public ProductInfo.Group getGroup() {
            return group.getSelectionModel().getSelectedItem();
        }

        public void setUaName(String uaName) {
            this.uaName.setText(uaName);
        }

        public String getUaName() {
            return uaName.getText();
        }

    }

    private class PackageInfoForm {


        private FormPanel formPanel;
        private DoubleTextField weight;
        private IntegerTextField boxCount;
        private DoubleTextField height;
        private DoubleTextField width;
        private DoubleTextField length;

        public PackageInfoForm() {
            formPanel = new FormPanel();
            formPanel.addRow("Box Count", boxCount = new IntegerTextField());
            formPanel.addRow("Weight (kg)", weight = new DoubleTextField());
            formPanel.addRow("Height (cm)", height = new DoubleTextField());
            formPanel.addRow("Width (cm)", width = new DoubleTextField());
            formPanel.addRow("Length (cm)", length = new DoubleTextField());
        }

        public FormPanel getForm() {
            return formPanel;
        }

        public void setWeight(int weight) {
            this.weight.setNumber(convertToKg(weight));
        }

        public int getWeight() {
            return convertToGr(weight.getNumber());
        }

        public void setBoxCount(int boxCount) {
            this.boxCount.setNumber(boxCount);
        }

        public int getBoxCount() {
            return boxCount.getNumber();
        }

        public void setHeight(int height) {
            this.height.setNumber(convertToCm(height));
        }

        public int getHeight() {
            return convertToMm(height.getNumber());
        }

        public void setWidth(int width) {
            this.width.setNumber(convertToCm(width));
        }

        public int getWidth() {
            return convertToMm(width.getNumber());
        }

        public void setLength(int length) {
            this.length.setNumber(convertToCm(length));
        }

        public int getLength() {
            return convertToMm(length.getNumber());
        }
    }

    public void bind(ProductInfo productInfo, EntityDialogCallback<ProductInfo> callback) {
        this.callback = callback;
        currentProductInfo = productInfo;
        bindProperties();
        boolean hasParts = productInfo.getParts() != null && !productInfo.getParts().isEmpty();

        if (hasParts) {
            subProductTableView.setItems(FXCollections.observableArrayList(productInfo.getParts()));
            subProductTableView.getItems().add(0, new ProductPart(0, currentProductInfo));
            getChildren().add(getChildren().size() - 1, subProductTableView);
        } else {
            getChildren().remove(subProductTableView);
        }
        subProductTableView.setVisible(hasParts);
        options.getSelectionModel().select(options.getTabs().get(0));
    }

    private void bindProperties() {
        productForm.setArtNumber(currentProductInfo.getArtNumber());
        productForm.setOriginalArtNumber(currentProductInfo.getOriginalArtNum());
        productForm.setName(currentProductInfo.getName());
        productForm.setShortName(currentProductInfo.getShortName());
        productForm.setPrice(currentProductInfo.getPrice());
        productForm.setGroup(currentProductInfo.getGroup());

        if (currentProductInfo.getUaName() == null || currentProductInfo.getUaName().trim().length() == 0)
            productForm.setUaName(currentProductInfo.getArtNumber());
        else
            productForm.setUaName(currentProductInfo.getUaName());

        packageInfoForm.setWeight(currentProductInfo.getPackageInfo().getWeight());
        packageInfoForm.setBoxCount(currentProductInfo.getPackageInfo().getBoxCount());
        packageInfoForm.setHeight(currentProductInfo.getPackageInfo().getHeight());
        packageInfoForm.setWidth(currentProductInfo.getPackageInfo().getWidth());
        packageInfoForm.setLength(currentProductInfo.getPackageInfo().getLength());
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    @Override
    public void onOk() {
        currentProductInfo.setGroup(productForm.getGroup());
        currentProductInfo.setVerified(true);
        currentProductInfo.setName(productForm.getName());
        currentProductInfo.setShortName(productForm.getShortName());
        currentProductInfo.setPrice(productForm.getPrice());
        currentProductInfo.setUaName(productForm.getUaName());

        currentProductInfo.getPackageInfo().setWeight(packageInfoForm.getWeight());
        currentProductInfo.getPackageInfo().setBoxCount(packageInfoForm.getBoxCount());
        currentProductInfo.getPackageInfo().setHeight(packageInfoForm.getHeight());
        currentProductInfo.getPackageInfo().setWidth(packageInfoForm.getWidth());
        currentProductInfo.getPackageInfo().setLength(packageInfoForm.getLength());

        onSave(currentProductInfo, subProductTableView.isVisible());
    }

    public void onSave(ProductInfo productInfo, boolean isCombo) {
        if (callback != null)
            callback.onSave(productInfo, isCombo);
    }

    public static void browse(String artNumber) {
        try {
            URI uri = new URI("http://www.ikea.com/pl/pl/catalog/products/" + artNumber);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}