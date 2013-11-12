package org.menesty.ikea.ui.controls.search;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.ui.controls.form.TextField;


/**
 * User: Menesty
 * Date: 10/31/13
 * Time: 10:57 PM
 */
public class OrderItemSearchBar extends ToolBar {

    private ComboBox<OrderItem.Type> type;

    private TextField artNumber;

    private ComboBox<ProductInfo.Group> productGroup;

    private CheckBox pum;

    public OrderItemSearchBar() {


        InvalidationListener invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                applyFilter();
            }
        };

        type = new ComboBox<>();
        type.setPromptText("Type");
        type.getSelectionModel().selectedItemProperty().addListener(invalidationListener);
        type.getItems().add(null);
        type.getItems().addAll(OrderItem.Type.values());

        productGroup = new ComboBox<>();
        productGroup.getSelectionModel().selectedItemProperty().addListener(invalidationListener);
        productGroup.setPromptText("Product group");
        productGroup.getItems().add(null);
        productGroup.getItems().addAll(ProductInfo.Group.values());


        artNumber = new TextField();
        artNumber.setDelay(1);
        artNumber.setOnDelayAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                applyFilter();
            }
        });
        artNumber.setPromptText("Product ID #");



        Label pumLable = new Label("PUM");
        pumLable.setTooltip(new Tooltip("Price unmatched"));
        pum = new CheckBox();
        pum.selectedProperty().addListener(invalidationListener);

        Button clearButton = new Button("reset");
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                artNumber.setText(null);
                type.getSelectionModel().select(null);
                productGroup.getSelectionModel().select(null);
                pum.setSelected(false);
                onSearch(new OrderItemSearchData());
            }
        });

        getItems().addAll(artNumber, type, productGroup, pumLable, pum, clearButton);
    }

    private void applyFilter() {
        onSearch(collectData());
    }

    private OrderItemSearchData collectData() {
        OrderItemSearchData orderItemSearchForm = new OrderItemSearchData();
        orderItemSearchForm.type = type.getSelectionModel().getSelectedItem();
        orderItemSearchForm.artNumber = artNumber.getText();
        orderItemSearchForm.productGroup = productGroup.getSelectionModel().getSelectedItem();
        orderItemSearchForm.pum = pum.isSelected();
        return orderItemSearchForm;
    }

    public void onSearch(OrderItemSearchData orderItemSearchForm) {

    }


}

