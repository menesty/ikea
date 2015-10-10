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

public class IkeaUserService {
    private final static Pattern LIST_ID_PATTERN = Pattern.compile("listId=(\\d+)");

    public IkeaUserService() {

    }

    public <T extends UserProductInfo> void fillUser(final User user, final Collection<ProductInfo.Group> groups,
                                                     final Map<ProductInfo.Group, List<T>> groupMap,
                                                     final TaskProgressLog taskProgressLog) throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
            fillUser(httpClient, user, groups, groupMap, null, taskProgressLog);
        }
    }

    private <T extends UserProductInfo> void fillUser(CloseableHttpClient httpClient,
                                                      final User user,
                                                      final Collection<ProductInfo.Group> groups,
                                                      final Map<ProductInfo.Group, List<T>> groupMap,
                                                      final Map<ProductInfo.Group, List<T>> subGroupMap,
                                                      final TaskProgressLog taskProgressLog) throws IOException {

        prepareUserWorkSpace(httpClient, user, taskProgressLog);

        taskProgressLog.addLog("Create list of categories ...");
        List<Category> categories = createList(httpClient, groups);
        taskProgressLog.updateLog("Finish creating  list of categories");

        for (final Category category : categories) {
            List<T> list = groupMap.get(category.group);
            List<T> subList = fillListWithProduct(httpClient, category, list, taskProgressLog);

            if (subList != null && subGroupMap != null)
                subGroupMap.put(category.group, subList);

        }

        taskProgressLog.addLog("logout ....");
        logout(httpClient);
    }

    private <T extends UserProductInfo> List<T> fillListWithProduct(CloseableHttpClient httpClient,
                                                                    final Category category, final List<T> list,
                                                                    final TaskProgressLog taskProgressLog) throws IOException {
        if (list == null) return null;

        int index = 0;
        List<T> workList = list.size() > 99 ? list.subList(0, 99) : list;
        taskProgressLog.addLog("Next group");

        for (UserProductInfo item : workList) {
            index++;
            taskProgressLog.updateLog(String.format("Group %1$s - product : %2$s  %3$s/%4$s", category.group, item.getArtNumber(), index, list.size()));
            addProductToList(httpClient, category.id, prepareArtNumber(item.getArtNumber()), item.getCount());

        }

        return list.size() > 99 ? list.subList(99, list.size()) : null;
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

    private List<Category> createList(CloseableHttpClient httpClient, Collection<ProductInfo.Group> groups) throws IOException {
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

            for (String id : ids)
                try {
                    ProductInfo.Group group = ProductInfo.Group.valueOf(document.select("#listId" + id).text());
                    categories.add(new Category(group, id));
                } catch (Exception e) {
                    //skip
                }
        }

        return categories;
    }

    private void logout(CloseableHttpClient httpClient) throws IOException {
        httpClient.execute(new HttpGet("https://secure.ikea.com/webapp/wcs/stores/servlet/Logoff?langId=-27&storeId=19&rememberMe=false")).close();
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

    class Category {

        public Category(ProductInfo.Group group, String id) {
            this.group = group;
            this.id = id;
        }

        public String id;
        public ProductInfo.Group group;
    }
}


