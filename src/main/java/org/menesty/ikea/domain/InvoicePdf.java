package org.menesty.ikea.domain;

import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * User: Menesty
 * Date: 10/13/13
 * Time: 12:05 AM
 */
@Entity
public class InvoicePdf extends Identifiable {

    public InvoicePdf() {

    }

    public InvoicePdf(String name) {
        setName(name);
        this.createdDate = new Date();
    }

    private double price;

    private String name;

    private Date createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    public CustomerOrder customerOrder;

    public Date getCreatedDate() {
        return createdDate;
    }

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "invoicePdf")
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

    public void setProducts(List<RawInvoiceProductItem> items) {
        for (RawInvoiceProductItem item : items)
            item.invoicePdf = this;

        this.products = items;
    }
}