package org.menesty.ikea.service;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.ui.TaskProgressLog;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IkeaUserService {

    private CloseableHttpClient httpClient;
    private BasicCookieStore cookieStore;

    private final static Pattern LIST_ID_PATTERN = Pattern.compile("listId=(\\d+)");

    public IkeaUserService() {
        cookieStore = new BasicCookieStore();
        httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    }


    public void fillOrder(Order order, TaskProgressLog taskProgressLog) throws IOException {

        prepareUserWorkSpace(order.getGeneralUser(), taskProgressLog);

        Map<ProductInfo.Group, List<OrderItem>> groupMap = groupItems(order.getByType(OrderItem.Type.General));

        Map<ProductInfo.Group, List<OrderItem>> subGroupMap = new HashMap<>();

        taskProgressLog.addLog("Create list of categories ...");
        List<Category> categories = createList(ProductInfo.Group.general());
        taskProgressLog.updateLog("Finish creating  list of categories");

        for (final Category category : categories) {
            List<OrderItem> list = groupMap.get(category.group);
            List<OrderItem> subList = fillListWithProduct(category, list, taskProgressLog);
            if (subList != null)
                subGroupMap.put(category.group, subList);

        }
        taskProgressLog.addLog("logout ....");
        logout();

        prepareUserWorkSpace(order.getComboUser(), taskProgressLog);

        subGroupMap.put(ProductInfo.Group.Combo, order.getByType(OrderItem.Type.Combo));

        taskProgressLog.addLog("Create list of categories ...");
        categories = createList(subGroupMap.keySet());

        for (final Category category : categories) {
            List<OrderItem> list = subGroupMap.get(category.group);
            fillListWithProduct(category, list, taskProgressLog);
        }

        taskProgressLog.addLog("logout ....");
        logout();
        taskProgressLog.addLog("Finish");

        closeSession();
        taskProgressLog.done();
    }

    private List<OrderItem> fillListWithProduct(final Category category, final List<OrderItem> list, final TaskProgressLog taskProgressLog) throws IOException {
        if (list == null) return null;

        int index = 0;
        List<OrderItem> workList = list.size() > 99 ? list.subList(0, 99) : list;
        taskProgressLog.addLog("Next group");

        for (OrderItem item : workList) {
            index++;
            taskProgressLog.updateLog(String.format("Group %1$s - product : %2$s  %3$s/%4$s", category.group, item.getArtNumber(), index, list.size()));
            addProductToList(category.id, prepareArtNumber(item.getArtNumber()), item.getCount());

        }
        return list.size() > 99 ? list.subList(99, list.size()) : null;
    }

    private String prepareArtNumber(String artNumber) {
        if (Character.isAlphabetic(artNumber.charAt(0)))
            return artNumber.substring(1);
        return artNumber;
    }

    private void prepareUserWorkSpace(User user, TaskProgressLog taskProgressLog) throws IOException {
        taskProgressLog.addLog(String.format("Try to login by user : %1$s ...", user.getLogin()));
        //TODO Handle login error
        login(user);
        taskProgressLog.updateLog(String.format("Logged as user : %1$s", user.getLogin()));

        taskProgressLog.addLog("Deleting categories under this user ...");
        deleteExisted();
        taskProgressLog.updateLog("Finish deleting categories");

    }

    private void addProductToList(String categoryId, String artNumber, int quantity) throws IOException {
        HttpPost httPost = new HttpPost("http://www.ikea.com/webapp/wcs/stores/servlet/IrwInterestItemAddByPartNumber");

        List<NameValuePair> nvps = new ArrayList<>();

        nvps.add(new BasicNameValuePair("storeId", "19"));
        nvps.add(new BasicNameValuePair("langId", "-27"));
        nvps.add(new BasicNameValuePair("catalogId", "null"));
        nvps.add(new BasicNameValuePair("partNumber", artNumber));
        nvps.add(new BasicNameValuePair("priceexclvat", ""));
        nvps.add(new BasicNameValuePair("listId", categoryId));
        nvps.add(new BasicNameValuePair("quantity", quantity + ""));

        httPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httPost)) {
            EntityUtils.consume(response.getEntity());
        }


    }

    private Map<ProductInfo.Group, List<OrderItem>> groupItems(List<OrderItem> orderItems) {
        Map<ProductInfo.Group, List<OrderItem>> groupMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            List<OrderItem> list = groupMap.get(item.getProductInfo().getGroup());

            if (list == null) groupMap.put(item.getProductInfo().getGroup(), list = new ArrayList<>());

            list.add(item);
        }
        return groupMap;
    }

    public void closeSession() throws IOException {
        httpClient.close();
    }

    private void deleteExisted() throws IOException {
        HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/InterestItemDisplay?storeId=19&langId=-27");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            List<String> ids = getCategoriesIds(EntityUtils.toString(response.getEntity()));

            for (String id : ids)
                deleteList(id);
        }

    }

    private void deleteList(String listId) throws IOException {
        HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/IrwDeleteShoppingList?langId=-27&storeId=19&slId=" + listId);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        }
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


    private List<Category> createList(Collection<ProductInfo.Group> groups) throws IOException {
        for (ProductInfo.Group group : groups) {
            HttpGet request = new HttpGet("http://www.ikea.com/webapp/wcs/stores/servlet/IrwWSCreateInterestList?langId=-27&storeId=19&slName=" + group.toString());
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

            for (String id : ids) {
                try {
                    ProductInfo.Group group = ProductInfo.Group.valueOf(document.select("#listId" + id).text());
                    categories.add(new Category(group, id));

                } catch (Exception e) {

                }
            }
        }

        return categories;

    }

    private void logout() throws IOException {
        httpClient.execute(new HttpGet("https://secure.ikea.com/webapp/wcs/stores/servlet/Logoff?langId=-27&storeId=19&rememberMe=false")).close();
    }

    private String login(User user) throws IOException {
        HttpPost httPost = new HttpPost("https://secure.ikea.com/webapp/wcs/stores/servlet/Logon");
        List<NameValuePair> nvps = new ArrayList<>();

        nvps.add(new BasicNameValuePair("storeId", "19"));
        nvps.add(new BasicNameValuePair("langId", "-27"));
        nvps.add(new BasicNameValuePair("logonId", user.getLogin())); //"komb_husar@gmail.com"
        nvps.add(new BasicNameValuePair("logonPassword", user.getPassword())); //"Mature65"

        httPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

        String forwardUrl;

        try (CloseableHttpResponse response = httpClient.execute(httPost)) {
            Header[] locations = response.getHeaders("location");
            forwardUrl = locations[0].getValue();

            if (!forwardUrl.contains("MyProfile"))
                throw new RuntimeException("Login or password invalid");

        }
        return forwardUrl;

    }


}

class Category {

    public Category(ProductInfo.Group group, String id) {
        this.group = group;
        this.id = id;
    }

    public String id;
    public ProductInfo.Group group;
}
/*System.out.println("Login form get: " + response.getStatusLine());
            System.out.println(EntityUtils.toString(entity) + " =====");

            System.out.println("Post logon cookies:");
            List<Cookie> cookies = cookieStore.getCookies();
            if (cookies.isEmpty()) {
                System.out.println("None");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    System.out.println("- " + cookies.get(i).toString());
                }
            }*/