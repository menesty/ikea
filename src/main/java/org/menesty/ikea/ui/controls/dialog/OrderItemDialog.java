package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.domain.OrderItem;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.controls.form.ProductIdField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

public class OrderItemDialog extends BaseDialog {
    private OrderItemForm orderItemForm;

    private OrderItem currentOrderItem;
    private EntityDialogCallback<OrderItem> callback;

    public OrderItemDialog() {
        getChildren().add(createTitle("Create/Edit OrderItem"));
        orderItemForm = new OrderItemForm();

        getChildren().addAll(orderItemForm, bottomBar);
        okBtn.setText("Save");
    }

    public void bind(OrderItem orderItem, EntityDialogCallback<OrderItem> callback) {
        currentOrderItem = orderItem;
        this.callback = callback;

        orderItemForm.reset();
        orderItemForm.setProductId(orderItem.getArtNumber());
        orderItemForm.setCount(orderItem.getCount());
        orderItemForm.setComment(orderItem.getComment());
        orderItemForm.setType(orderItem.getType());

    }

    @Override
    public void onOk() {
        if (currentOrderItem.getId() != null)
            currentOrderItem.setArtNumber(orderItemForm.getProductId());

        currentOrderItem.setCount(orderItemForm.getCount());
        currentOrderItem.setComment(orderItemForm.getComment());
        currentOrderItem.setType(orderItemForm.getType());

        if (callback != null)
            callback.onSave(currentOrderItem);
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }

    @Override
    public void onShow() {
        orderItemForm.focus();
    }

    class OrderItemForm extends FormPanel {
        ProductIdField productId;
        DoubleTextField count;
        ComboBox<OrderItem.Type> type;
        TextField comment;
        DoubleTextField price;
        TextField shortName;

        OrderItemForm() {
            addRow("Art Number", productId = new ProductIdField());
            productId.getField().focusedProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    productId.setInvalid(false);
                    if (!productId.getField().isFocused() && productId.isEditable()) {
                        ProductInfo productInfo;
                        try {
                            productInfo = ServiceFacade.getProductService().loadOrCreate(productId.getProductId());
                        } catch (Exception e) {
                            productInfo = null;
                        }

                        if (productInfo != null) {
                            if (ProductInfo.Group.Combo == productInfo.getGroup())
                                type.getSelectionModel().select(OrderItem.Type.Combo);

                            shortName.setText(productInfo.getShortName());
                            price.setNumber(productInfo.getPrice());
                        }

                        currentOrderItem.setProductInfo(productInfo);
                        productId.setInvalid(productInfo == null);

                    }

                    okBtn.setDisable(currentOrderItem == null || currentOrderItem.getProductInfo() == null);
                }
            });

            addRow("Count", count = new DoubleTextField());

            addRow("Type", type = new ComboBox<>());
            type.getItems().addAll(OrderItem.Type.values());

            addRow("Comment", comment = new TextField());
            addRow("Short name", shortName = new TextField());
            addRow("Price", price = new DoubleTextField());
            shortName.setDisable(true);
            price.setDisable(true);
        }

        public void reset() {
            shortName.setText(null);
            price.setNumber(0d);
        }

        public void setProductId(String productId) {
            this.productId.setProductId(productId);
        }

        public void setCount(double count) {
            this.count.setNumber(count);
        }

        public void setComment(String comment) {
            this.comment.setText(comment);
        }

        public void setType(OrderItem.Type type) {
            this.type.getSelectionModel().select(type);
        }

        public String getProductId() {
            return productId.getProductId();
        }

        public double getCount() {
            return count.getNumber();
        }

        public OrderItem.Type getType() {
            return type.getSelectionModel().getSelectedItem();
        }

        public String getComment() {
            return comment.getText();
        }


        public void focus() {
            productId.setEditable(StringUtils.isNotBlank(productId.getProductId()));
            productId.focus();
        }
    }
}
