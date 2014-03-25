package org.menesty.ikea.processor.invoice;

import org.menesty.ikea.domain.Identifiable;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.PackageInfo;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.util.NumberUtil;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 11:03 PM
 */
@Entity
public class RawInvoiceProductItem extends Identifiable {

    public RawInvoiceProductItem() {

    }

    public RawInvoiceProductItem(InvoicePdf invoicePdf) {
       this.invoicePdf = invoicePdf;
    }

    private String originalArtNumber;

    private String name;

    private double count;

    private double price;

    private String wat;
    @ManyToOne(fetch = FetchType.LAZY)
    public InvoicePdf invoicePdf;

    @ManyToOne
    private ProductInfo productInfo;

    private static final int MAX_TEXTILE_DIMENSION = 2050;

    private static final int MAX_TEXTILE_WEIGHT = 3500;

    private static final int MAX_DIMENSION = 450;

    private static final int MAX_WEIGHT = 3000;

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getTotal() {
        return BigDecimal.valueOf(getPrice()).multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    public String getWat() {
        return wat;
    }

    public int getIntWat() {
        return (int) NumberUtil.parse(wat, 23);
    }

    public void setWat(String wat) {
        this.wat = wat;
    }

    public String getOriginalArtNumber() {
        return originalArtNumber;
    }

    public void setOriginalArtNumber(String originalArtNumber) {
        this.originalArtNumber = originalArtNumber;
    }

    public String getPrepareArtNumber() {
        return ProductInfo.formatProductId(originalArtNumber);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return NumberUtil.round(price, 2);
    }

    public boolean isSeparate() {
        PackageInfo packageInfo = getProductInfo().getPackageInfo();
        if (ProductInfo.Group.Textile == getProductInfo().getGroup()) {
            if (packageInfo.getWeight() > MAX_TEXTILE_WEIGHT || packageInfo.getLength() > MAX_TEXTILE_DIMENSION || packageInfo.getWidth() > MAX_TEXTILE_DIMENSION || packageInfo.getHeight() > MAX_TEXTILE_DIMENSION)
                return true;
            return false;
        }
        if (packageInfo.getWeight() > MAX_WEIGHT || getPrice() * getCount() > 150 || (
                packageInfo.getLength() > MAX_DIMENSION || packageInfo.getWidth() > MAX_DIMENSION || packageInfo.getHeight() > MAX_DIMENSION
        ))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return getPrepareArtNumber() + ";" + name + ";" + count + ";" + wat + ";" + String.valueOf(getPrice()).replace(".", ",") + ";" + String.valueOf(getTotal()).replace(".", ",");
    }
}
