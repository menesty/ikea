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
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.domain.UserProductInfo;
import org.menesty.ikea.exception.LoginIkeaException;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.ui.TaskProgressLog;
import org.menesty.ikea.util.NumberUtil;

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

    public <T extends UserProductInfo> void fillUser(final User user, final Collection<ProductInfo.Group> groups, final Map<ProductInfo.Group, List<T>> groupMap, final TaskProgressLog taskProgressLog) throws IOException {
        fillUser(user, groups, groupMap, null, taskProgressLog);
    }

    public <T extends UserProductInfo> void fillUser(final User user, final Collection<ProductInfo.Group> groups, final Map<ProductInfo.Group, List<T>> groupMap, final Map<ProductInfo.Group, List<T>> subGroupMap, final TaskProgressLog taskProgressLog) throws IOException {
        prepareUserWorkSpace(user, taskProgressLog);
        prepareUserWorkSpace(user, taskProgressLog);

        taskProgressLog.addLog("Create list of categories ...");
        List<Category> categories = createList(groups);
        taskProgressLog.updateLog("Finish creating  list of categories");

        for (final Category category : categories) {
            List<T> list = groupMap.get(category.group);
            List<T> subList = fillListWithProduct(category, list, taskProgressLog);

            if (subList != null && subGroupMap != null)
                subGroupMap.put(category.group, subList);

        }

        taskProgressLog.addLog("logout ....");
        logout();
    }

    public void fillOrder(CustomerOrder order, TaskProgressLog taskProgressLog) {
        try {

            Map<ProductInfo.Group, List<OrderItem>> groupMap = groupItems(order.getByType(OrderItem.Type.General));
            Map<ProductInfo.Group, List<OrderItem>> subGroupMap = new HashMap<>();

            fillUser(order.getGeneralUser(), ProductInfo.Group.general(), groupMap, subGroupMap, taskProgressLog);

            subGroupMap.put(ProductInfo.Group.Combo, order.getByType(OrderItem.Type.Combo));

            fillUser(order.getComboUser(), subGroupMap.keySet(), subGroupMap, taskProgressLog);

            taskProgressLog.addLog("Finish");
            closeSession();
        } catch (LoginIkeaException e) {
            taskProgressLog.addLog(e.getMessage());
        } catch (IOException e) {
            taskProgressLog.addLog("Error happened during connection to IKEA site");
        }

        taskProgressLog.done();
    }

    private <T extends UserProductInfo> List<T> fillListWithProduct(final Category category, final List<T> list, final TaskProgressLog taskProgressLog) throws IOException {
        if (list == null) return null;

        int index = 0;
        List<T> workList = list.size() > 99 ? list.subList(0, 99) : list;
        taskProgressLog.addLog("Next group");

        for (UserProductInfo item : workList) {
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

        login(user);
        taskProgressLog.updateLog(String.format("Logged as user : %1$s", user.getLogin()));

        taskProgressLog.addLog("Deleting categories under this user ...");
        deleteExisted();
        taskProgressLog.updateLog("Finish deleting categories");

    }

    private void addProductToList(String categoryId, String artNumber, double quantity) throws IOException {
        HttpPost httPost = new HttpPost("http://www.ikea.com/webapp/wcs/stores/servlet/IrwInterestItemAddByPartNumber");

        List<NameValuePair> nvps = new ArrayList<>();

        nvps.add(new BasicNameValuePair("storeId", "19"));
        nvps.add(new BasicNameValuePair("langId", "-27"));
        nvps.add(new BasicNameValuePair("catalogId", "null"));
        nvps.add(new BasicNameValuePair("partNumber", artNumber));
        nvps.add(new BasicNameValuePair("priceexclvat", ""));
        nvps.add(new BasicNameValuePair("listId", categoryId));
        nvps.add(new BasicNameValuePair("quantity", NumberUtil.toString(quantity)));

        httPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httPost)) {
            EntityUtils.consume(response.getEntity());
        }


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

    private String login(User user) throws LoginIkeaException {
        HttpPost httPost = new HttpPost("https://secure.ikea.com/webapp/wcs/stores/servlet/Logon");
        List<NameValuePair> nvps = new ArrayList<>();

        nvps.add(new BasicNameValuePair("storeId", "19"));
        nvps.add(new BasicNameValuePair("langId", "-27"));
        nvps.add(new BasicNameValuePair("logonId", user.getLogin())); //"komb_husar@gmail.com"
        nvps.add(new BasicNameValuePair("logonPassword", user.getPassword())); //"Mature65"

        httPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

        String forwardUrl;

        try {
            try (CloseableHttpResponse response = httpClient.execute(httPost)) {
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

    class Category {

        public Category(ProductInfo.Group group, String id) {
            this.group = group;
            this.id = id;
        }

        public String id;
        public ProductInfo.Group group;
    }
}


