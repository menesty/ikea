package org.menesty.ikea.processor.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.menesty.ikea.domain.ProductInfo;

public class InvoiceItem {

    private int index;

    private String artNumber;

    private String name;

    private double price;

    private String shortName;

    private int wat;

    private int count;

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

    public double getPriceWat() {
        return round(price);
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
        return round(getRetail() * 1.23);
    }

    public double getPrice() {
        return round(price / ((double) wat / (double) 100 + 1));
    }

    public double getTaxPay() {
        return round((getPriceWat() - getPrice()) * count);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    public static String format(double value) {
        String valueStr = value + "";
        int pos = valueStr.length() - (valueStr.indexOf(".") + 1);
        for (int i = 4 - pos; i > 0; i--)
            valueStr += "0";
        return valueStr;
    }

    public static void main(String... arg) {
        format(23d);
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public static List<InvoiceItem> get(ProductInfo productInfo, int count) {
        List<InvoiceItem> result = new ArrayList<>();
        if (productInfo.getNumberBox() > 1)
            for (int i = 1; i <= productInfo.getNumberBox(); i++)
                result.add(InvoiceItem.get(productInfo, count, i, productInfo.getNumberBox()));
        else
            result.add(InvoiceItem.get(productInfo, count, 1, 1));
        return result;

    }

    public static InvoiceItem get(ProductInfo productInfo, int count, int box, int boxes) {
        InvoiceItem invoiceItem = new InvoiceItem();

        invoiceItem.name = productInfo.getName();
        invoiceItem.artNumber = "IKEA_" + productInfo.getArtNumber().replaceAll("-", "");
        invoiceItem.shortName = productInfo.getShortName();
        if (boxes > 1) {
            invoiceItem.artNumber += "(" + box + ")";
            invoiceItem.shortName += " " + box + "/" + boxes;
        }
        invoiceItem.price = productInfo.getPrice();
        invoiceItem.wat = 23;
        invoiceItem.count = count;

        return invoiceItem;
    }
}
