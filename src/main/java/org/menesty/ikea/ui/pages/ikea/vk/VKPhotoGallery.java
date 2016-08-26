package org.menesty.ikea.ui.pages.ikea.vk;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.menesty.ikea.domain.PhotoUrl;
import org.menesty.ikea.domain.vk.VKPhoto;
import org.menesty.ikea.domain.vk.VKPhotoAlbum;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.ui.CallBack;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.ikea.vk.service.VKLoadService;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.vk.VKMarketAPIRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 7/14/16.
 * 21:50.
 */
public class VKPhotoGallery {
  private VKLoadService vkLoadService;

  private ListView<VKPhotoAlbum> listView;
  private GalleryView<VKPhoto> photoPanel;
  private final Scene scene;

  private Stage stage;
  private CallBack<List<VKPhoto>> callBack;

  public VKPhotoGallery() {
    initServices();

    scene = initScene(initContent());
  }

  private void initServices() {
    vkLoadService = new VKLoadService();
  }

  private Parent initContent() {
    StackPane stackPane = new StackPane();

    LoadingPane loadingPane = new LoadingPane();
    loadingPane.bindTask(vkLoadService);

    stackPane.getChildren().addAll(initStructure(), loadingPane);

    AnchorPane.setBottomAnchor(stackPane, 0d);
    AnchorPane.setTopAnchor(stackPane, 0d);
    AnchorPane.setLeftAnchor(stackPane, 0d);
    AnchorPane.setRightAnchor(stackPane, 0d);

    return new AnchorPane(stackPane);
  }

  public void show(CallBack<List<VKPhoto>> callBack) {
    final Stage stage = getStage();
    stage.centerOnScreen();
    show(stage, callBack);
  }

  private Stage getStage() {
    if (stage == null) {
      stage = new Stage();
    }

    return stage;
  }

  public void show(Stage stage, CallBack<List<VKPhoto>> callBack) {
    this.callBack = callBack;

    stage.setScene(scene);
    stage.show();

    loadAlbums();
  }

  private Scene initScene(Parent parent) {
    Scene scene = new Scene(parent, 600, 400, Color.WHITESMOKE);
    scene.getStylesheets().add("/styles/application.css");

    return scene;
  }

  private void loadAlbums() {
    vkLoadService.vkRun(() -> {
      VKMarketAPIRequest request = HttpServiceUtil.getVKMarketAPI();
      return request.getPhotosAlbums();
    }, value -> listView.getItems().setAll(value.getItems()));
  }

  private void hide() {
    getStage().hide();
  }


  private Pane initStructure() {
    BorderPane borderPane = new BorderPane();

    borderPane.setLeft(listView = new ListView<>());

    listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadAlbumPhotos(newValue.getId()));

    ScrollPane scrollPane = new ScrollPane(photoPanel = new GalleryView<>());

    borderPane.setCenter(scrollPane);

    photoPanel.maxWidthProperty().bind(scrollPane.widthProperty().subtract(20));
    photoPanel.minWidthProperty().bind(scrollPane.widthProperty().subtract(20));

    listView.setCellFactory(param -> new ListCell<VKPhotoAlbum>() {
      @Override
      protected void updateItem(VKPhotoAlbum item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
          setText(null);
          setGraphic(null);
        } else {
          setText(item.getTitle());
        }
      }
    });

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(I18n.UA.getString(I18nKeys.OK));
      button.setOnAction(event -> {
        if (callBack != null) {
          callBack.onResult(photoPanel.getSelected());
        }

        hide();
      });

      toolBar.getItems().add(button);
    }

    borderPane.setBottom(toolBar);

    return borderPane;
  }

  private void loadAlbumPhotos(long albumId) {
    vkLoadService.vkRun(() -> {
      VKMarketAPIRequest request = HttpServiceUtil.getVKMarketAPI();

      return request.getPhotosByAlbum(albumId);
    }, (value -> {
      photoPanel.getItems().setAll(value.getItems());
    }));
  }
}

class GalleryView<T extends PhotoUrl> extends TilePane {
  private ObjectProperty<ObservableList<T>> items;

  private ObservableList<T> selectedItems;

  private final ListChangeListener<T> listViewItemsListener = c -> updateListViewItems();
  private final InvalidationListener itemsChangeListener = observable -> updateListViewItems();

  public final ObjectProperty<ObservableList<T>> itemsProperty() {
    if (items == null) {
      items = new SimpleObjectProperty<>(this, "items");
    }
    return items;
  }

  public GalleryView() {
    super(8, 8);

    getStyleClass().add("gallery-pane");
    setPadding(new Insets(10, 10, 10, 10));
    setItems(FXCollections.<T>observableArrayList());

    selectedItems = FXCollections.<T>observableArrayList();
    assignListeners();
  }

  private void updateListViewItems() {
    getChildren().setAll(items.get().stream().map(item -> {
      final ImageTile tile = new ImageTile(item.getUrl());

      tile.setOnMouseClicked(event -> {
        event.consume();
        boolean newState = !tile.selectedProperty().get();
        tile.selectedProperty().setValue(newState);

        int selectedIndex = getChildren().indexOf(tile);
        Optional<T> optional = Optional.ofNullable(items.get().get(selectedIndex));

        if (optional.isPresent()) {
          if (newState) {
            selectedItems.add(optional.get());
          } else {
            selectedItems.remove(optional.get());
          }
        }

        tile.select(newState);
      });

      return tile;

    }).collect(Collectors.toList()));

    //update selected
    selectedItems.removeIf(item -> !items.get().contains(item));
  }

  public List<T> getSelected() {
    return Collections.unmodifiableList(selectedItems);
  }

  private void assignListeners() {
    getItems().addListener(new WeakListChangeListener<>(listViewItemsListener));
    itemsProperty().addListener(new WeakInvalidationListener(itemsChangeListener));
  }

  public void setItems(ObservableList<T> value) {
    itemsProperty().setValue(value);
  }

  public final ObservableList<T> getItems() {
    return items == null ? null : items.get();
  }

  class ImageTile extends Group {
    private double holderSize = 150;
    private double imageSize = 130;

    private Color normalBackgroundColor = Color.web("#eee");
    private Color selectedBackgroundColor = Color.web("#d8f0f9");
    private Color normalBorderColor = Color.web("#ccc");
    private Color selectedBorderColor = Color.web("#ccc");


    private Rectangle background;

    private BooleanProperty selected;

    public final BooleanProperty selectedProperty() {
      if (selected == null) {
        selected = new SimpleBooleanProperty(this, "selected");
      }
      return selected;
    }

    public ImageTile(String imageUrl) {
      background = new Rectangle(holderSize, holderSize);
      background.setFill(normalBackgroundColor);
      background.setStroke(normalBorderColor);

      Group pane = new Group(background);

      ImageView imageView = new ImageView();

      if (imageUrl != null) {
        imageView.setImage(new Image(imageUrl, true));
      }

      imageView.setFitHeight(imageSize);
      imageView.setFitWidth(imageSize);

      imageView.setTranslateX((int) ((holderSize - imageView.getBoundsInParent().getWidth()) / 2) - (int) imageView.getBoundsInParent().getMinX());
      imageView.setTranslateY((int) ((holderSize - imageView.getBoundsInParent().getHeight()) / 2) - (int) imageView.getBoundsInParent().getMinY());

      pane.getChildren().add(imageView);

      getChildren().add(pane);
    }

    public void select(boolean select) {
      background.setFill(select ? selectedBackgroundColor : normalBackgroundColor);
    }
  }
}