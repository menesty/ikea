package org.menesty.ikea.service;

import org.junit.Assert;
import org.junit.Test;
import org.menesty.ikea.db.DatabaseTestCase;
import org.menesty.ikea.domain.ProductInfo;

import java.io.IOException;

/**
 * Created by Menesty on 2/21/14.
 */
public class ProductServiceTest extends DatabaseTestCase {

    @Test
    public void comboLoadTest() throws IOException {
        ProductService productService = new ProductService();

        ProductInfo productInfo = productService.loadComboProduct("S49004841");

        Assert.assertEquals("S490-048-41", productInfo.getArtNumber());
        Assert.assertEquals("S49004841", productInfo.getOriginalArtNum());
        Assert.assertEquals("Kombinacja regałowa z drzw/szuf, biały", productInfo.getName());
        Assert.assertEquals("Kombinacja regałowa z drzw/szuf, biały", productInfo.getShortName());
        Assert.assertEquals(ProductInfo.Group.Combo, productInfo.getGroup());
        Assert.assertEquals(410.0, productInfo.getPrice(), 0);
        Assert.assertEquals(5, productInfo.getParts().size());
    }
}
