package org.menesty.ikea.service;

import net.sf.jxls.reader.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.order.RawOrderItem;
import org.menesty.ikea.ui.TaskProgress;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderService {
    private static final Pattern artNumberPattern = Pattern.compile("\\w{0,}\\d+");

    private ProductService productService;

    public OrderService() {
        this.productService = new ProductService();
    }

    public Order createOrder(String name, InputStream is, TaskProgress taskProgress) {

        try {
            Order order = new Order();
            order.setName(name);

            InputStream inputXML = getClass().getResourceAsStream("/config/config.xml");
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes(true);

            List<RawOrderItem> rawOrderItems = new ArrayList<>();
            Map<String, Object> beans = new HashMap<>();
            beans.put("rawOrderItems", rawOrderItems);

            taskProgress.updateProgress(5, 100);
            XLSReadStatus readStatus = mainReader.read(is, beans);
            taskProgress.updateProgress(20, 100);

            for (XLSReadMessage message : (List<XLSReadMessage>) readStatus.getReadMessages())
                order.addWarning(message.getMessage());

            order.setOrderItems(reduce(rawOrderItems, taskProgress));

            return order;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<OrderItem> reduce(List<RawOrderItem> list, TaskProgress taskProgress) {
        List<OrderItem> result = new ArrayList<>();
        Map<String, OrderItem> reduceMap = new HashMap<>();

        int progressPercantage = 80;
        //load

        int itemIndex = 0;
        for (RawOrderItem rawOrderItem : list) {
            if (rawOrderItem.getArtNumber() == null) continue;

            String artNumber = getArtNumber(rawOrderItem.getArtNumber());
            if (!artNumber.isEmpty()) {

                OrderItem orderItem = reduceMap.get(artNumber);
                if (orderItem == null) {
                    orderItem = new OrderItem();
                    orderItem.setArtNumber(artNumber);
                    orderItem.setComment(rawOrderItem.getComment());
                    orderItem.setCount(rawOrderItem.getCount());
                    orderItem.setName(rawOrderItem.getDescription());
                    orderItem.setPrice(rawOrderItem.getPrice());

                    if (StringUtils.isNotBlank(rawOrderItem.getCombo()))
                        orderItem.setType(OrderItem.Type.Combo);
                    else if (StringUtils.isNotBlank(rawOrderItem.getComment()))
                        orderItem.setType(OrderItem.Type.Specials);
                    else
                        orderItem.setType(OrderItem.Type.General);

                    ProductInfo productInfo = productService.findByArtNumber(orderItem.getArtNumber());

                    if (productInfo == null)
                        orderItem.setNew(true);
                    else
                        productInfo = productService.loadOrCreate(orderItem.getArtNumber());

                    if (productInfo == null) {
                        orderItem.setType(OrderItem.Type.Na);
                        orderItem.setNew(false);
                    } else
                        orderItem.setProductInfo(productInfo);

                    reduceMap.put(artNumber, orderItem);

                } else
                    orderItem.setCount(orderItem.getCount() + rawOrderItem.getCount());

                result.add(orderItem);
                itemIndex++;

               int done = (100* itemIndex)/ list.size();

                taskProgress.updateProgress(80,100);
            }
        }
        return result;
    }

    private String getArtNumber(String artNumber) {
        Matcher m = artNumberPattern.matcher(artNumber);
        if (m.find())
            return m.group().trim();
        return "";
    }


    public List<Order> load() {
        return null;
    }
}
