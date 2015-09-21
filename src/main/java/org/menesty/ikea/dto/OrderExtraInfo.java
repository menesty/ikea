package org.menesty.ikea.dto;

import java.util.List;

/**
 * Created by Menesty on
 * 8/25/15.
 * 09:16.
 */
public class OrderExtraInfo {
    private List<OrderItemInfo> notAvailable;
    private List<OrderItemInfo> priceUnMatches;
    private List<String> filesName;

    public List<String> getFilesName() {
        return filesName;
    }

    public void setFilesName(List<String> filesName) {
        this.filesName = filesName;
    }

    public List<OrderItemInfo> getNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(List<OrderItemInfo> notAvailable) {
        this.notAvailable = notAvailable;
    }

    public List<OrderItemInfo> getPriceUnMatches() {
        return priceUnMatches;
    }

    public void setPriceUnMatches(List<OrderItemInfo> priceUnMatches) {
        this.priceUnMatches = priceUnMatches;
    }
}
