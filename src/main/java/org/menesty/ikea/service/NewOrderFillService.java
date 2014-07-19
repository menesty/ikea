package org.menesty.ikea.service;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.*;
import org.menesty.ikea.exception.LoginIkeaException;
import org.menesty.ikea.ui.TaskProgressLog;
import org.menesty.ikea.util.NumberUtil;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Menesty on
 * 6/17/14.
 * 20:31.
 */
public class NewOrderFillService {
    private final static Pattern LIST_ID_PATTERN = Pattern.compile("listId=(\\d+)");

    public void fillOrder(CustomerOrder order, TaskProgressLog taskProgressLog) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
            try {
                taskProgressLog.addLog("Start");
                List<OrderItem> orderItems = ServiceFacade.getOrderItemService().loadBy(order);

                taskProgressLog.addLog("start prepare data...");
                Map<ProductInfo.Group, List<OrderItem>> data = groupItems(
                        OrderItemService.getByType(orderItems, Arrays.asList(OrderItem.Type.General, OrderItem.Type.Combo)));

                taskProgressLog.addLog("finish prepare data");

                start(httpClient, order.getIkeaShops(), order.getUsers(), data, taskProgressLog);

                taskProgressLog.addLog("Finish");
            } catch (LoginIkeaException e) {
                taskProgressLog.addLog(e.getMessage());
            } catch (IOException e) {
                taskProgressLog.addLog("Error happened during connection to IKEA site");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            taskProgressLog.addLog(e.getMessage());
        }
        taskProgressLog.done();
    }

    public <T extends UserProductInfo> Map<ProductInfo.Group, List<T>> groupItems(List<T> orderItems) {
        Map<ProductInfo.Group, List<T>> groupMap = new HashMap<>();

        for (T item : orderItems) {

            if (item.getProductInfo() == null) continue;

            List<T> list = groupMap.get(item.getProductInfo().getGroup());

            if (list == null) groupMap.put(item.getProductInfo().getGroup(), list = new ArrayList<>());

            list.add(item);
        }
        return groupMap;
    }

    private void start(CloseableHttpClient httpClient, List<IkeaShop> shops, List<User> users, Map<ProductInfo.Group,
            List<OrderItem>> inputData, final TaskProgressLog taskProgressLog
    ) throws Exception {
        int needUsers = 0;
        taskProgressLog.addLog("Start check product availability ...");
        Map<ProductInfo.Group, List<ProductAvailabilityInfo>> data = checkProductAvailability(inputData, taskProgressLog);
        taskProgressLog.addLog("Finish check availability");

        List<Map<String, List<StockItem>>> reduceResult = new ArrayList<>();

        Iterator<IkeaShop> shopIterator = shops.iterator();
        List<Map<String, List<StockItem>>> shopDataByUser = splitByGroupCount(getShopData(shopIterator.next(), data));
        reduceResult.addAll(shopDataByUser);


        Map<String, List<StockItem>> joinShopData = new HashMap<>();

        while (shopIterator.hasNext()) {
            IkeaShop shop = shopIterator.next();
            joinShopData.putAll(reduceGroups(shop.getName(), getShopData(shop, data)));
        }

        if (data.size() != 0)
            joinShopData.putAll(reduceGroups("unknown", convert(data)));

        reduceResult.addAll(splitByGroupCount(joinShopData));

        if (needUsers > users.size())
            throw new Exception("not enough users assign more user to this order");

        Iterator<User> userIterator = users.iterator();

        for (Map<String, List<StockItem>> item : reduceResult)
            fillUser(httpClient, userIterator.next(), item, taskProgressLog);

    }

    private Map<String, List<StockItem>> convert(Map<ProductInfo.Group, List<ProductAvailabilityInfo>> data) {
        Map<String, List<StockItem>> result = new HashMap<>();
        for (Map.Entry<ProductInfo.Group, List<ProductAvailabilityInfo>> entry : data.entrySet()) {
            List<StockItem> targetResult = new ArrayList<>();

            for (ProductAvailabilityInfo item : entry.getValue())
                targetResult.add(item.getStockItem());

            result.put(entry.getKey().getTitle(), targetResult);
        }

        return result;
    }


    private void fillUser(CloseableHttpClient httpClient,
                          final User user,
                          final Map<String, List<StockItem>> data,
                          final TaskProgressLog taskProgressLog) throws IOException {

        prepareUserWorkSpace(httpClient, user, taskProgressLog);

        taskProgressLog.addLog("Create list of categories ...");
        List<Category> categories = createList(httpClient, data.keySet());
        taskProgressLog.updateLog("Finish creating  list of categories");

        for (final Category category : categories) {
            List<StockItem> list = data.get(category.group);
            fillListWithProduct(httpClient, category, list, taskProgressLog);
        }

        taskProgressLog.addLog("logout ....");
        logout(httpClient);
    }

    private void fillListWithProduct(CloseableHttpClient httpClient,
                                     final Category category, final List<StockItem> list,
                                     final TaskProgressLog taskProgressLog) throws IOException {
        if (list == null)
            return;

        int index = 0;
        taskProgressLog.addLog("Next group");

        for (StockItem item : list) {
            index++;
            taskProgressLog.updateLog(String.format("Group %1$s - product : %2$s  %3$s/%4$s", category.group, item.artNumber, index, list.size()));
            addProductToList(httpClient, category.id, prepareArtNumber(item.artNumber), item.getCount());
        }
    }


    private void addProductToList(CloseableHttpClient httpClient, String categoryId, String artNumber, double quantity) throws IOException {
        HttpUriRequest request = RequestBuilder.post()
                .setUri("http://www.ikea.com/webapp/wcs/stores/servlet/IrwInterestItemAddByPartNumber")
                .addParameter("storeId", "19")
                .addParameter("langId", "-27")
                .addParameter("catalogId", "null")
                .addParameter("partNumber", artNumber)
                .addParameter("priceexclvat", "")
                .addParameter("listId", categoryId)
                .addParameter("quantity", NumberUtil.toString(quantity)).build();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        }
    }

    private String prepareArtNumber(String artNumber) {
        if (Character.isAlphabetic(artNumber.charAt(0)))
            return artNumber.substring(1);

        return artNumber;
    }

    private void prepareUserWorkSpace(CloseableHttpClient httpClient, User user, TaskProgressLog taskProgressLog) throws IOException {
        taskProgressLog.addLog(String.format("Try to login by user : %1$s ...", user.getLogin()));

        login(httpClient, user);
        taskProgressLog.updateLog(String.format("Logged as user : %1$s", user.getLogin()));

        taskProgressLog.addLog("Deleting categories under this user ...");

        deleteExisted(httpClient);

        taskProgressLog.updateLog("Finish deleting categories");
    }

    private void deleteExisted(CloseableHttpClient httpClient) throws IOException {
        HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/InterestItemDisplay?storeId=19&langId=-27");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            List<String> ids = getCategoriesIds(EntityUtils.toString(response.getEntity()));

            for (String id : ids)
                deleteList(httpClient, id);
        }

    }

    private void deleteList(CloseableHttpClient httpClient, String listId) throws IOException {
        HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/IrwDeleteShoppingList?langId=-27&storeId=19&slId=" + listId);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        }
    }

    private String login(CloseableHttpClient httpClient, User user) throws LoginIkeaException {
        HttpUriRequest request = RequestBuilder.post()
                .setUri("https://secure.ikea.com/webapp/wcs/stores/servlet/Logon")
                .addParameter("storeId", "19")
                .addParameter("langId", "-27")
                .addParameter("logonId", user.getLogin())
                .addParameter("logonPassword", user.getPassword()).build();

        String forwardUrl;

        try {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                Header[] locations = response.getHeaders("location");
                forwardUrl = locations[0].getValue();

                if (!forwardUrl.contains("MyProfile"))
                    throw new LoginIkeaException("Invalid login or password for user : " + user.getLogin());

            }
        } catch (IOException e) {
            throw new LoginIkeaException(e);
        }
        return forwardUrl;

    }

    private void logout(CloseableHttpClient httpClient) throws IOException {
        httpClient.execute(new HttpGet("https://secure.ikea.com/webapp/wcs/stores/servlet/Logoff?langId=-27&storeId=19&rememberMe=false")).close();
    }

    private List<Category> createList(CloseableHttpClient httpClient, Collection<String> groups) throws IOException {
        for (String group : groups) {
            HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/IrwWSCreateInterestList?langId=-27&storeId=19&slName=" + group);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                EntityUtils.consume(response.getEntity());
            }
        }

        HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/InterestItemDisplay?storeId=19&langId=-27");
        List<Category> categories = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String html = EntityUtils.toString(response.getEntity());
            List<String> ids = getCategoriesIds(html);
            Document document = Jsoup.parse(html);

            for (String id : ids)
                try {
                    String group = document.select("#listId" + id).text();
                    categories.add(new Category(group, id));
                } catch (Exception e) {
                    //skip
                }
        }

        return categories;
    }

    private List<String> getCategoriesIds(String html) {
        List<String> result = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements categories = document.select(".navigationBlock .navItem a");

        for (Element link : categories) {
            Matcher m = LIST_ID_PATTERN.matcher(link.attr("href"));

            if (m.find())
                result.add(m.group(1));
        }

        return result;
    }

    class Category {

        public Category(String group, String id) {
            this.group = group;
            this.id = id;
        }

        public String id;
        public String group;
    }

   /* private Map<String, List<StockItem>> joinInOneGroup(String groupName, Map<String, List<StockItem>> data) {
        Map<String, List<StockItem>> result = new HashMap<String, List<StockItem>>();
        List<StockItem> joinData = new ArrayList<StockItem>();

        for (List<StockItem> list : data.values()) {
            joinData.addAll(list);
        }

        int itemCount = 99;
        int count = (int) Math.ceil((double) data.size() / itemCount);

        for (int i = 0; i < count; i++) {
            int start = i * itemCount;
            int end = start + itemCount;

            if (end >= data.size())
                end = data.size();

            result.put(groupName + (i != 0 ? "_" + i : ""), joinData.subList(start, end));
        }
        return result;
    }*/

    private List<Map<String, List<StockItem>>> splitByGroupCount(Map<String, List<StockItem>> shopData) {
        List<Map<String, List<StockItem>>> result = new ArrayList<>();

        List<List<Map.Entry<String, List<StockItem>>>> split = splitList(new ArrayList<>(shopData.entrySet()), 10);

        for (List<Map.Entry<String, List<StockItem>>> part : split) {
            Map<String, List<StockItem>> map = new HashMap<>();
            result.add(map);

            for (Map.Entry<String, List<StockItem>> entry : part)
                map.put(entry.getKey(), entry.getValue());
        }

        split.clear();

        return result;
    }


    private Map<String, List<StockItem>> getShopData(IkeaShop shop, Map<ProductInfo.Group, List<ProductAvailabilityInfo>> data) {
        Map<String, List<StockItem>> preparedData = new HashMap<>();
        List<StockItem> extraGroupItems = new ArrayList<>();

        for (Map.Entry<ProductInfo.Group, List<ProductAvailabilityInfo>> entry : data.entrySet()) {
            List<List<ProductAvailabilityInfo>> groupList = splitList(entry.getValue(), 99);

            for (int i = 0; i < groupList.size(); i++) {
                String keyGroupName = entry.getKey().getTitle() + (i != 0 ? "_" + i : "");

                List<StockItem> groupItems = new ArrayList<>();
                preparedData.put(keyGroupName, groupItems);

                for (ProductAvailabilityInfo info : groupList.get(i)) {
                    double shopCount = info.getStockCount(shop.getShopId());

                    if (shopCount != 0) {
                        double currentCount = info.getStockItem().count > shopCount ? shopCount : info.getStockItem().count;

                        List<StockItem> items = splitStockItem(info.getStockItem().artNumber, currentCount);

                        groupItems.add(items.get(0));
                        items.remove(0);

                        if (items.size() != 0)
                            extraGroupItems.addAll(items);

                        if (info.getStockItem().count > shopCount)
                            info.setStockItem(new StockItem(info.getStockItem().artNumber, info.getStockItem().count - shopCount));
                        else
                            entry.getValue().remove(info);

                    }
                }
            }
        }

        if (extraGroupItems.size() != 0)
            preparedData.put("ExtraGroup", extraGroupItems);

        //clean up map from empty list
        for (ProductInfo.Group key : new ArrayList<>(data.keySet()))
            if (data.get(key).size() == 0)
                data.remove(key);

        return preparedData;
    }

    private List<StockItem> splitStockItem(String artNumber, double itemCount) {
        List<StockItem> items = new ArrayList<>();
        int count = (int) Math.ceil(itemCount / 99);

        for (int i = 0; i < count; i++) {
            int r = (i + 1) * 99;
            double newCount = r > itemCount ? itemCount - i * 99 : 99;
            StockItem item = new StockItem(artNumber, newCount);
            items.add(item);
        }

        return items;
    }

    private <T> List<List<T>> splitList(List<T> data, int itemCount) {
        List<List<T>> result = new ArrayList<>();

        int count = (int) Math.ceil((double) data.size() / itemCount);

        for (int i = 0; i < count; i++) {
            int start = i * itemCount;
            int end = start + itemCount;

            if (end >= data.size())
                end = data.size();

            result.add(new ArrayList<>(data.subList(start, end)));
        }
        return result;
    }

    /*private List<StockItem> splitItem(StockItem stockItem) {
        List<StockItem> result = new ArrayList<StockItem>();
        double currentCount = stockItem.count;

        while (true) {
            if (currentCount > 99) {
                result.add(new StockItem(stockItem.artNumber, 99));
                currentCount -= 99;
            } else {
                result.add(new StockItem(stockItem.artNumber, currentCount));
                break;
            }
        }
        return result;
    }*/


    private Map<ProductInfo.Group, List<ProductAvailabilityInfo>> checkProductAvailability(
            Map<ProductInfo.Group, List<OrderItem>> products, final TaskProgressLog taskProgressLog) {

        Map<ProductInfo.Group, List<ProductAvailabilityInfo>> result = new HashMap<>();

        for (Map.Entry<ProductInfo.Group, List<OrderItem>> entry : products.entrySet()) {
            List<ProductAvailabilityInfo> productInfoList = new ArrayList<>();
            result.put(entry.getKey(), productInfoList);
            int index = 0;

            for (OrderItem item : entry.getValue()) {
                index++;
                taskProgressLog.updateLog(String.format("Check available Group %1$s - product : %2$s  %3$s/%4$s", entry.getKey(),
                        item.getArtNumber(), index, entry.getValue().size()));
                Map<Integer, StockAvailability> aResult = checkAvailability(item.getArtNumber());
                productInfoList.add(new ProductAvailabilityInfo(new StockItem(item.getArtNumber(), item.getCount()), aResult));
            }
        }

        return result;
    }

    private Map<Integer, StockAvailability> checkAvailability(String artNumber) {
        int tryCount = 0;
        Map<Integer, StockAvailability> result = new HashMap<>();

        while (tryCount != 3) {
            try {
                Document document = Jsoup.connect("http://www.ikea.com/pl/pl/iows/catalog/availability/" + artNumber).get();
                Elements elements = document.select("availability localStore");

                for (Element element : elements) {
                    try {
                        StockAvailability stock = new StockAvailability();
                        stock.setShopId(Integer.valueOf(element.attr("buCode")));

                        Elements forcasts = element.select("forecasts forcast availableStock");

                        if (forcasts.size() > 1) {
                            stock.setAvailable(Integer.valueOf(forcasts.get(0).html()));
                            stock.setNextAvailable(Integer.valueOf(forcasts.get(0).html()));
                        }

                        result.put(stock.getShopId(), stock);
                    } catch (NumberFormatException e) {
                        //skip
                    }
                }
                break;
            } catch (IOException e) {
                tryCount++;
            }
        }

        return result;
    }

    public static void main(String... arg) throws Exception {
        NewOrderFillService service = new NewOrderFillService();
        List<IkeaShop> ikeaShops = new ArrayList<>();
        {
            IkeaShop ikeaShop = new IkeaShop();
            ikeaShop.setShopId(204);
            ikeaShop.setName("Krakow");
            ikeaShops.add(ikeaShop);
        }
        User user = new User();
        user.setLogin("kra96@gmail.com");
        user.setPassword("Mature65");

        Map<ProductInfo.Group, List<OrderItem>> inputData = new HashMap<>();
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setArtNumber("00133123");
        item.setCount(120);
        orderItems.add(item);

        inputData.put(ProductInfo.Group.Kitchen, orderItems);


        try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
            service.start(httpClient, ikeaShops, Arrays.asList(user), inputData, new TaskProgressLog() {
                @Override
                public void addLog(String log) {

                }

                @Override
                public void updateLog(String log) {

                }

                @Override
                public void done() {

                }
            });
        }
    }

    private Map<String, List<StockItem>> reduceGroups(String groupName, Map<String, List<StockItem>> data) {
        List<List<StockItem>> joinData = new ArrayList<>();

        List<StockItem> currentList = new ArrayList<>();
        List<StockItem> nextList = new ArrayList<>();
        List<StockItem> unsorted = new ArrayList<>();
        for (Map.Entry<String, List<StockItem>> entry : data.entrySet()) {
            for (StockItem item : entry.getValue()) {
                if (!currentList.contains(item))
                    currentList.add(item);
                else if (!nextList.contains(item))
                    nextList.add(item);
                else
                    unsorted.add(item);

                if (currentList.size() == 99) {
                    joinData.add(currentList);
                    currentList = nextList;
                    nextList = new ArrayList<>();
                }

                if (nextList.size() == 99) {
                    joinData.add(nextList);
                    nextList = new ArrayList<>();
                }
            }
        }
        if (currentList.size() != 0)
            joinData.add(currentList);
        if (nextList.size() != 0)
            joinData.add(nextList);

        Map<String, List<StockItem>> newData = new HashMap<>();
        int index = 0;

        for (List<StockItem> list : joinData) {
            index++;
            newData.put(groupName + "_" + index, list);
        }

        return newData;
    }

    static class StockItem {
        private String artNumber;
        private double count;

        StockItem(String artNumber, double count) {
            this.artNumber = artNumber;
            this.count = count;
        }

        public double getCount() {
            return count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StockItem stockItem = (StockItem) o;

            return artNumber != null ? artNumber.equals(stockItem.artNumber) : stockItem.artNumber == null;
        }
    }

    static class ProductAvailabilityInfo {
        private StockItem stockItem;

        private Map<Integer, StockAvailability> stockAvailability;

        ProductAvailabilityInfo(StockItem stockItem, Map<Integer, StockAvailability> stockAvailability) {
            this.stockItem = stockItem;
            this.stockAvailability = stockAvailability;
        }

        public void setStockItem(StockItem stockItem) {
            this.stockItem = stockItem;
        }

        public double getStockCount(int shopId) {
            StockAvailability stock = stockAvailability.get(shopId);

            if (stock != null)
                return stock.getAvailable();

            return 0;
        }

        public StockItem getStockItem() {
            return stockItem;
        }
    }

    static class StockAvailability {
        private int shopId;

        private int available;

        private int nextAvailable;

        public int getShopId() {
            return shopId;
        }

        public void setShopId(int shopId) {
            this.shopId = shopId;
        }

        public int getAvailable() {
            return available;
        }

        public void setAvailable(int available) {
            this.available = available;
        }

        public void setNextAvailable(int nextAvailable) {
            this.nextAvailable = nextAvailable;
        }

        @Override
        public String toString() {
            return "StockAvailability{" +
                    "shopId=" + shopId +
                    ", available=" + available +
                    ", nextAvailable=" + nextAvailable +
                    '}';
        }
    }
}
