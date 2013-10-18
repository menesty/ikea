package org.menesty.ikea.domain;

import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;

import java.util.Date;
import java.util.List;

/**
 * User: Menesty
 * Date: 10/13/13
 * Time: 12:05 AM
 */
public class InvoicePdf {

    public InvoicePdf(String name) {
        setName(name);
        this.createdDate = new Date();
    }

    private double price;

    private String name;

    private Date createdDate;

    public Date getCreatedDate() {
        return createdDate;
    }

    private List<RawInvoiceProductItem> products;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RawInvoiceProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<RawInvoiceProductItem> products) {
        this.products = products;
    }
}