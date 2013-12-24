package org.menesty.ikea.service;

import net.sf.jxls.reader.*;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.menesty.ikea.domain.*;
import org.menesty.ikea.exception.ProductFetchException;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderService extends Repository<CustomerOrder> {
    private static final Pattern artNumberPattern = Pattern.compile("\\w{0,}\\d+");

    private ProductService productService;

    public OrderService() {
        this.productService = new ProductService();
    }

    public CustomerOrder createOrder(String name, InputStream is, TaskProgress taskProgress) {

        try {
            CustomerOrder order = new CustomerOrder();
            order.setName(name);
            order.setCreatedDate(new Date());

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

            order = ServiceFacade.getOrderService().save(order);
            order.setOrderItems(ServiceFacade.getOrderService().save(reduce(order, rawOrderItems, taskProgress)));

            return order;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<OrderItem> reduce(CustomerOrder order, List<RawOrderItem> list, TaskProgress taskProgress) {
        Map<String, OrderItem> reduceMap = new HashMap<>();
        int itemIndex = 0;

        for (RawOrderItem rawOrderItem : list) {
            itemIndex++;
            if (rawOrderItem.getArtNumber() == null || (StringUtils.isNotBlank(rawOrderItem.getCombo()) && rawOrderItem.getPrice() == 0))
                continue;

            String artNumber = getArtNumber(rawOrderItem.getArtNumber());
            if (!artNumber.isEmpty()) {
                String keyPrefix = StringUtils.isNotBlank(rawOrderItem.getComment()) ? "_s" : "";
                OrderItem orderItem = reduceMap.get(artNumber + keyPrefix);

                if (orderItem == null) {
                    orderItem = new OrderItem();
                    orderItem.customerOrder = order;
                    orderItem.setArtNumber(artNumber);
                    orderItem.setComment(rawOrderItem.getComment());
                    orderItem.setCount(rawOrderItem.getCount());
                    orderItem.setName(rawOrderItem.getDescription());

                    if (rawOrderItem.getCount() != 0)
                        orderItem.setPrice(NumberUtil.round(rawOrderItem.getPrice() / rawOrderItem.getCount()));

                    if (StringUtils.isNotBlank(rawOrderItem.getComment()))
                        orderItem.setType(OrderItem.Type.Specials);
                    else if (StringUtils.isNotBlank(rawOrderItem.getCombo()))
                        orderItem.setType(OrderItem.Type.Combo);
                    else
                        orderItem.setType(OrderItem.Type.General);

                    String productArtNumber = getPrepareArtNumber(orderItem.getArtNumber());

                    ProductInfo productInfo = productService.findByArtNumber(productArtNumber);

                    if (productInfo == null) {
                        try {
                            productInfo = productService.loadOrCreate(productArtNumber);

                            if (productInfo == null)
                                orderItem.setType(OrderItem.Type.Na);

                        } catch (ProductFetchException e) {
                            orderItem.setInvalidFetch(true);
                            orderItem.setTryCount(1);
                        }
                    }

                    orderItem.setProductInfo(productInfo);

                    reduceMap.put(artNumber + keyPrefix, orderItem);

                } else
                    orderItem.setCount(orderItem.getCount() + rawOrderItem.getCount());

                int done = (100 * itemIndex) / list.size();
                taskProgress.updateProgress((80 * done) / 100 + 20, 100);
            }
        }
        return new ArrayList<>(reduceMap.values());
    }

    public static void main(String... args) throws FileNotFoundException {
        OrderService orderService = new OrderService();
        orderService.createOrder("39", new FileInputStream("C:\\Users\\Menesty\\Downloads/ikea39.xlsx"), new TaskProgress() {
            @Override
            public void updateProgress(long l, long l1) {
            }
        });
    }

    public String getPrepareArtNumber(String artNumber) {
        String prepared = StringUtils.leftPad(artNumber.trim(), 8, '0');
        int lastPos = artNumber.length();
        prepared = prepared.substring(0, lastPos - 5) + "-" + prepared.substring(lastPos - 5, lastPos - 2) + "-" + prepared.substring(lastPos - 2, lastPos);
        return prepared;
    }

    private String getArtNumber(String artNumber) {
        Matcher m = artNumberPattern.matcher(artNumber);
        if (m.find())
            return m.group().trim();

        return "";
    }

    public void exportToXls(CustomerOrder order, String filePath, TaskProgress taskProgress) throws URISyntaxException {
        Map<OrderItem.Type, List<OrderItem>> typeFilterMap = new HashMap<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            List<OrderItem> orderItems = getByType(orderItem.getType(), typeFilterMap);
            orderItems.add(orderItem);
        }

        XLSTransformer transformer = new XLSTransformer();
        List<String> templateSheetNameList = Arrays.asList("combo", "color", "na", "invoice");
        List<String> sheetNameList = Arrays.asList("combo_r", "color_r", "na_r", "invoice_r");

        double totalSum = 0d;
        List<Map<String, Object>> mapBeans = new ArrayList<>();

        taskProgress.updateProgress(2, 100);
        totalSum += populateData(OrderItem.Type.Combo, typeFilterMap, mapBeans);
        totalSum += populateData(OrderItem.Type.Specials, typeFilterMap, mapBeans);
        totalSum += populateData(OrderItem.Type.Na, typeFilterMap, mapBeans);
        totalSum += populateData(OrderItem.Type.General, typeFilterMap, mapBeans);
        taskProgress.updateProgress(5, 100);
        mapBeans.get(3).put("totalSum", totalSum);

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");


        if (!filePath.endsWith(".zip")) filePath += ".zip";

        URI fileUri = new File(filePath).toURI();
        URI zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);

        try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
            taskProgress.updateProgress(10, 100);

            Workbook workbook = transformer.transformXLS(getClass().getResourceAsStream("/config/reduce.xlsx"), templateSheetNameList, sheetNameList, mapBeans);
            workbook.write(Files.newOutputStream(zipfs.getPath("reduce-result.xlsx"), StandardOpenOption.CREATE_NEW));

            taskProgress.updateProgress(15, 100);

            int taskStep = 85 / (ProductInfo.Group.values().length + 1);
            int taskIndex = 0;

            for (ProductInfo.Group group : ProductInfo.Group.values()) {
                taskIndex++;
                Map<String, Object> bean = new HashMap<>();
                bean.put("orderItems", filterByProductGroup(group, (List<OrderItem>) mapBeans.get(3).get("orderItems")));
                workbook = transformer.transformXLS(getClass().getResourceAsStream("/config/group.xlsx"), bean);
                workbook.write(Files.newOutputStream(zipfs.getPath(group.toString().toLowerCase() + ".xlsx"), StandardOpenOption.CREATE_NEW));
                taskProgress.updateProgress(15 + taskStep * taskIndex, 100);
            }

            Map<String, Object> bean = new HashMap<>();
            bean.put("orderItems", filterByProductGroup(null, (List<OrderItem>) mapBeans.get(3).get("orderItems")));
            workbook = transformer.transformXLS(getClass().getResourceAsStream("/config/group.xlsx"), bean);
            workbook.write(Files.newOutputStream(zipfs.getPath("unknown-group.xlsx"), StandardOpenOption.CREATE_NEW));
            taskProgress.updateProgress(100, 100);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<OrderItem> filterByProductGroup(ProductInfo.Group group, List<OrderItem> orderItems) {
        List<OrderItem> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            if (group == null && orderItem.getProductInfo() == null) {
                result.add(orderItem);
                continue;
            }

            if (orderItem.getProductInfo() != null && orderItem.getProductInfo().getGroup() == group)
                result.add(orderItem);
        }

        return result;
    }

    private double populateData(OrderItem.Type type, Map<OrderItem.Type, List<OrderItem>> typeFilterMap, List<Map<String, Object>> mapBeans) {
        Map<String, Object> bean = new HashMap<>();
        List<OrderItem> data = getByType(type, typeFilterMap);
        bean.put("orderItems", data);
        double total = getTotal(data);
        bean.put("total", total);
        mapBeans.add(bean);
        return total;
    }

    private List<OrderItem> getByType(OrderItem.Type type, Map<OrderItem.Type, List<OrderItem>> typeFilterMap) {
        List<OrderItem> orderItems = typeFilterMap.get(type);
        if (orderItems == null)
            typeFilterMap.put(type, orderItems = new ArrayList<>());

        return orderItems;
    }

    private double getTotal(List<OrderItem> orderItems) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : orderItems) {
            total = total.add(item.getTotal());
        }
        return total.doubleValue();
    }

    public List<StorageLack> calculateOrderInvoiceDiff(CustomerOrder order) {
        List<RawInvoiceProductItem> rawItems = new ArrayList<>();

        for (InvoicePdf invoicePdf : order.getInvoicePdfs())
            rawItems.addAll(invoicePdf.getProducts());

        rawItems = InvoicePdfService.reduce(rawItems);

        Map<String, Double> orderData = new HashMap<>();
        Map<String, ProductInfo> infoData = new HashMap<>();


        for (OrderItem orderItem : order.getOrderItems()) {
            if (OrderItem.Type.Na != orderItem.getType() && !orderItem.isInvalidFetch() && OrderItem.Type.Specials != orderItem.getType())
                if (OrderItem.Type.Combo == orderItem.getType())
                    for (ProductPart part : orderItem.getProductInfo().getParts()) {
                        increaseData(orderData, part.getProductInfo().getOriginalArtNum(), NumberUtil.round(orderItem.getCount() * part.getCount()));
                        infoData.put(part.getProductInfo().getOriginalArtNum(), part.getProductInfo());
                    }
                else
                    increaseData(orderData, orderItem.getProductInfo().getOriginalArtNum(), orderItem.getCount());

            if (!orderItem.isInvalidFetch() && OrderItem.Type.Na != orderItem.getType())
                infoData.put(orderItem.getProductInfo().getOriginalArtNum(), orderItem.getProductInfo());
        }

        List<StorageLack> result = new ArrayList<>();

        for (RawInvoiceProductItem item : rawItems) {
            Double count = orderData.get(item.getOriginalArtNumber());
            if (count == null)
                result.add(new StorageLack(item.getProductInfo(), item.getCount(), false));
            else {
                if (NumberUtil.round(count - item.getCount()) == 0)
                    orderData.remove(item.getOriginalArtNumber());
                else
                    orderData.put(item.getOriginalArtNumber(), NumberUtil.round(count - item.getCount()));
            }

        }

        for (Map.Entry<String, Double> entry : orderData.entrySet())
            result.add(new StorageLack(infoData.get(entry.getKey()), entry.getValue()));

        return result;
    }

    private void increaseData(Map<String, Double> data, String key, double value) {
        Double currentValue = data.get(key);
        currentValue = currentValue == null ? value : NumberUtil.round(currentValue + value);
        data.put(key, currentValue);
    }

    public void remove(final CustomerOrder order, InvoicePdf invoicePdf) {
        order.getInvoicePdfs().remove(invoicePdf);
        ServiceFacade.getInvoicePdfService().remove(invoicePdf);
    }

    public void remove(CustomerOrder order, List<InvoicePdf> items) {
        for (InvoicePdf item : items)
            remove(order, item);
    }

    public void remove(CustomerOrder item) {
        try {
            begin();
            remove(item, item.getInvoicePdfs());
            remove(item.getOrderItems());
            super.remove(item);
            commit();
        } catch (Exception e) {
            rollback();
        }
    }
}
