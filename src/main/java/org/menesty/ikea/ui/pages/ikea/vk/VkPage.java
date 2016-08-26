package org.menesty.ikea.ui.pages.ikea.vk;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.domain.vk.VKData;
import org.menesty.ikea.domain.vk.VKMarketAlbum;
import org.menesty.ikea.domain.vk.VKMarketItem;
import org.menesty.ikea.domain.vk.VKMarketProduct;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.product.ProductCategoryShortInfo;
import org.menesty.ikea.lib.domain.product.ProductShortInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.vk.dialog.VKAlbumCreateDialog;
import org.menesty.ikea.ui.pages.ikea.vk.dialog.VKImportProductDialog;
import org.menesty.ikea.ui.pages.ikea.vk.service.VKLoadService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.vk.VKMarketAPIRequest;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 7/11/16.
 * 20:00.
 */
public class VkPage extends BasePage {
  private TableView<VKMarketItem> tableView;
  private AlbumComponent albumComponent;
  private VKLoadService loadService;
  private VKImportProductDialog vkImportProductDialog;
  private VKCategoryLoadService vkCategoryLoadService;

  @Override
  protected void initialize() {
    loadService = new VKLoadService();
    vkCategoryLoadService = new VKCategoryLoadService();
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    main.setLeft(albumComponent = new AlbumComponent(getDialogSupport(), loadService));


    BorderPane marketPane = new BorderPane();
    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createImport16Icon());
      button.setOnAction(event -> {
        VKImportProductDialog dialog = getVKImportProductDialog();

        dialog.setDefaultAction(baseDialog -> getDialogSupport().hidePopupDialog());
        dialog.setCallBack(data -> {
          vkCategoryLoadService.setProductIds(data.stream().map(ProductShortInfo::getId).collect(Collectors.toList()));

          vkCategoryLoadService.restart(value -> {
            importProducts(value, data);
          });
        });


        getDialogSupport().showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    marketPane.setTop(toolBar);
    marketPane.setCenter(tableView = new TableView<>());

    {
      TableColumn<VKMarketItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.NAME));
      column.setPrefWidth(250);
      column.setCellValueFactory(ColumnUtil.column("title"));

      tableView.getColumns().add(column);
    }
    {
      TableColumn<VKMarketItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setPrefWidth(100);
      column.setCellValueFactory(ColumnUtil.column("price.text"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<VKMarketItem, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CATEGORY));
      column.setPrefWidth(150);
      column.setCellValueFactory(ColumnUtil.column("category.name"));

      tableView.getColumns().add(column);
    }

    main.setCenter(marketPane);

