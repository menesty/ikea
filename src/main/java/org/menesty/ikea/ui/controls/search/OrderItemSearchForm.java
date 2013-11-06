package org.menesty.ikea.ui.controls.search;

import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;

/**
 * User: Menesty
 * Date: 11/1/13
 * Time: 12:02 AM
 */
public class OrderItemSearchForm {

    public OrderItem.Type type;

    public String artNumber;

    public ProductInfo.Group productGroup;

    public boolean pum;
}
