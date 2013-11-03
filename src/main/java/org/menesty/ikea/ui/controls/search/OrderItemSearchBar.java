package org.menesty.ikea.ui.controls.search;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;

/**
 * User: Menesty
 * Date: 10/31/13
 * Time: 10:57 PM
 */
public class OrderItemSearchBar extends ToolBar {

    private ComboBox<OrderItem.Type> type;

    private TextField artNumber;

    private ComboBox<ProductInfo.Group> productGroup;

    public OrderItemSearchBar() {
        type = new ComboBox<>();
        type.setPromptText("Select type");
        type.getItems().add(null);
        type.getItems().addAll(OrderItem.Type.values());

        productGroup = new ComboBox<>();
        productGroup.setPromptText("Select product group");
        productGroup.getItems().add(null);
        productGroup.getItems().addAll(ProductInfo.Group.values());

        artNumber = new TextField();
        artNumber.setPromptText("Product ID #");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                OrderItemSearchForm orderItemSearchForm = new OrderItemSearchForm();
                orderItemSearchForm.type = type.getSelectionModel().getSelectedItem();
                orderItemSearchForm.artNumber = artNumber.getText();
                orderItemSearchForm.productGroup = productGroup.getSelectionModel().getSelectedItem();
                onSearch(orderItemSearchForm);
            }
        });

        Button clearButton = new Button("reset");
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                artNumber.setText(null);
                type.getSelectionModel().select(null);
                onSearch(new OrderItemSearchForm());
            }
        });

        getItems().addAll(artNumber, type, productGroup, searchButton, clearButton);
    }

    public void onSearch(OrderItemSearchForm orderItemSearchForm) {

    }


}

