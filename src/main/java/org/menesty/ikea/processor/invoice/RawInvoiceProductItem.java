package org.menesty.ikea.processor.invoice;

import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.domain.Identifiable;
import org.menesty.ikea.domain.PackageInfo;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.util.NumberUtil;

import javax.persistence.Entity;
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

    private String originalArtNumber;

    private String artNumber;

    private String name;

    private double count;

    private String comment;

    private double price;

    private String wat;

    @ManyToOne
    private ProductInfo productInfo;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setPriceStr(String priceStr) {
        price = Double.valueOf(priceStr.trim().replaceAll("[\\s\\u00A0]+", "").replace(",", "."));
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

    public String getArtNumber() {
        return artNumber;
    }

    public String getPrepareArtNumber() {
        String artNumber = StringUtils.leftPad(this.artNumber.trim(), 8, '0');
        int lastPos = artNumber.length();
        artNumber = artNumber.substring(0, lastPos - 5) + "-" + artNumber.substring(lastPos - 5, lastPos - 2) + "-" + artNumber.substring(lastPos - 2, lastPos);
        return artNumber;
    }

    public void setArtNumber(String artNumber) {
        this.artNumber = artNumber;
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
            if (packageInfo.getWeight() > 3500 || packageInfo.getLength() > 2050 || packageInfo.getWidth() > 2050 || packageInfo.getHeight() > 2050)
                return true;
            return false;
        }
        if (packageInfo.getWeight() > 3000 || getPrice() * getCount() > 150 || (
                packageInfo.getLength() > 450 || packageInfo.getWidth() > 450 || packageInfo.getHeight() > 450
        ))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return artNumber + ";" + name + ";" + count + ";" + wat + ";" + String.valueOf(getPrice()).replace(".", ",") + ";" + String.valueOf(getTotal()).replace(".", ",");
    }
}
