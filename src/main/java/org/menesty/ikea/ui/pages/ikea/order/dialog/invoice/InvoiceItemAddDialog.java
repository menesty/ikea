package org.menesty.ikea.ui.pages.ikea.order.dialog.invoice;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.ikea.invoice.InvoiceItem;
import org.menesty.ikea.lib.service.IkeaProductService;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.Toast;
import org.menesty.ikea.ui.controls.dialog.EntityDialog;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.controls.form.ProductIdField;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.pane.LoadingPane;

/**
 * Created by Menesty on
 * 10/8/15.
 * 21:10.
 */
public class InvoiceItemAddDialog extends EntityDialog<InvoiceItem> {
    private ProductLoadService productLoadService;

    public InvoiceItemAddDialog(Stage stage) {
        super(stage);
        setAllowAutoHide(false);

        LoadingPane loadingPane = new LoadingPane();

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(getEntityForm(), loadingPane);

        loadingPane.bindTask(getProductLoadService());
        addRow(stackPane, bottomBar);
    }

    private ProductLoadService getProductLoadService() {
        if (productLoadService == null) {
            productLoadService = new ProductLoadService();
        }

        return productLoadService;
    }

    @Override
    protected EntityForm<InvoiceItem> createForm() {
        return new InvoiceItemForm();
    }

    class InvoiceItemForm extends EntityForm<InvoiceItem> {
        private NumberTextField countField;
        private NumberTextField priceField;
        private NumberTextField watField;
        private TextField shortName;
        private ProductIdField productIdField;
        private CheckBox ikeaProduct;

        private IkeaProduct activeProduct;

        private FormPane detailPane;

        public InvoiceItemForm() {
            addRow(I18n.UA.getString(I18nKeys.IKEA_PRODUCT), ikeaProduct = new CheckBox());
            ikeaProduct.selectedProperty().addListener((observable, oldValue, newValue) -> {
                detailPane.setDisable(newValue);
                productIdField.enableBrowse(newValue);
                detailPane.reset();
                activeProduct = null;
                priceField.setDisable(false);

                if (newValue) {
                    productIdField.setValidationPattern(ProductIdField.VALIDATION_PATTERN);
                } else {
                    productIdField.setValidationPattern(null);
                }
            });

            add(productIdField = new ProductIdField(I18n.UA.getString(I18nKeys.ART_NUMBER), false));

            productIdField.setDelay(1);
            productIdField.setOnDelayAction(event -> {
                if (ikeaProduct.isSelected()) {
                    if (productIdField.isValid()) {
                        productLoadService.setArtNumber(productIdField.getProductId());
                        productLoadService.restart();
                    } else {
                        activeProduct = null;
                        priceField.setDisable(false);
                        detailPane.reset();
                        detailPane.setDisable(true);
                    }
                }
            });

            getProductLoadService().setOnSucceededListener(value -> {
                if (value == null) {
                    Toast.makeText(I18n.UA.getString(I18nKeys.PRODUCT_NOT_FOUND), Toast.DURATION_LONG).show(getStage());
                    priceField.setDisable(false);
                } else {
                    detailPane.setDisable(false);
                    shortName.setText(value.getShortName());
                    priceField.setNumber(value.getPrice());
                    priceField.setDisable(true);
                }

                activeProduct = value;
            });

            addRow(detailPane = new FormPane(), 2);

            detailPane.add(shortName = new TextField(null, I18n.UA.getString(I18nKeys.SHORT_NAME), false));
            detailPane.add(countField = new NumberTextField(null, I18n.UA.getString(I18nKeys.COUNT), false));
            detailPane.add(priceField = new NumberTextField(null, I18n.UA.getString(I18nKeys.PRICE), false));
            detailPane.add(watField = new NumberTextField(null, I18n.UA.getString(I18nKeys.WAT), false));
            watField.setAllowDouble(false);

            shortName.showCharCounter(true);
        }

        @Override
        protected InvoiceItem collect(InvoiceItem entity) {
            entity.setArtNumber(productIdField.getProductId());
            entity.setCount(countField.getNumber());
            entity.setWat(watField.getNumber());
            entity.setShortName(shortName.getText());
            entity.setPrice(priceField.getNumber());
            entity.setProduct(activeProduct);

            return entity;
        }

        @Override
        public boolean isValid() {
            boolean isValid = detailPane.isValid();
            return super.isValid() & isValid;
        }

        @Override
        protected void populate(InvoiceItem entity) {

        }

        @Override
        public void reset() {
            super.reset();
            ikeaProduct.setSelected(true);
        }
    }

    class ProductLoadService extends AbstractAsyncService<IkeaProduct> {
        private StringProperty artNumberProperty = new SimpleStringProperty();

        @Override
        protected Task<IkeaProduct> createTask() {
            final String _artNumber = artNumberProperty.get();

            return new Task<IkeaProduct>() {
                @Override
                protected IkeaProduct call() throws Exception {
                    IkeaProductService productService = ServiceFacade.getIkeaProductService();
                    IkeaProduct product = null;
                    try {
                        product = productService.getProduct(_artNumber, true, throwable -> ServiceFacade.getErrorConsole().add(throwable));
                    } catch (Exception e) {
                        //skip
                    }
                    return product;
                }
            };
        }

        public void setArtNumber(String artNumber) {
            artNumberProperty.set(artNumber);
        }
    }
}
