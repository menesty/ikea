package org.menesty.ikea.domain;

import java.util.Date;

public class ParagonDto {
    private int id;

    private int driverId;

    private int counterPartyId;

    private Date createdDate;

    private int orderId;

    private double price;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getCounterPartyId() {
        return counterPartyId;
    }

    public void setCounterPartyId(int counterPartyId) {
        this.counterPartyId = counterPartyId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
