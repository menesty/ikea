package org.menesty.ikea.ui.controls.dialog;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.lang.math.NumberUtils;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.exception.ProductFetchException;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.Toast;
import org.menesty.ikea.ui.controls.BaseEntityDialog;
import org.menesty.ikea.ui.controls.form.*;
import org.menesty.ikea.util.NumberUtil;

import java.math.BigDecimal;

public class RawInvoiceItemDialog extends BaseEntityDialog<RawInvoiceProductItem> {
    private RawInvoiceItemForm form;
    private BigDecimal priceMargin;

    public RawInvoiceItemDialog(Stage stage) {
        super(stage);
        setTitle("Create Invoice item");

        addRow(form = new RawInvoiceItemForm(), bottomBar);
        okBtn.setText("Save");
    }

    public void setPriceMargin(int priceMargin) {
        this.priceMargin = BigDecimal.valueOf((double) (priceMargin == 0 ? 2 : priceMargin) / 100 + 1);
    }

    private class RawInvoiceItemForm extends FormPane {
        private ProductIdField productIdField;

        private FormPane detailPane;

        private TextField name;

        private CheckBox ikeaProduct;

        private DoubleTextField price;

        private DoubleTextField count;

        private IntegerTextField wat;

        private boolean silent;

        private ProductInfo productInfo;

        public RawInvoiceItemForm() {
            final InvalidationListener invalidationListener = initListener();
            addRow("Ikea Product", ikeaProduct = new CheckBox());

            ikeaProduct.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                    invalidationListener.invalidated(null);
                }
            });

            add(productIdField = new ProductIdField("Product Id", false));

            productIdField.setChangeListener(invalidationListener);
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

            addRow(detailPane = new FormPane(), 2);

            detailPane.add(name = new TextField(null, "Name", false));
            detailPane.add(count = new DoubleTextField("Count"));
            detailPane.add(price = new DoubleTextField("Price"));
            detailPane.addRow("Wat", wat = new IntegerTextField());

        }

        private InvalidationListener initListener() {
            return new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (silent)
                        return;

                    if (ikeaProduct.isSelected()) {
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
                    } else {
                        detailPane.setDisable(false);
                        okBtn.setDisable(false);
                    }

                    productIdField.enableBrowse(ikeaProduct.isSelected());

                }
            };
        }

        @Override
        public void reset() {
            productInfo = null;
            super.reset();
            detailPane.reset();
            wat.setNumber(23);
            ikeaProduct.setSelected(true);
        }

        @Override
        public boolean isValid() {
            return super.isValid() && detailPane.isValid();
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

        boolean isIkeaProduct() {
            return ikeaProduct.isSelected();
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

    @Override
    protected RawInvoiceProductItem collect() {
        entityValue.setOriginalArtNumber(form.getProductId());

        double marginPrice = NumberUtil.round(BigDecimal.valueOf(form.getPrice()).multiply(priceMargin).doubleValue());
        entityValue.setPrice(marginPrice);
        entityValue.setBasePrice(form.getPrice());
        entityValue.setName(form.getName());
        entityValue.setCount(form.getCount());
        entityValue.setProductInfo(form.getProductInfo());
        entityValue.setWat(form.getWat() + "");
        entityValue.setSynthetic(!form.isIkeaProduct());
        return entityValue;
    }

    @Override
    protected void populate(RawInvoiceProductItem entityValue) {
        form.setProductId(entityValue.getOriginalArtNumber());
        form.setPrice(entityValue.getPrice());
        form.setName(entityValue.getName());
        form.setCount(entityValue.getCount());
        form.setProductInfo(entityValue.getProductInfo());
    }

    @Override
    public boolean isValid() {
        return form.isValid();
    }

    @Override
    public void reset() {
        form.reset();
    }
}
