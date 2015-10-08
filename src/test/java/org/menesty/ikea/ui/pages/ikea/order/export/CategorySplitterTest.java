package org.menesty.ikea.ui.pages.ikea.order.export;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Menesty on
 * 10/3/15.
 * 23:26.
 */
public class CategorySplitterTest {
    @Test
    public void testItemSplit() {
        CategorySplitter categorySplitter = new CategorySplitter(BigDecimal.TEN);

        categorySplitter.add(new StockItem("art-num-1", "key", new BigDecimal(32)));

        List<List<StockItem>> result = categorySplitter.getResult();
        assertEquals(4, result.size());
    }

    @Test
    public void testAutoExpand() {
        CategorySplitter categorySplitter = new CategorySplitter(BigDecimal.ONE);

        categorySplitter.add(new StockItem("art-num-1", "key", new BigDecimal(2)));
        categorySplitter.add(new StockItem("art-num-2", "key", new BigDecimal(1)));

        List<List<StockItem>> result = categorySplitter.getResult();
        assertEquals(3, result.size());

        assertEquals(1, result.get(0).size());
        assertEquals("art-num-1", result.get(0).get(0).getArtNumber());

        assertEquals(1, result.get(1).size());
        assertEquals("art-num-1", result.get(1).get(0).getArtNumber());

        assertEquals(1, result.get(2).size());
        assertEquals("art-num-2", result.get(2).get(0).getArtNumber());
    }

}