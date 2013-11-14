package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.controls.form.IntegerTextField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


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
    private EntityDialogCallback callback;


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
        currentProductInfo = productPart.getProductInfo();
        bindProperties();
    }

    private class ProductForm extends FormPanel {
        public ProductForm() {

            ImageView imageView = new ImageView(new Image("/styles/images/icon/web-22x22.png"));
            imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    try {
                        URI uri = new URI("http://www.ikea.com/pl/pl/catalog/products/" + currentProductInfo.getOriginalArtNum());
                        Desktop.getDesktop().browse(uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            HBox.setMargin(imageView, new Insets(2, 2, 2, 2));

            artNumber = new TextField();
            HBox.setHgrow(artNumber, Priority.ALWAYS);

            addRow("Art Number", new HBox(artNumber, imageView));
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

    public void bind(ProductInfo productInfo, EntityDialogCallback callback) {
        this.callback = callback;
        currentProductInfo = productInfo;
        bindProperties();

        if (productInfo.getParts() != null) {
            subProductTableView.setItems(FXCollections.observableArrayList(productInfo.getParts()));
            subProductTableView.getItems().add(0, new ProductPart(0, currentProductInfo));
        }
        subProductTableView.setVisible(productInfo.getParts() != null);
    }

    private void bindProperties() {
        artNumber.setText(currentProductInfo.getArtNumber());
        originalArtNumber.setText(currentProductInfo.getOriginalArtNum());
        name.setText(currentProductInfo.getName());
        shortName.setText(currentProductInfo.getShortName());
        price.setNumber(currentProductInfo.getPrice());
        group.getSelectionModel().select(currentProductInfo.getGroup());
        weight.setNumber(currentProductInfo.getPackageInfo().getWeight());
        boxCount.setNumber(currentProductInfo.getPackageInfo().getBoxCount());
        height.setNumber(currentProductInfo.getPackageInfo().getHeight());
        width.setNumber(currentProductInfo.getPackageInfo().getWidth());
        length.setNumber(currentProductInfo.getPackageInfo().getLength());
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
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

        onSave(currentProductInfo, subProductTableView.isVisible());
    }

    public void onSave(ProductInfo productInfo, boolean isCombo) {
        if (callback != null)
            callback.onSave(productInfo, isCombo);
    }
}
