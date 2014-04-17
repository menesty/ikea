package org.menesty.ikea.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.menesty.ikea.db.DatabaseTestCase;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.ProductPart;

import java.io.IOException;
import java.util.List;

/**
 * Created by Menesty on 2/21/14.
 */
public class ProductServiceTest extends DatabaseTestCase {

    @Ignore
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
    @Ignore
    @Test
    public void comboPartParseTest() throws IOException {
        ProductService productService = new ProductService();

        ProductInfo productInfo = productService.loadComboProduct("S59932698");

        Assert.assertEquals("S599-326-98", productInfo.getArtNumber());
        Assert.assertEquals("S59932698", productInfo.getOriginalArtNum());
        Assert.assertEquals(ProductInfo.Group.Combo, productInfo.getGroup());
        Assert.assertEquals(3, productInfo.getParts().size());

        List<ProductPart> parts = productInfo.getParts();
        ProductPart commod = parts.get(0);

        Assert.assertEquals(2, commod.getCount());

    }
}