    albumComponent.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      loadService.vkRun(() -> {
        VKMarketAPIRequest request = HttpServiceUtil.getVKMarketAPI();

        return newValue != null ? request.get(newValue.getId()) : request.get();
      }, value -> tableView.getItems().setAll(value.getItems()));
    });

    return wrap(main);
  }

  private void importProducts(final Map<Long, List<ProductCategoryShortInfo>> categories, final List<ProductShortInfo> products) {
    Map<String, VKMarketAlbum> mapVkAlbum = getAlbumMap();

    Set<String> secondLvl = categories.values().stream()
        .filter(productCategoryShortInfos -> !productCategoryShortInfos.isEmpty() && productCategoryShortInfos.size() > 1)
        .map(productCategoryShortInfos -> productCategoryShortInfos.get(1).getName())
        .filter(catName -> !mapVkAlbum.containsKey(catName))
        .collect(Collectors.toSet());

    loadService.vkRun(() -> {
      VKMarketAPIRequest request = HttpServiceUtil.getVKMarketAPI();

      secondLvl.stream().forEach(cat -> {
        try {
          VKMarketAlbum album = new VKMarketAlbum();
          album.setTitle(cat);

          request.addAlbum(album);
          TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });

      VKData<VKMarketAlbum> data = request.getAlbums();
      Platform.runLater(() -> albumComponent.setData(data));
      TimeUnit.SECONDS.sleep(3);

      Map<String, VKMarketAlbum> mapVkAlbumP = getAlbumMap();
      Map<Long, List<VKMarketItem>> alumItems = new HashMap<>();

      products.stream().forEach(productShortInfo -> {
        List<ProductCategoryShortInfo> cats = categories.get(productShortInfo.getId());
        if (cats.size() == 1) {
          System.out.println("Only one LVL " + cats.get(0).getArtNumber());
          return;
        }
        VKMarketAlbum album = mapVkAlbumP.get(cats.get(1).getName());
        if (!alumItems.containsKey(album.getId())) {
          try {
            alumItems.put(album.getId(), request.get(album.getId()).getItems());
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        Optional<VKMarketItem> exist = alumItems.get(album.getId()).stream()
            .filter(vkMarketItem -> vkMarketItem.getTitle().endsWith(productShortInfo.getArtNumber()))
            .findFirst();
        try {
          if (StringUtils.isBlank(productShortInfo.getDescription())) {
            System.out.println("empty description :" + productShortInfo.getArtNumber());
            return;
          }

          if (exist.isPresent()) {
            //update
            VKMarketItem productItem = exist.get();

            VKMarketProduct product = new VKMarketProduct();

            product.setId(productItem.getId());
            product.setName(productShortInfo.getTitle() + " " + productShortInfo.getArtNumber());
            product.setDescription(productShortInfo.getDescription());
            product.setCategoryId(productItem.getCategory().getId());
            product.setPrice(productShortInfo.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
            product.setMainPhotoId(productItem.getPhotos().get(0).getId());

            request.editProduct(product);
          } else {
            TimeUnit.SECONDS.sleep(1);
            VKMarketProduct product = new VKMarketProduct();

            product.setName(productShortInfo.getTitle() + " " + productShortInfo.getArtNumber());
            product.setDescription(productShortInfo.getDescription());
            product.setCategoryId(cats.get(0).getVkId());
            product.setPrice(productShortInfo.getPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
            Long photoId = request.uploadPhotoProduct(productShortInfo.getArtNumber(), true);

            if (photoId == null) {
              throw new RuntimeException("Problem upload photo for product " + productShortInfo.getArtNumber());
            }
            product.setMainPhotoId(photoId);
            //upload photo
            Long itemId = request.addProduct(product);

            request.addToAlbum(itemId, album.getId());

          }
          TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
          System.out.println("Error :" + productShortInfo.getArtNumber());
          //e.printStackTrace();
        }
      });
      return null;
    }, value -> {

    });

  }

  private Map<String, VKMarketAlbum> getAlbumMap() {
    return albumComponent.getItems().stream().collect(Collectors.toMap(VKMarketAlbum::getTitle, Function.identity()));
  }


  private VKImportProductDialog getVKImportProductDialog() {
    if (vkImportProductDialog == null) {
      vkImportProductDialog = new VKImportProductDialog(getStage());
    }
    return vkImportProductDialog;
  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(loadService, vkCategoryLoadService);

    loadService.vkRun(() -> {
      VKMarketAPIRequest request = HttpServiceUtil.getVKMarketAPI();

      VKData<VKMarketItem> items = request.get();
      VKData<VKMarketAlbum> albums = request.getAlbums();

      return new LoadDataResult(items, albums);
    }, value -> {
      albumComponent.setData(value.getAlbums());
      tableView.getItems().setAll(value.getItems().getItems());
    });
  }
}

class AlbumComponent extends BorderPane {
  private ListView<VKMarketAlbum> albumListView;
  private VKAlbumCreateDialog vkAlbumCreateDialog;

  public AlbumComponent(DialogSupport dialogSupport, VKLoadService vkLoadService) {
    setCenter(albumListView = new ListView<>());

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createAdd16Icon());
      button.setOnAction(event -> {
        VKAlbumCreateDialog dialog = getAlbumDialog(dialogSupport.getStage());
        dialog.bind(new VKMarketAlbum(), new EntityDialogCallback<VKMarketAlbum>() {
          @Override
          public void onSave(final VKMarketAlbum vkMarketAlbum, Object... params) {
            dialogSupport.hidePopupDialog();

            vkLoadService.vkRun(() -> HttpServiceUtil.getVKMarketAPI().addAlbum(vkMarketAlbum)
                , value ->
                {
                  vkMarketAlbum.setId(value);
                  albumListView.getItems().add(vkMarketAlbum);
                }
            );
          }

          @Override
          public void onCancel() {
            dialogSupport.hidePopupDialog();
          }
        });

        dialogSupport.showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    {
      Button button = new Button(null, ImageFactory.createEdit16Icon());
      button.setOnAction(event -> {
        VKAlbumCreateDialog dialog = getAlbumDialog(dialogSupport.getStage());
        dialog.bind(albumListView.getSelectionModel().getSelectedItem(), new EntityDialogCallback<VKMarketAlbum>() {
          @Override
          public void onSave(VKMarketAlbum vkMarketAlbum, Object... params) {
            dialogSupport.hidePopupDialog();

            vkLoadService.vkRun(() -> HttpServiceUtil.getVKMarketAPI().editAlbum(vkMarketAlbum)
                , value ->
                {
                  if (value) {
                    int index = albumListView.getItems().indexOf(vkMarketAlbum);

                    if (index != -1) {
                      albumListView.getItems().remove(vkMarketAlbum);
                      albumListView.getItems().add(index, vkMarketAlbum);
                    }
                  }
                }
            );
          }

          @Override
          public void onCancel() {
            dialogSupport.hidePopupDialog();
          }
        });

        dialogSupport.showPopupDialog(dialog);
      });

      button.setDisable(true);

      albumListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));
      toolBar.getItems().add(button);
    }

    {
      Button button = new Button(null, ImageFactory.createDelete16Icon());
      button.setOnAction(event -> {
        VKMarketAlbum selected = albumListView.getSelectionModel().getSelectedItem();

        vkLoadService.vkRun(() -> HttpServiceUtil.getVKMarketAPI().deleteAlbum(selected)
            , value ->
            {
              if (value) {
                albumListView.getItems().remove(selected);
              }
            }
        );
      });

      button.setDisable(true);

      albumListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));
      toolBar.getItems().add(button);
    }

    setTop(toolBar);
  }

  public ReadOnlyObjectProperty<VKMarketAlbum> selectedItemProperty() {
    return albumListView.getSelectionModel().selectedItemProperty();
  }

  public List<VKMarketAlbum> getItems() {
    return albumListView.getItems();
  }

  public void setData(VKData<VKMarketAlbum> albums) {
    albumListView.getItems().setAll(albums.getItems());
  }

  private VKAlbumCreateDialog getAlbumDialog(Stage stage) {
    if (vkAlbumCreateDialog == null) {
      vkAlbumCreateDialog = new VKAlbumCreateDialog(stage);
    }

    return vkAlbumCreateDialog;
  }
}

class VKCategoryLoadService extends AbstractAsyncService<Map<Long, List<ProductCategoryShortInfo>>> {
  private ObjectProperty<List<Long>> productIdsProperties = new SimpleObjectProperty<>();

  @Override
  protected Task<Map<Long, List<ProductCategoryShortInfo>>> createTask() {
    final List<Long> _ids = productIdsProperties.get();
    return new Task<Map<Long, List<ProductCategoryShortInfo>>>() {
      @Override
      protected Map<Long, List<ProductCategoryShortInfo>> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/vk-category/");
        return request.postData(_ids, new TypeReference<Map<Long, List<ProductCategoryShortInfo>>>() {
        });
      }
    };
  }

  public void setProductIds(List<Long> ids) {
    productIdsProperties.setValue(ids);
  }
}

class LoadDataResult {
  private final VKData<VKMarketItem> items;
  private final VKData<VKMarketAlbum> albums;

  public LoadDataResult(VKData<VKMarketItem> items, VKData<VKMarketAlbum> albums) {
    this.items = items;
    this.albums = albums;
  }

  public VKData<VKMarketItem> getItems() {
    return items;
  }

  public VKData<VKMarketAlbum> getAlbums() {
    return albums;
  }
}
