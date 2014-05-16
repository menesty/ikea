package org.menesty.ikea.domain;

import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;

import javax.persistence.*;
import java.math.BigDecimal;
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
        this(name, null);
    }

    public InvoicePdf(CustomerOrder customerOrder) {
        this(null, customerOrder);
    }

    public InvoicePdf(String name, CustomerOrder customerOrder) {
        setName(name);

        this.customerOrder = customerOrder;
        this.createdDate = new Date();
    }

    private double price;

    private String name;
    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    private String invoiceNumber;

    private String paragonName;
    @Temporal(TemporalType.DATE)
    private Date paragonDate;

    private boolean sync;

    @ManyToOne(fetch = FetchType.LAZY)
    public CustomerOrder customerOrder;

    public Date getCreatedDate() {
        return createdDate;
    }

    @OneToMany(mappedBy = "invoicePdf")
    private List<RawInvoiceProductItem> products;

    public String getParagonName() {
        return paragonName;
    }

    public void setParagonName(String paragonName) {
        this.paragonName = paragonName;
    }

    public Date getParagonDate() {
        return paragonDate;
    }

    public void setParagonDate(Date paragonDate) {
        this.paragonDate = paragonDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

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
        this.products = items;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public static BigDecimal getTotalPrice(List<RawInvoiceProductItem> items) {
        BigDecimal price = BigDecimal.ZERO;

        for (RawInvoiceProductItem item : items)
            price = price.add(BigDecimal.valueOf(item.getTotal()));

        return price;

    }
}