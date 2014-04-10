package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang.math.NumberUtils;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.exception.ProductFetchException;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.Toast;
import org.menesty.ikea.ui.controls.form.DoubleTextField;
import org.menesty.ikea.ui.controls.form.IntegerTextField;
import org.menesty.ikea.ui.controls.form.ProductIdField;
import org.menesty.ikea.ui.layout.RowPanel;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

public class RawInvoiceItemDialog extends BaseDialog {
    private RawInvoiceProductItem currentItem;

    private RawInvoiceItemForm form;

    public RawInvoiceItemDialog() {
        setTitle("Create Invoice item");

        addRow(form = new RawInvoiceItemForm(), bottomBar);
        okBtn.setText("Save");
    }


    private EntityDialogCallback<RawInvoiceProductItem> callback;

    private class RawInvoiceItemForm extends RowPanel {
        private ProductIdField productIdField;

        private RowPanel detailPane;

        private TextField name;

        private DoubleTextField price;

        private DoubleTextField count;

        private IntegerTextField wat;

        private boolean silent;

        private ProductInfo productInfo;

        public RawInvoiceItemForm() {
            addRow("Product Id", productIdField = new ProductIdField());

            productIdField.setChangeListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (silent)
                        return;

                    if (productIdField.getProductId() != null && productIdField.getProductId().length() == 8) {
                        loadingPane.show();

                        ProductInfo productInfo = null;

                        try {
                            productInfo = ServiceFacade.getProductService().loadOrCreate(productIdField.getProductId());
                        } catch (ProductFetchException e) {
                            Toast.makeText("Product not found", Toast.DURATION_LONG).show(getStage());
                        }

                        if (productInfo != null) {
                            name.setText(productInfo.getName());
                            price.setNumber(productInfo.getPrice());
                            wat.setNumber(23);
                        }

                        RawInvoiceItemForm.this.productInfo = productInfo;

                        loadingPane.hide();

                    }

                    String productId = productIdField.getProductId();

                    if (productInfo == null || productId == null || productId.length() < 8) {
                        detailPane.setDisable(true);
                        okBtn.setDisable(true);
                    } else {
                        detailPane.setDisable(false);
                        okBtn.setDisable(false);
                    }

                }
            });
            productIdField.getField().addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
                public void handle(KeyEvent event) {
                    TextField field = productIdField.getField();
                    String newValue;

                    if (field.getText() == null)
                        newValue = event.getCharacter();
                    else
                        newValue = field.getText().substring(0, field.getSelection().getStart()) +
                                event.getCharacter() + field.getText()
                                .substring(field.getSelection().getEnd(), field.getText().length());

                    if (!NumberUtils.isNumber(newValue) || (field.getText() != null && field.getText().length() + 1 > 8))
                        event.consume();
                }
            });

            addRow(detailPane = new RowPanel(), 2);

            detailPane.addRow("Name", name = new TextField());
            detailPane.addRow("Count", count = new DoubleTextField());
            detailPane.addRow("Price", price = new DoubleTextField());
            detailPane.addRow("Wat", wat = new IntegerTextField());

        }

        public void reset() {
            productInfo = null;

            productIdField.setProductId(null);
            count.setNumber(0d);
            price.setNumber(0d);
            name.setText(null);
            wat.setNumber(23);

        }

        void setPrice(double price) {
            this.price.setNumber(price);
        }

        void setCount(double count) {
            this.count.setNumber(count);
        }

        void setName(String name) {
            this.name.setText(name);
        }

        void setProductId(String productId) {
            silent = true;
            this.productIdField.setProductId(productId);
            silent = false;
        }

        String getProductId() {
            return productIdField.getProductId();
        }

        double getPrice() {
            return this.price.getNumber();
        }

        double getCount() {
            return this.count.getNumber();
        }

        int getWat() {
            return wat.getNumber();
        }

        String getName() {
            return this.name.getText();
        }

        void setProductInfo(ProductInfo productInfo) {
            this.productInfo = productInfo;
            detailPane.setDisable(productInfo == null);
            okBtn.setDisable(productInfo == null);
        }

        ProductInfo getProductInfo() {
            return productInfo;
        }
    }

    public void bind(RawInvoiceProductItem item, EntityDialogCallback<RawInvoiceProductItem> callback) {
        currentItem = item;
        this.callback = callback;

        form.reset();

        form.setProductId(item.getOriginalArtNumber());
        form.setPrice(item.getPrice());
        form.setName(item.getName());
        form.setCount(item.getCount());
        form.setProductInfo(item.getProductInfo());
    }

    @Override
    public void onOk() {
        currentItem.setOriginalArtNumber(form.getProductId());
        currentItem.setPrice(form.getPrice());
        currentItem.setName(form.getName());
        currentItem.setCount(form.getCount());
        currentItem.setProductInfo(form.getProductInfo());
        currentItem.setWat(form.getWat() + "");

        if (callback != null)
            callback.onSave(currentItem);
    }

    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel();
    }
}
