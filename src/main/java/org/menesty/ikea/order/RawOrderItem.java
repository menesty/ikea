package org.menesty.ikea.order;

import org.menesty.ikea.domain.ProductInfo;

/**
 * User: Menesty
 * Date: 9/22/13
 * Time: 9:49 AM
 */
public class RawOrderItem {

    private Integer count;

    private String artNumber;

    private String description;

    private String combo;

    private String comment;

    private Double price;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getArtNumber() {
        return artNumber;
    }

    public void setArtNumber(String artNumber) {
        this.artNumber = artNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCombo() {
        return combo;
    }

    public void setCombo(String combo) {
        this.combo = combo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
