package org.menesty.ikea.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.menesty.ikea.db.DatabaseTestCase;
import org.menesty.ikea.processor.invoice.InvoiceItem;

import java.io.File;
import java.util.Arrays;

public class InvoiceServiceTest extends DatabaseTestCase {
    @Ignore
    @Test
    public void exportToEppTest() {
        InvoiceService invoiceService = new InvoiceService();

        InvoiceItem item = new InvoiceItem();
        item.setArtNumber("IKEA_1");
        item.setPrice(10);
        item.setCount(2);

        File path = new File("test.epp");

        if (path.exists())
            path.delete();

        //invoiceService.exportToEpp("bla", Arrays.asList(item), path.getAbsolutePath());

        Assert.assertTrue(path.exists());

        path.delete();
    }
}
