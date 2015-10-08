package org.menesty.ikea.service;

import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.OrderItem;
import org.menesty.ikea.domain.OrderShop;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.lib.domain.IkeaShop;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.ui.TaskProgressLog;
import org.menesty.ikea.ui.pages.ikea.order.export.IkeaExportService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 6/17/14.
 * 20:31.
 */
public class NewOrderFillService {

    public void fillOrder(CustomerOrder order, TaskProgressLog taskProgressLog) throws Exception {

        IkeaExportService exportService = ServiceFacade.getIkeaExportService();

        List<OrderItem> orderItems = ServiceFacade.getOrderItemService().loadBy(order);
        List<OrderItem> itemForExport = OrderItemService.getByType(orderItems, Arrays.asList(OrderItem.Type.General, OrderItem.Type.Combo));

        List<IkeaOrderItem> items = itemForExport.stream().map(orderItem -> {
            IkeaOrderItem ikeaOrderItem = new IkeaOrderItem();
            ikeaOrderItem.setCount(new BigDecimal(orderItem.getCount()));

            IkeaProduct ikeaProduct = new IkeaProduct();
            ikeaProduct.setArtNumber(orderItem.getArtNumber());

            Product.Group group = Product.Group.valueOf(orderItem.getProductInfo().getGroup().toString());

            ikeaProduct.setGroup(group);
            ikeaOrderItem.setProduct(ikeaProduct);
            return ikeaOrderItem;
        }).collect(Collectors.toList());

        List<String> users = new ArrayList<>();

        for (User user : order.getUsers()) {
            users.add(user.getLogin());
        }

        List<IkeaShop> shops = new ArrayList<>();

        for (OrderShop orderShop : order.getOrderShops()) {
            IkeaShop shop = new IkeaShop();

            shop.setName(orderShop.getIkeaShop().getName());
            shop.setShopId(orderShop.getIkeaShop().getShopId());

            shops.add(shop);
        }

        exportService.export(items, users, shops, true, taskProgressLog);
    }
}
