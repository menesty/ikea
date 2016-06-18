package org.menesty.ikea.ui.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Menesty on
 * 6/17/16.
 * 15:59.
 */
public class ProductImageCell<T> extends TableCell<T, String> {
  private static final int cacheSize = 100;
  private static Map<String, SoftReference<Image>> cache = new LinkedHashMap<>(cacheSize);
  private static final String THUMB_URL = "http://fotel.com.ua//image/thumb/";

  public ProductImageCell(TableColumn<T, String> column) {
  }

  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if (empty) {
      setGraphic(null);
      setText(null);
    } else {
      ImageView imageView = new ImageView();

      imageView.setFitHeight(80);
      imageView.setFitWidth(80);

      imageView.setImage(getImage(item));

      setGraphic(imageView);
    }
  }

  private Image getImage(String artNumber) {
    String key = THUMB_URL + artNumber;

    SoftReference<Image> softReference = cache.get(key);
    Image image = null;

    if (softReference != null) {
      image = softReference.get();
    }

    if (image == null) {
      image = new Image(key, true);

      cache.put(key, new SoftReference<>(image));

      if (cache.size() > cacheSize) {
        while (cache.size() > cacheSize) {
          cache.remove(cache.keySet().iterator().next());
        }
      }
    }

    return image;
  }
}
