package org.menesty.ikea.ui.controls.dialog;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import org.menesty.ikea.domain.ProductInfo;
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


        getChildren().addAll(options, bottomBar);
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
        artNumber.textProperty().bindBidirectional(new PathProperty<ProductInfo, String>(productInfo, "artNumber"));
        originalArtNumber.textProperty().bindBidirectional(new PathProperty<ProductInfo, String>(productInfo, "originalArtNum"));
        name.textProperty().bindBidirectional(new PathProperty<ProductInfo, String>(productInfo, "name"));
        shortName.textProperty().bindBidirectional(new PathProperty<ProductInfo, String>(productInfo, "shortName"));
        price.numberProperty().bindBidirectional(new PathProperty<ProductInfo, Number>(productInfo, "price"));
        group.getSelectionModel().select(productInfo.getGroup());
        weight.numberProperty().bindBidirectional(new PathProperty<ProductInfo, Number>(productInfo, "packageInfo.weight"));
        boxCount.numberProperty().bindBidirectional(new PathProperty<ProductInfo, Number>(productInfo, "packageInfo.boxCount"));
        height.numberProperty().bindBidirectional(new PathProperty<ProductInfo, Number>(productInfo, "packageInfo.height"));
        width.numberProperty().bindBidirectional(new PathProperty<ProductInfo, Number>(productInfo, "packageInfo.width"));
        length.numberProperty().bindBidirectional(new PathProperty<ProductInfo, Number>(productInfo, "packageInfo.length"));

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
        unbind();
        System.out.println(currentProductInfo.toString());
    }
}
