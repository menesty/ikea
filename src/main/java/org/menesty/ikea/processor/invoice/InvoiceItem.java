package org.menesty.ikea.processor.invoice;

import org.menesty.ikea.domain.Identifiable;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.util.NumberUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.menesty.ikea.util.NumberUtil.convertToKg;

@Entity
public class InvoiceItem extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    public InvoicePdf invoicePdf;

    @Transient
    private int index;

    private String artNumber;

    private String originArtNumber;

    private String name;

    @Column(scale = 8, precision = 2)
    public double basePrice;

    @Column(scale = 8, precision = 2)
    private double price;

    private String shortName;

    private int wat;

    @Column(scale = 8, precision = 2)
    private double count;
    @Column(scale = 8, precision = 3)
    private double weight;

    private String size;

    private boolean zestav;

    private boolean visible;

    public String getOriginArtNumber() {
        return originArtNumber;
    }

    public void setOriginArtNumber(String originArtNumber) {
        this.originArtNumber = originArtNumber;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getArtNumber() {
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

    public boolean isZestav() {
        return zestav;
    }

    public InvoiceItem setZestav(boolean zestav) {
        this.zestav = zestav;
        return this;
    }

    public double getPriceWat() {
        return round(price);
    }

    public BigDecimal getTotalWatPrice() {
        return BigDecimal.valueOf(price).setScale(2, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(count).setScale(2, BigDecimal.ROUND_DOWN)).setScale(2, BigDecimal.ROUND_DOWN);
    }

    public double getPriceWatTotal() {
        return round(getPriceWat() * count);
    }

    public double getPriceTotal() {
        return round(getPrice() * count);
    }

    public double getRetail() {
        return round(getPrice() * 1.02);
    }

    public double getMarginPercent() {
        return round((getMargin() / getRetail()) * 100);
    }

    public double getMargin() {
        return round(getRetail() - getPrice());
    }

    public double getRetailWat() {
        return round(getRetail() * getWatCof());
    }

    public double getPrice() {
        return round(price / getWatCof());
    }

    public double getTaxPay() {
        return round((getPriceWat() - getPrice()) * count);
    }

    private double getWatCof() {
        return (double) wat / (double) 100 + 1;
    }

    private static double round(double value) {
        return NumberUtil.round(value);
    }

    public static String format(double value) {
        String valueStr = value + "";
        int pos = valueStr.length() - (valueStr.indexOf(".") + 1);

        for (int i = 4 - pos; i > 0; i--)
            valueStr += "0";

        return valueStr;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getWat() {
        return wat;
    }

    public void setWat(int wat) {
        this.wat = wat;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public static List<InvoiceItem> get(ProductInfo productInfo, String artSuffix, double count) {
        List<InvoiceItem> result = new ArrayList<>();
        boolean needGrind = productInfo.getPackageInfo().getBoxCount() > 1 && productInfo.getPackageInfo().getWeight() > 20000;

        if (needGrind)
            for (int i = 1; i <= productInfo.getPackageInfo().getBoxCount(); i++)
                result.add(InvoiceItem.get(productInfo, artSuffix, count, i, productInfo.getPackageInfo().getBoxCount()));
        else
            result.add(InvoiceItem.get(productInfo, artSuffix, count, 1, 1));

        if (needGrind) {
            double price = productInfo.getPrice();
            double pricePerItem = round(price / productInfo.getPackageInfo().getBoxCount());

            for (InvoiceItem item : result) {
                item.price = pricePerItem;
                item.basePrice = pricePerItem;
            }

            double total = round(pricePerItem * productInfo.getPackageInfo().getBoxCount());

            if (total != price)
                result.get(0).price = pricePerItem + (price - total);

        }

        return result;
    }


    public static InvoiceItem get(ProductInfo productInfo, String artSuffix, double count, int box, int boxes) {
        return get(productInfo.getOriginalArtNum(), artSuffix, productInfo.getName(), productInfo.getShortName(), productInfo.getPrice(), productInfo.getWat(), productInfo.getPackageInfo().size(), convertToKg(productInfo.getPackageInfo().getWeight()), count, box, boxes);
    }

    public static InvoiceItem get(String artNumber, String artSuffix, String name, String shortName, double price, int wat, String size, double weight, double count, int box, int boxes) {
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setVisible(true);
        invoiceItem.name = name;
        invoiceItem.artNumber = "IKEA_" + artNumber;
        invoiceItem.originArtNumber = artNumber;
        invoiceItem.shortName = shortName;
        invoiceItem.weight = weight;

        if (boxes > 1) {
            invoiceItem.artNumber += "(" + box + ")";
            invoiceItem.shortName += " " + box + "/" + boxes;
        }

        if (artSuffix != null && !artSuffix.isEmpty())
            invoiceItem.artNumber += "_" + artSuffix;

        invoiceItem.basePrice = price;
        invoiceItem.price = price;
        invoiceItem.wat = wat;
        invoiceItem.count = count;
        invoiceItem.size = size;

        return invoiceItem;
    }

}
