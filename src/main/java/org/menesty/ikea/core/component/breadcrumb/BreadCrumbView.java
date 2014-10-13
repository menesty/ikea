package org.menesty.ikea.core.component.breadcrumb;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.menesty.ikea.factory.ImageFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Created by Menesty on
 * 10/10/14.
 * 18:39.
 */
public class BreadCrumbView extends HBox {
    public interface OnBreadCrumbItemClickListener {
        void onItemClick(BreadCrumbItem item);
    }

    private List<Button> buttons = new ArrayList<>();

    private OnBreadCrumbItemClickListener listener;


    public BreadCrumbView() {
        super(0);
        getStyleClass().setAll("breadcrumb-bar");
        setFillHeight(true);
        setAlignment(Pos.CENTER_LEFT);
    }

    private void render(final Deque<BreadCrumbItem> crumbItems) {
        Deque<BreadCrumbItem> items = new ArrayDeque<>(crumbItems);

        for (int i = 0; i < Math.max(crumbItems.size(), buttons.size()); i++) {
            if (i < crumbItems.size()) {
                Button button;

                if (i < buttons.size()) {
                    button = buttons.get(i);
                } else {
                    button = new Button();
                    button.setMaxHeight(Double.MAX_VALUE);
                    buttons.add(button);
                    getChildren().add(button);
                }

                final BreadCrumbItem breadCrumbItem = items.pollFirst();
                button.setVisible(true);

                if (crumbItems.getFirst().equals(breadCrumbItem)) {
                    button.setGraphic(ImageFactory.createHome24Icon());
                    button.setText(" ");
                    button.setDisable(false);
                } else {
                    button.setText(breadCrumbItem.getName());
                    button.setDisable(breadCrumbItem.getPageDescription() == null || !breadCrumbItem.getPageDescription().isAllowRefresh());
                }

                button.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        if (listener != null) {
                            listener.onItemClick(breadCrumbItem);
                        }
                    }
                });

                if (i == crumbItems.size() - 1) {
                    if (i == 0) {
                        button.getStyleClass().setAll("button", "only-button");
                    } else {
                        button.getStyleClass().setAll("button", "last-button");
                    }
                } else if (i == 0) {
                    button.getStyleClass().setAll("button", "first-button");
                } else {
                    button.getStyleClass().setAll("button", "middle-button");
                }
            } else {
                //hide button
                //avoid memory leek
                buttons.get(i).setOnAction(null);
                buttons.get(i).setVisible(false);
            }
        }
    }

    public void setListener(OnBreadCrumbItemClickListener listener) {
        this.listener = listener;
    }

    public void register(BreadCrumb breadCrumb) {
        breadCrumb.setChangeListener(new BreadCrumb.OnBreadCrumbChangeListener() {
            @Override
            public void onChange(Deque<BreadCrumbItem> crumbItems) {
                render(crumbItems);
            }
        });
    }

}
