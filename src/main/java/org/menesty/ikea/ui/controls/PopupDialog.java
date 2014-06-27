package org.menesty.ikea.ui.controls;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * User: Menesty
 * Date: 10/9/13
 * Time: 7:29 PM
 */
public class PopupDialog extends StackPane {
    private boolean allowAutoHide;

    public PopupDialog() {
        setId("ModalDimmer");
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent t) {
                t.consume();
                if (allowAutoHide)
                    hideModalMessage();
            }
        });
        setVisible(false);
    }


    public void showModalMessage(Node message, boolean allowAutoHide) {
        this.allowAutoHide = allowAutoHide;
        getChildren().add(message);
        setOpacity(0);
        setVisible(true);
        setCache(true);

        new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>() {
                            public void handle(ActionEvent t) {
                                setCache(false);
                            }
                        },
                        new KeyValue(opacityProperty(), 1, Interpolator.EASE_BOTH)
                )
        ).play();
    }

    /**
     * Hide any modal message that is shown
     */
    public void hideModalMessage() {
        hideModalMessage(true);
    }

    public void hideModalMessage(boolean animate) {
        if (animate) {
            setCache(true);

            new Timeline(
                    new KeyFrame(Duration.seconds(1),
                            new EventHandler<ActionEvent>() {
                                public void handle(ActionEvent t) {
                                    setCache(false);
                                    setVisible(false);
                                    getChildren().clear();
                                }
                            },
                            new KeyValue(opacityProperty(), 0, Interpolator.EASE_BOTH)
                    )
            ).play();
        } else {
            setCache(false);
            setVisible(false);
            getChildren().clear();
        }
    }
}
