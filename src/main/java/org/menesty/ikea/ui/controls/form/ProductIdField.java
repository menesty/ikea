package org.menesty.ikea.ui.controls.form;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;

import java.util.regex.Pattern;

public class ProductIdField extends HBox implements Field {
    private TextField productId;
    private ImageView imageView;
    private BooleanProperty validProperty;
    public static final Pattern VALIDATION_PATTERN = Pattern.compile("^(?i)S{0,1}\\d{3}\\.{0,1}\\d{3}\\.{0,1}\\d{2}$");

    public ProductIdField(String label, boolean allowBlank) {
        this();
        productId.setLabel(label);
        productId.setAllowBlank(allowBlank);
    }

    public ProductIdField() {
        imageView = ImageFactory.createWeb22Icon();
        imageView.setOnMouseClicked(mouseEvent -> ProductDialog.browse(productId.getText()));

        HBox.setMargin(imageView, new Insets(2, 2, 2, 2));

        productId = new TextField();
        HBox.setHgrow(productId, Priority.ALWAYS);
        getChildren().addAll(productId, imageView);

        validProperty = new SimpleBooleanProperty();

        productId.textProperty().addListener(event -> {
            validProperty.set(isValid());
        });

        setValidationPattern(VALIDATION_PATTERN);
    }

    public void setValidationPattern(Pattern pattern) {
        productId.setValidationPattern(pattern);
    }

    public void enableBrowse(boolean enable) {
        imageView.setVisible(enable);
    }

    public void setEditable(boolean editable) {
        productId.setDisable(editable);
    }

    public void setProductId(String productId) {
        this.productId.setText(productId);
    }

    public String getProductId() {
        return productId.getText();
    }

    public TextField getField() {
        return productId;
    }

    public boolean isEditable() {
        return !productId.isDisable();
    }

    public void setInvalid(boolean invalid) {
        productId.setValid(!invalid);

    }

    public void focus() {
        if (productId.isEditable() && !productId.isDisable())
            productId.requestFocus();
    }

    public void setChangeListener(InvalidationListener listener) {
        productId.textProperty().addListener(listener);
    }

    @Override
    public boolean isValid() {
        return productId.isValid();
    }

    @Override
    public void reset() {
        productId.setText(null);
    }

    @Override
    public String getLabel() {
        return productId.getLabel();
    }

    @Override
    public void setValid(boolean valid) {
        productId.setValid(valid);
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }

    public void setOnDelayAction(EventHandler<ActionEvent> onDelayAction) {
        productId.setOnDelayAction(onDelayAction);
    }

    public void setDelay(int sec) {
        productId.setDelay(sec);
    }
}
