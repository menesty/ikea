package org.menesty.ikea.ui.pages.ikea.order.export;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Menesty on
 * 10/3/15.
 * 23:25.
 */
public class CategorySplitter {
    private List<List<StockItem>> fullHolder;
    private List<List<StockItem>> activeHolder;
    private BigDecimal maxItemCount;

    public CategorySplitter(BigDecimal maxItemCount) {
        fullHolder = new LinkedList<>();
        activeHolder = new LinkedList<>();
        this.maxItemCount = maxItemCount;
    }

    public void add(StockItem stockItem) {
        BigDecimal needGroupCount = stockItem.getCount().divide(maxItemCount, 0, BigDecimal.ROUND_CEILING);

        if (activeHolder.size() < needGroupCount.intValue()) {
            int createGroups = needGroupCount.intValue() - activeHolder.size();

            for (int i = 0; i < createGroups; i++) {
                activeHolder.add(new ArrayList<>());
            }
        }

        BigDecimal totalCount = stockItem.getCount();

        for (int i = 0; i < needGroupCount.intValue(); i++) {
            StockItem slittedItem;

            if (totalCount.compareTo(maxItemCount) <= 0) {
                slittedItem = new StockItem(stockItem.getArtNumber(), stockItem.getKey(), totalCount);
            } else {
                slittedItem = new StockItem(stockItem.getArtNumber(), stockItem.getKey(), maxItemCount);
                totalCount = totalCount.subtract(maxItemCount);
            }

            activeHolder.get(i).add(slittedItem);
        }

        Iterator<List<StockItem>> iterator = activeHolder.iterator();

        while (iterator.hasNext()) {
            List<StockItem> cat = iterator.next();

            if (cat.size() == maxItemCount.intValue()) {
                fullHolder.add(cat);
                iterator.remove();
            }
        }
    }

    public List<List<StockItem>> getResult() {
        List<List<StockItem>> result = new LinkedList<>();

        if (!fullHolder.isEmpty()) {
            result.addAll(fullHolder);
        }

        if (!activeHolder.isEmpty()) {
            result.addAll(activeHolder);
        }

        return result;
    }

    public void addItems(List<StockItem> items) {
        items.forEach(this::add);
    }
}