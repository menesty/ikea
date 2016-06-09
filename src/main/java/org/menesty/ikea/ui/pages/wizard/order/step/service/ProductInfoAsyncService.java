package org.menesty.ikea.ui.pages.wizard.order.step.service;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.parse.RawItem;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.lib.dto.OrderItemDetails;
import org.menesty.ikea.lib.dto.ProductPriceMismatch;
import org.menesty.ikea.lib.service.IkeaProductService;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;

import java.util.List;

/**
 * Created by Menesty on
 * 9/10/15.
 * 18:04.
 */
public class ProductInfoAsyncService extends AbstractAsyncService<OrderItemDetails> {
    private SimpleObjectProperty<List<RawItem>> rawItemsProperty = new SimpleObjectProperty<>();
    protected StringProperty artNumberProperty = new SimpleStringProperty();
    protected IntegerProperty artNumberIndexProperty = new SimpleIntegerProperty();
    private IkeaProductService ikeaProductService;

    public ProductInfoAsyncService() {
        ikeaProductService = ServiceFacade.getIkeaProductService();
    }

    @Override
    protected Task<OrderItemDetails> createTask() {
        List<RawItem> _raRawItems = rawItemsProperty.get();

        return new Task<OrderItemDetails>() {
            @Override
            protected OrderItemDetails call() throws Exception {
                OrderItemDetails result = new OrderItemDetails();
                if (!_raRawItems.isEmpty()) {
                    for (RawItem rawItem : _raRawItems)
                        try {
                            Platform.runLater(() -> {
                                artNumberProperty.set(rawItem.getArtNumber());
                                artNumberIndexProperty.set(artNumberIndexProperty.get() + 1);
                            });

                            IkeaProduct product = ikeaProductService.getProduct(rawItem.getArtNumber(), throwable -> ServiceFacade.getErrorConsole().add(throwable));
                            result.add(new IkeaOrderItem(product, rawItem.getCount(), product.getPrice(), rawItem.getComment()));

                            if (!product.getPrice().equals(rawItem.getPrice())) {
                                result.add(new ProductPriceMismatch(rawItem.getArtNumber(), rawItem.getPrice(), product.getPrice(), rawItem.getCount()));
                            }

                        } catch (Exception e) {
                            result.addNotAvailable(rawItem.getArtNumber());
                        }

                }
                return result;
            }
        };
    }

    public void setRawItems(List<RawItem> items) {
        rawItemsProperty.set(items);
    }

    public StringProperty artNumberProperty() {
        return artNumberProperty;
    }

    public IntegerProperty artNumberIndexProperty() {
        return artNumberIndexProperty;
    }
}
