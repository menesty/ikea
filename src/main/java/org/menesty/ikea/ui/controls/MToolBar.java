package org.menesty.ikea.ui.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class MToolBar extends ToolBar {
    private int spacing = 5;

    public MToolBar() {
        initialize();
    }

    public MToolBar(Node... nodes) {
        super(nodes);
        initialize();
    }


    private void initialize() {
        orientationProperty().addListener(new ChangeListener<Orientation>() {
            @Override
            public void changed(ObservableValue<? extends Orientation> observableValue, Orientation orientation, Orientation orientation2) {
                List<Node> items = getItems();
                for (Node item : items)
                    applyMargin(orientation2, item);
            }
        });

        getItems().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> change) {
                for (Node item : change.getList())
                    applyMargin(getOrientation(), item);
            }
        });
    }

    private void applyMargin(Orientation orientation2, Node node) {
        Insets insets = new Insets(0, spacing, 0, 0);
        if (Orientation.HORIZONTAL == orientation2)
            HBox.setMargin(node, insets);
        else
            VBox.setMargin(node, insets);
    }
}
