package org.menesty.ikea.core.component.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumb;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumbView;
import org.menesty.ikea.factory.ImageFactory;

/**
 * Created by Menesty on
 * 10/11/14.
 * 10:46.
 */
public class BreadCrumbToolBar extends ToolBar {
    public interface ControlActionListener {
        void onSetting();

        void onInfo();
    }

    private final ControlActionListener controlActionListener;

    public BreadCrumbToolBar(BreadCrumb breadCrumb, final ControlActionListener controlActionListener,
                             BreadCrumbView.OnBreadCrumbItemClickListener changeListener) {
        this.controlActionListener = controlActionListener;
        setId("page-toolbar");
        setMinHeight(29);
        setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = new Button(null, ImageFactory.createSetting22Icon());

        settingsButton.setId("SettingsButton");
        settingsButton.setMaxHeight(Double.MAX_VALUE);

        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controlActionListener.onSetting();
            }
        });

        Button iconButton = new Button(null, ImageFactory.createInfo22Icon());
        iconButton.setId("infoButton");
        iconButton.setMaxHeight(Double.MAX_VALUE);
        iconButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controlActionListener.onInfo();
            }
        });


        BreadCrumbView breadCrumbView = new BreadCrumbView();
        breadCrumbView.setListener(changeListener);
        breadCrumbView.register(breadCrumb);

        getItems().addAll(breadCrumbView, spacer, settingsButton, iconButton);
    }
}
