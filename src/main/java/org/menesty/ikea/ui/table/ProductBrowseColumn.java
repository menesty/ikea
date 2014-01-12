package org.menesty.ikea.ui.table;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.dialog.ProductDialog;

/**
 * Created by Menesty on 1/12/14.
 */
public class ProductBrowseColumn<S> extends TableColumn<S, String> {

    public ProductBrowseColumn() {
        setMaxWidth(25);
        setCellFactory(new Callback<TableColumn<S, String>, TableCell<S, String>>() {
            @Override
            public TableCell<S, String> call(TableColumn<S, String> invoiceItemNumberTableColumn) {
                TableCell<S, String> tableCell = new TableCell<S, String>() {
                    private ImageView imageView;
                    private HBox content;

                    @Override
                    protected void updateItem(final String artNumber, boolean empty) {
                        super.updateItem(artNumber, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            if (imageView == null) {
                                imageView = ImageFactory.createWeb16Icon();
                                content = new HBox();
                                content.getChildren().add(imageView);

                            }

                            imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    ProductDialog.browse(artNumber);
                                }
                            });
                            setGraphic(content);
                        }
                    }
                };
                tableCell.setAlignment(Pos.CENTER);
                return tableCell;
            }
        });
    }
}
