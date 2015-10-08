package org.menesty.ikea.ui.pages.ikea.order.export;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Menesty on
 * 10/4/15.
 * 20:36.
 */
public class IkeaExportServiceTest {
    @Test
    public void testSortCategories() {
        IkeaExportService ikeaExportService = new IkeaExportService();
        Map<String, List<StockItem>> unsorted = new HashMap<>();

        unsorted.put("cat1", Collections.singletonList(new StockItem("art-num-1", "key", BigDecimal.ONE)));
        unsorted.put("cat2", Arrays.asList(new StockItem("art-num-1", "key", BigDecimal.ONE), new StockItem("art-num-2", "key", BigDecimal.ONE)));

        Map<String, List<StockItem>> sorted = ikeaExportService.sortCategories(unsorted);

        Map.Entry<String, List<StockItem>> entry = sorted.entrySet().iterator().next();
        assertEquals("cat2", entry.getKey());
    }

}