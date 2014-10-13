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

    public BreadCrumbToolBar(BreadCrumb breadCrumb, EventHandler<ActionEvent> settingListener,
                             BreadCrumbView.OnBreadCrumbItemClickListener changeListener) {
        setId("page-toolbar");
        setMinHeight(29);
        setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = new Button(null, ImageFactory.createSetting22Icon());

        settingsButton.setId("SettingsButton");
        settingsButton.setMaxHeight(Double.MAX_VALUE);

        settingsButton.setOnAction(settingListener);

        BreadCrumbView breadCrumbView = new BreadCrumbView();
        breadCrumbView.setListener(changeListener);
        breadCrumbView.register(breadCrumb);

        getItems().addAll(breadCrumbView, spacer, settingsButton);
    }
}
