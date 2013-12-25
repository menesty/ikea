package org.menesty.ikea.domain;

import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.ui.controls.search.OrderItemSearchData;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class CustomerOrder extends Identifiable {

    private String name;

    @OneToMany(mappedBy = "customerOrder", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    private Date createdDate;
    @ElementCollection
    private List<String> parseWarnings;

    @OneToMany(mappedBy = "customerOrder")
    private List<InvoicePdf> invoicePdfs;

    @ManyToOne
    private User generalUser;
    @ManyToOne
    private User comboUser;
    @ManyToOne
    private User lackUser;

    public boolean isOnline;

    public User getLackUser() {
        return lackUser;
    }

    public void setLackUser(User lackUser) {
        this.lackUser = lackUser;
    }

    public List<String> getParseWarnings() {
        return parseWarnings;
    }

    public void setParseWarnings(List<String> parseWarnings) {
        this.parseWarnings = parseWarnings;
    }

    public List<InvoicePdf> getInvoicePdfs() {
        return invoicePdfs;
    }

    public void setInvoicePdfs(List<InvoicePdf> invoicePdfs) {
        this.invoicePdfs = invoicePdfs;
    }

    public CustomerOrder() {
        parseWarnings = new ArrayList<>();
        orderItems = new ArrayList<>();
        invoicePdfs = new ArrayList<>();
    }

    public void addWarning(String message) {
        parseWarnings.add(message);
    }

    public CustomerOrder(String name, Date createdDate) {
        setName(name);
        setCreatedDate(createdDate);
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public List<OrderItem> getByType(OrderItem.Type type) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems)
            if (type == orderItem.getType())
                result.add(orderItem);

        return result;

    }

    public User getGeneralUser() {
        return generalUser;
    }

    public void setGeneralUser(User generalUser) {
        this.generalUser = generalUser;
    }

    public User getComboUser() {
        return comboUser;
    }

    public void setComboUser(User comboUser) {
        this.comboUser = comboUser;
    }

    public List<OrderItem> filterOrderItems(OrderItemSearchData orderItemSearchForm) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {

            ProductInfo productInfo = orderItem.getProductInfo();

            if (orderItemSearchForm.type != null && orderItemSearchForm.type != orderItem.getType())
                continue;
            if (StringUtils.isNotBlank(orderItemSearchForm.artNumber) && !orderItem.getArtNumber().contains(orderItemSearchForm.artNumber))
                continue;
            if (orderItemSearchForm.productGroup != null &&
                    (productInfo == null || !orderItemSearchForm.productGroup.equals(productInfo.getGroup())))
                continue;

            if (orderItemSearchForm.pum &&
                    (OrderItem.Type.Na == orderItem.getType() ||
                            (productInfo != null && orderItem.getPrice() == productInfo.getPrice())
                    ))
                continue;

            if (productInfo != null) {
                if (orderItemSearchForm.gei && !(productInfo.getPackageInfo().getWeight() > 3000 ||
                        productInfo.getPackageInfo().getLength() > 450 || productInfo.getPackageInfo().getWidth() > 450 || productInfo.getPackageInfo().getHeight() > 450
                ))
                    continue;

                if (orderItemSearchForm.ufd && productInfo.getPackageInfo().hasAllSize())
                    continue;
            }

            result.add(orderItem);
        }

        return result;
    }

    public void addInvoicePdfs(final List<InvoicePdf> entities) {
        invoicePdfs.addAll(entities);
    }
}
