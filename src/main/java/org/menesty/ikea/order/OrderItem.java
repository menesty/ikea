package org.menesty.ikea.order;

/**
 * User: Menesty
 * Date: 9/22/13
 * Time: 11:24 AM
 */
public class OrderItem {
    private int count;
    private String artNumber;
    private String name;
    private Double price;
    private String comment;
    private String group;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public Double getTotal() {
        return (double) count * price;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItem orderItem = (OrderItem) o;

        if (!artNumber.equals(orderItem.artNumber)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return artNumber.hashCode();
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
