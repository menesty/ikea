package org.menesty.ikea.ui.table;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Menesty on
 * 10/6/15.
 * 08:34.
 */
public class ArtNumberCell<T> extends TableCell<T, String> {
  private Label numberLabel;
  private HBox content;
  private final TableColumn<T, String> column;
  private final Pattern ART_NUMBER_PATTERN = Pattern.compile("I_(.*)_.*");

  public ArtNumberCell(TableColumn<T, String> column) {
    this.column = column;
  }

  @Override
  protected void updateItem(String number, boolean empty) {
    super.updateItem(number, empty);
    if (empty) {
      setGraphic(null);
      setText(null);
    } else {
      if (numberLabel == null) {
        content = new HBox();
        content.setPrefWidth(Double.MAX_VALUE);
        numberLabel = new Label();

        ImageView imageView = ImageFactory.createWeb16Icon();
        HBox.setMargin(imageView, new Insets(0, 3, 0, 3));
        imageView.setOnMouseClicked(mouseEvent -> {
          String artNumber = numberLabel.getText();

          Matcher matcher = ART_NUMBER_PATTERN.matcher(artNumber);

          if (matcher.find()) {
            artNumber = matcher.group(1);
          }

          ProductDialog.browse(artNumber);

        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        content.getChildren().addAll(numberLabel, space, imageView);
      }

      content.setMinWidth(getWidth() - getGraphicTextGap() * 2);
      setGraphic(content);
      numberLabel.setText(number);
    }
  }

}
