package org.menesty.ikea.ui.pages.ikea.order.export;

import org.menesty.ikea.domain.User;
import org.menesty.ikea.exception.LoginIkeaException;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.IkeaShop;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StorageLackItem;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.ui.TaskProgressLog;
import org.menesty.ikea.ui.controls.dialog.IkeaUserFillProgressDialog;
import org.menesty.ikea.ui.pages.ikea.order.IkeaExportHttpClient;
import org.menesty.ikea.ui.pages.ikea.order.StockAvailability;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 10/2/15.
 * 00:17.
 */
public class IkeaExportService {
  private BigDecimal maxItemCount = new BigDecimal(99);
  private BigDecimal maxCategoriesCount = BigDecimal.TEN;
  private int NOT_IN_ANY_SHOP_INDEX = -1;

  public void export(List<IkeaOrderItem> items,
                     List<String> users,
                     List<IkeaShop> ikeaShops,
                     boolean splitByGroup,
                     TaskProgressLog taskProgressLog) {

    if (!items.isEmpty()) {
      try (IkeaExportHttpClient ikeaExportHttpClient = new IkeaExportHttpClient()) {
        try {
          taskProgressLog.addLog("Start export goods");

          taskProgressLog.addLog("Start validating users ...");
          AtomicBoolean validUsers = new AtomicBoolean(true);

          users.stream().forEach(login -> {
            try {
              taskProgressLog.addLog("Validating users :" + login);
              ikeaExportHttpClient.login(new User(login, "Mature65"));
              taskProgressLog.updateLog("User :" + login + " is valid");
            } catch (LoginIkeaException e) {
              validUsers.set(false);
              taskProgressLog.updateLog(e.getMessage());
            }
          });

          taskProgressLog.addLog("Finish validating users.");

          if (validUsers.get()) {
            if (splitByGroup) {
              start(ikeaExportHttpClient, items, users, ikeaShops, taskProgressLog);
            } else {
              startFlat(ikeaExportHttpClient, items, users, ikeaShops, taskProgressLog);
            }
          }

          taskProgressLog.addLog("Finish export goods");
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
    }
    taskProgressLog.done();
  }

  private void start(IkeaExportHttpClient ikeaExportHttpClient,
                     final List<IkeaOrderItem> items,
                     List<String> users,
                     List<IkeaShop> ikeaShops,
                     TaskProgressLog taskProgressLog) throws IOException {

    List<IkeaOrderItem> standardItems = items.stream()
        .filter(item -> !item.isSpecial())
        .collect(Collectors.toList());

    taskProgressLog.addLog("start prepare data...");
    ikeaShops = new ArrayList<>(ikeaShops);
    users = new ArrayList<>(users);

    Map<String, ProductAvailabilityInfo> availabilityInfoMap = checkProductAvailability(ikeaExportHttpClient, standardItems, taskProgressLog);
    Map<Integer, List<StockItem>> sortedByIkeaShop = sortByShopAvailability(standardItems, availabilityInfoMap, ikeaShops);
    //form by group only first shop
    Map<String, List<StockItem>> spitedByCategories = new HashMap<>();

    IkeaShop firstShop = ikeaShops.remove(0);
    List<StockItem> shopItems = sortedByIkeaShop.remove(firstShop.getShopId());
    Map<String, List<StockItem>> groupedByCategory;

    groupedByCategory = shopItems.stream().collect(Collectors.groupingBy(StockItem::getKey));

    Set<Map.Entry<String, List<StockItem>>> result = groupedByCategory.entrySet().
        stream()
        .map(groupedByCategoryEntry -> prepareSplitByCategory(groupedByCategoryEntry.getKey(), groupedByCategoryEntry.getValue()).entrySet())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    addMapContent(spitedByCategories, result);
    spitedByCategories = sortCategories(spitedByCategories);

    List<Map<String, List<StockItem>>> slittedByUsers = splitByUsers(spitedByCategories);
    taskProgressLog.addLog("finish prepare data");
    //fill first user
    String login = users.remove(0);
    Map<String, List<StockItem>> dataForFirstUser = slittedByUsers.remove(0);
        /*========================================================================*/
    fillUser(ikeaExportHttpClient, new User(login, "Mature65"), dataForFirstUser, taskProgressLog);
        /*========================================================================*/

    List<IkeaOrderItem> specialItems = items.stream()
        .filter(IkeaOrderItem::isSpecial)
        .collect(Collectors.toList());

    if (!slittedByUsers.isEmpty() || !ikeaShops.isEmpty() || !specialItems.isEmpty()) {

      taskProgressLog.addLog("start prepare data for next users ...");

      /*List<StockItem> joinedItems = flatItems(slittedByUsers);
      Map<String, List<StockItem>> allRestItems = joinedItems.isEmpty() ? new HashMap<>() : prepareSplitByCategory("Category", joinedItems);*/

      Map<String, List<StockItem>> allRestItems = slittedByUsers.remove(0);


      ikeaShops.stream().forEach(ikeaShop ->
              addMapContent(allRestItems, prepareSplitByCategory(ikeaShop.getName(), sortedByIkeaShop.get(ikeaShop.getShopId())).entrySet())
      );

      if (sortedByIkeaShop.containsKey(NOT_IN_ANY_SHOP_INDEX)) {
        addMapContent(allRestItems, prepareSplitByCategory("NOT_IN_ANY_SHOP", sortedByIkeaShop.get(NOT_IN_ANY_SHOP_INDEX)).entrySet());
      }


      if (!specialItems.isEmpty()) {
        addMapContent(allRestItems, prepareSplitByCategory("SPECIAL_ITEMS", specialItems.stream()
            .map(specialItem -> new StockItem(specialItem.getProduct().getArtNumber(), specialItem.getProduct().getGroup().getTitle(), specialItem.getCount()))
            .collect(Collectors.toList()))
            .entrySet());
      }

      final List<Map<String, List<StockItem>>> otherUsersData = splitByUsers(allRestItems);

      if (otherUsersData.size() > users.size()) {
        throw new RuntimeException(I18n.UA.getString(I18nKeys.EXCEPTION_IKEA_EXPORT_NOT_ENOUGH_USERS));
      }

      taskProgressLog.addLog("finish prepare data");

      for (Map<String, List<StockItem>> data : otherUsersData) {
        login = users.remove(0);
        fillUser(ikeaExportHttpClient, new User(login, "Mature65"), data, taskProgressLog);
      }
    }
  }

  protected Map<String, List<StockItem>> sortCategories(Map<String, List<StockItem>> unsortedMap) {
    Map<String, List<StockItem>> sortedMap = new LinkedHashMap<>();

    List<Map.Entry<String, List<StockItem>>> unSortedEntries = new ArrayList<>(unsortedMap.entrySet());
    Collections.sort(unSortedEntries, (o1, o2) -> Integer.valueOf(o1.getValue().size()).compareTo(o2.getValue().size()) * -1);

    addMapContent(sortedMap, unSortedEntries);

    return sortedMap;
  }

  private <Key, Value> void addMapContent(Map<Key, Value> targetMap, Collection<Map.Entry<Key, Value>> items) {
    items.stream().forEach(item -> targetMap.put(item.getKey(), item.getValue()));
  }

  private List<StockItem> flatItems(List<Map<String, List<StockItem>>> users) {
    List<List<StockItem>> value = users.stream()
        .map(stringListMap -> new ArrayList<>(stringListMap.values()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    List<StockItem> items = new ArrayList<>();

    value.stream().forEach(stockItems -> items.forEach(stockItem -> {
      if (items.contains(stockItem)) {
        int currentIndex = items.indexOf(stockItem);

        StockItem current = items.get(currentIndex);

        items.add(currentIndex, new StockItem(current.getArtNumber(), current.getKey(), current.getCount().add(stockItem.getCount())));

      } else {
        items.add(stockItem);
      }
    }));

    return items;
  }

  private void fillUser(IkeaExportHttpClient ikeaExportHttpClient,
                        final User user,
                        final Map<String, List<StockItem>> data,
                        final TaskProgressLog taskProgressLog) throws IOException {

    prepareUserWorkSpace(ikeaExportHttpClient, user, taskProgressLog);

    taskProgressLog.addLog("Create list of categories ...");
    List<ExportCategory> categories = ikeaExportHttpClient.createCategories(data.keySet());
    taskProgressLog.updateLog("Finish creating  list of categories");

    for (final ExportCategory category : categories) {
      List<StockItem> list = data.get(category.getGroup());
      fillListWithProduct(ikeaExportHttpClient, category, list, taskProgressLog);
    }

    taskProgressLog.addLog("logout ....");
    ikeaExportHttpClient.logout();
  }

  private void fillListWithProduct(IkeaExportHttpClient ikeaExportHttpClient,
                                   final ExportCategory category, final List<StockItem> list,
                                   final TaskProgressLog taskProgressLog) throws IOException {
    if (list == null)
      return;

    int index = 0;
    taskProgressLog.addLog("Next group");

    for (StockItem item : list) {
      index++;
      taskProgressLog.updateLog(String.format("Group %1$s - product : %2$s  %3$s/%4$s", category.getGroup(), item.getArtNumber(), index, list.size()));
      ikeaExportHttpClient.addProductToCategory(category.getId(), prepareArtNumber(item.getArtNumber()), item.getCount().doubleValue());
    }
  }

  private String prepareArtNumber(String artNumber) {
    if (Character.isAlphabetic(artNumber.charAt(0)))
      return artNumber.substring(1);

    return artNumber;
  }

  private void prepareUserWorkSpace(IkeaExportHttpClient ikeaExportHttpClient, User user, TaskProgressLog taskProgressLog) throws IOException {
    taskProgressLog.addLog(String.format("Try to login by user : %1$s ...", user.getLogin()));

    ikeaExportHttpClient.login(user);
    taskProgressLog.updateLog(String.format("Logged as user : %1$s", user.getLogin()));

    taskProgressLog.addLog("Deleting categories under this user ...");

    ikeaExportHttpClient.deleteCategories();

    taskProgressLog.updateLog("Finish deleting categories");
  }

  private List<Map<String, List<StockItem>>> splitByUsers(Map<String, List<StockItem>> items) {
    List<Map<String, List<StockItem>>> users = new ArrayList<>();

    Map<String, List<StockItem>> current = new LinkedHashMap<>();

    for (Map.Entry<String, List<StockItem>> entry : items.entrySet()) {
      if (current.size() == maxCategoriesCount.intValue()) {
        users.add(current);
        current = new LinkedHashMap<>();
      }
      current.put(entry.getKey(), entry.getValue());
    }

    if (!current.isEmpty()) {
      users.add(current);
    }

    return users;
  }

  private void startFlat(IkeaExportHttpClient ikeaExportHttpClient,
                         List<IkeaOrderItem> items,
                         List<String> users,
                         List<IkeaShop> ikeaShops,
                         TaskProgressLog taskProgressLog) throws IOException {
    taskProgressLog.addLog("start prepare data...");
    ikeaShops = new ArrayList<>(ikeaShops);
    users = new ArrayList<>(users);

    Map<String, ProductAvailabilityInfo> availabilityInfoMap = checkProductAvailability(ikeaExportHttpClient, items, taskProgressLog);
    Map<Integer, List<StockItem>> sortedByIkeaShop = sortByShopAvailability(items, availabilityInfoMap, ikeaShops);
    //form by group only first shop
    Map<String, List<StockItem>> dataByCategories = new HashMap<>();

    ikeaShops.stream().forEach(ikeaShop ->
            addMapContent(dataByCategories, prepareSplitByCategory(ikeaShop.getName(), sortedByIkeaShop.get(ikeaShop.getShopId())).entrySet())
    );

    if (sortedByIkeaShop.containsKey(NOT_IN_ANY_SHOP_INDEX)) {
      addMapContent(dataByCategories, prepareSplitByCategory("NOT_IN_ANY_SHOP", sortedByIkeaShop.get(NOT_IN_ANY_SHOP_INDEX)).entrySet());
    }

    final List<Map<String, List<StockItem>>> dataByUsers = splitByUsers(dataByCategories);

    if (dataByUsers.size() > users.size()) {
      throw new RuntimeException(I18n.UA.getString(I18nKeys.EXCEPTION_IKEA_EXPORT_NOT_ENOUGH_USERS));
    }

    taskProgressLog.addLog("finish prepare data");

    for (Map<String, List<StockItem>> data : dataByUsers) {
      String login = users.remove(0);
      fillUser(ikeaExportHttpClient, new User(login, "Mature65"), data, taskProgressLog);
    }
  }

  private Map<String, List<StockItem>> prepareSplitByCategory(String categoryName, List<StockItem> items) {
    CategorySplitter categorySplitter = new CategorySplitter(maxItemCount);

    categorySplitter.addItems(items);

    List<List<StockItem>> splitResult = categorySplitter.getResult();

    Map<String, List<StockItem>> result = new HashMap<>();

    for (int i = 0; i < splitResult.size(); i++) {
      String currentCategoryName = categoryName;

      if (i != 0) {
        currentCategoryName += "_" + i;
      }

      result.put(currentCategoryName, splitResult.get(i));
    }

    return result;
  }

  private Map<Integer, List<StockItem>> sortByShopAvailability(List<IkeaOrderItem> items,
                                                               Map<String, ProductAvailabilityInfo> availabilityInfoMap,
                                                               List<IkeaShop> shops) {
    Map<Integer, List<StockItem>> result = new HashMap<>();

    List<StockItem> stockItems = items.stream()
        .map(item -> new StockItem(item.getProduct().getArtNumber(), item.getProduct().getGroup().toString(), item.getCount()))
        .collect(Collectors.toList());

    for (IkeaShop ikeaShop : shops) {
      List<StockItem> nextShopItems = new ArrayList<>();
      List<StockItem> currentShopItems = new ArrayList<>();

      result.put(ikeaShop.getShopId(), currentShopItems);

      for (StockItem item : stockItems) {
        ProductAvailabilityInfo pai = availabilityInfoMap.get(item.getArtNumber());

        BigDecimal shopCount = pai.getStockCount(ikeaShop.getShopId());

        if (item.getCount().compareTo(shopCount) == 0 || item.getCount().compareTo(shopCount) < 0) {
          currentShopItems.add(item);
        } else if (BigDecimal.ZERO.equals(shopCount)) {
          nextShopItems.add(item);
        } else if (item.getCount().compareTo(shopCount) > 0) {
          //split item
          currentShopItems.add(new StockItem(item.getArtNumber(), item.getKey(), shopCount));
          nextShopItems.add(new StockItem(item.getArtNumber(), item.getKey(), item.getCount().subtract(shopCount)));
        }
      }

      stockItems = nextShopItems;
    }

    if (!stockItems.isEmpty()) {
      result.put(NOT_IN_ANY_SHOP_INDEX, stockItems);
    }

    return result;
  }

  private Map<String, ProductAvailabilityInfo> checkProductAvailability(
      IkeaExportHttpClient ikeaExportHttpClient,
      List<IkeaOrderItem> items, final TaskProgressLog taskProgressLog) {

    Map<String, ProductAvailabilityInfo> result = new HashMap<>();
    AtomicInteger atomicInteger = new AtomicInteger(0);

    items.stream().forEach(item -> {
      String artNumber = item.getProduct().getArtNumber();
      taskProgressLog.updateLog(String.format("Check available product : %1$s  %2$s/%3$s",
          artNumber, atomicInteger.incrementAndGet(), items.size()));

      Map<Integer, StockAvailability> aResult = ikeaExportHttpClient.checkAvailability(artNumber);
      result.put(artNumber, new ProductAvailabilityInfo(aResult));
    });

    return result;
  }

  public int calculateUsers(List<IkeaOrderItem> items, List<IkeaShop> ikeaShops, boolean splitByGroup) {
    BigDecimal countCategories = new BigDecimal(ikeaShops.size()).subtract(BigDecimal.ONE);

    BigDecimal initCategories = BigDecimal.valueOf(items.size()).divide(maxItemCount, 0, BigDecimal.ROUND_CEILING);
    countCategories = countCategories.add(initCategories);

    if (!splitByGroup) {
      BigDecimal extraCategories = items.stream()
          .map(item -> item.getCount().divide(maxItemCount, 0, BigDecimal.ROUND_CEILING).subtract(BigDecimal.ONE))
          .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

      countCategories = countCategories.add(extraCategories);
    } else {
      BigDecimal categories = items.stream()
          .collect(Collectors.groupingBy(item -> item.getProduct().getGroup().getTitle()))
          .values()
          .stream()
          .map(ikeaOrderItems -> {
            BigDecimal itemExtraCategories = ikeaOrderItems.stream()
                .map(item -> item.getCount().divide(maxItemCount, 0, BigDecimal.ROUND_CEILING).subtract(BigDecimal.ONE))
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            return BigDecimal.valueOf(ikeaOrderItems.size()).divide(maxItemCount, 0, BigDecimal.ROUND_CEILING)
                .add(itemExtraCategories);
          }).reduce(BigDecimal.ZERO, BigDecimal::add);

      countCategories = countCategories.add(categories);
    }

    return countCategories.divide(maxCategoriesCount, 0, BigDecimal.ROUND_CEILING).intValue();
  }

  public void export(List<StorageLackItem> items, String login, IkeaUserFillProgressDialog taskProgressLog) {
    List<StockItem> stockItems = items.stream().map(storageLack -> new StockItem(storageLack.getArtNumber(), "StorageLack", storageLack.getCount())).collect(Collectors.toList());

    Map<String, List<StockItem>> dataByCategories = prepareSplitByCategory("StorageLack", stockItems);

    try (IkeaExportHttpClient ikeaExportHttpClient = new IkeaExportHttpClient()) {
      try {
        taskProgressLog.addLog("Start export goods");
        fillUser(ikeaExportHttpClient, new User(login, "Mature65"), dataByCategories, taskProgressLog);
        taskProgressLog.addLog("Finish export goods");
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
  }
}
