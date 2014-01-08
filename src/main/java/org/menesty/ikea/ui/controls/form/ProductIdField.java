package org.menesty.ikea.ui.controls.form;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;

public class ProductIdField extends HBox {
    private TextField productId;

    public ProductIdField() {
        ImageView imageView = ImageFactory.createWeb22Icon();
        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                ProductDialog.browse(productId.getText());

            }
        });
        HBox.setMargin(imageView, new Insets(2, 2, 2, 2));

        productId = new TextField();
        HBox.setHgrow(productId, Priority.ALWAYS);
        getChildren().addAll(productId, imageView);
    }

    public void setEditable(boolean editable) {
        productId.setEditable(editable);
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
        return productId.isEditable();
    }
}
