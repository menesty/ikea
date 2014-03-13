package org.menesty.ikea.domain;

/**
 * Created by Menesty on 12/26/13.
 */
public class WarehouseItemDto {

    public String productNumber;

    public String shortName;

    public double price;

    public double count;

    public boolean visible;

    public boolean zestav;

    public double weight;

    public boolean allowed;

    public int orderId;

    public String productId;

    public String getProductNumber(){
        return productNumber;
    }

    public double getPrice(){
        return price;
    }

    public String getShortName(){
        return shortName;
    }
    public double getCount(){
        return count;
    }
}
