package org.menesty.ikea.ui.pages.ikea.order.export;

import org.menesty.ikea.ui.pages.ikea.order.StockAvailability;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by Menesty on
 * 10/3/15.
 * 23:26.
 */
class ProductAvailabilityInfo {

    private Map<Integer, StockAvailability> stockAvailability;

    ProductAvailabilityInfo(Map<Integer, StockAvailability> stockAvailability) {
        this.stockAvailability = stockAvailability;
    }


    public BigDecimal getStockCount(int shopId) {
        StockAvailability stock = stockAvailability.get(shopId);

        if (stock != null)
            return stock.getAvailable().add(stock.getAvailable2()).add(stock.getAvailable3());

        return BigDecimal.ZERO;
    }
}
