package org.menesty.ikea.ui.pages.ikea.order.export;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 10/3/15.
 * 23:26.
 */
class StockItem {
    private final String artNumber;
    private final BigDecimal count;
    private final String key;

    StockItem(String artNumber, String key, BigDecimal count) {
        this.artNumber = artNumber;
        this.count = count;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getArtNumber() {
        return artNumber;
    }

    public BigDecimal getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockItem stockItem = (StockItem) o;

        return artNumber != null ? artNumber.equals(stockItem.artNumber) : stockItem.artNumber == null;
    }
}
