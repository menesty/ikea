package org.menesty.ikea.service;

import net.sf.jxls.reader.*;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.order.RawOrderItem;
import org.menesty.ikea.ui.TaskProgress;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
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
                    orderItem.setArtNumber(artNumber);
                    orderItem.setComment(rawOrderItem.getComment());
                    orderItem.setCount(rawOrderItem.getCount());
                    orderItem.setName(rawOrderItem.getDescription());
                    orderItem.setPrice(rawOrderItem.getPrice());

                    if (StringUtils.isNotBlank(rawOrderItem.getComment()))
                        orderItem.setType(OrderItem.Type.Specials);
                    else if (StringUtils.isNotBlank(rawOrderItem.getCombo()))
                        orderItem.setType(OrderItem.Type.Combo);
                    else
                        orderItem.setType(OrderItem.Type.General);

                    String productArtNumber = getPrapareArtNumber(orderItem.getArtNumber());

                    ProductInfo productInfo = productService.findByArtNumber(productArtNumber);

                    if (productInfo == null) {
                        productInfo = productService.loadOrCreate(productArtNumber);

                        if (productInfo == null)
                            orderItem.setType(OrderItem.Type.Na);

                        orderItem.setNew(productInfo != null);
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

    public String getPrapareArtNumber(String artNumber) {
        String prepared = StringUtils.leftPad(artNumber.trim(), 8, '0');
        int lastPos = artNumber.length();
        prepared = artNumber.substring(0, lastPos - 5) + "-" + artNumber.substring(lastPos - 5, lastPos - 2) + "-" + artNumber.substring(lastPos - 2, lastPos);
        return prepared;
    }

    private String getArtNumber(String artNumber) {
        Matcher m = artNumberPattern.matcher(artNumber);
        if (m.find())
            return m.group().trim();
        return "";
    }

    public List<Order> load() {
        return DatabaseService.get().query(Order.class);
    }

    public void save(Order order) {
        DatabaseService.get().store(order);
    }

    public void save(InvoicePdf invoicePdf) {
        DatabaseService.get().store(invoicePdf);
    }

    public void exportToXls(Order order, String filePath, TaskProgress taskProgress) throws URISyntaxException {
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e1) {
            e1.printStackTrace();
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
        double total = 0d;

        for (OrderItem item : orderItems) {
            total += item.getTotal();
        }
        return total;
    }
}
