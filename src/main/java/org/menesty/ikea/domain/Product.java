package org.menesty.ikea.domain;

import java.math.BigDecimal;

/**
 * User: Menesty
 * Date: 9/23/13
 * Time: 9:43 PM
 */
public class Product {
    private String generalArtNumber;

    private String ikeaArtNumber;

    private String name;

    private String shortName;

    private BigDecimal wat;

    private Price retailPrice;

    private Price wholesalePrice;

    private Price specialPrice;

    public String getGeneralArtNumber() {
        return generalArtNumber;
    }

    public void setGeneralArtNumber(String generalArtNumber) {
        this.generalArtNumber = generalArtNumber;
    }

    public String getIkeaArtNumber() {
        return ikeaArtNumber;
    }

    public void setIkeaArtNumber(String ikeaArtNumber) {
        this.ikeaArtNumber = ikeaArtNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public BigDecimal getWat() {
        return wat;
    }

    public void setWat(BigDecimal wat) {
        this.wat = wat;
    }

    public Price getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(Price retailPrice) {
        this.retailPrice = retailPrice;
    }

    public Price getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(Price wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public Price getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(Price specialPrice) {
        this.specialPrice = specialPrice;
    }
}

