package org.menesty.ikea;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.BreadcrumbBar;
import org.menesty.ikea.ui.controls.MainToolBar;
import org.menesty.ikea.ui.controls.PopupDialog;
import org.menesty.ikea.ui.controls.WindowResizeButton;
import org.menesty.ikea.ui.controls.dialog.ApplicationPreferenceDialog;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.*;

/**
 * User: Menesty
 * Date: 10/9/13
 * Time: 6:11 PM
 */
public class IkeaApplication extends Application {
    private WindowResizeButton windowResizeButton;
    private Pane pageArea;
    private PopupDialog modalDimmer;

    private static PageManager pageManager;
    private static IkeaApplication instance;
    private Stage stage;


    private ApplicationPreferenceDialog preferenceDialog;


    public static PageManager getPageManager() {
        return pageManager;
    }

    public static IkeaApplication get() {
        return instance;
    }

    @Override
    public void start(final Stage stage) throws Exception {
        instance = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        this.stage = stage;
        stage.setTitle("Ikea Logistic Application");

        LoadingPane loadingPane;
        // set default docs location
        StackPane layerPane = new StackPane();
        stage.initStyle(StageStyle.UNDECORATED);
        // create window resize button
        windowResizeButton = new WindowResizeButton(stage, 1020, 650);
        // create root
        BorderPane root = new BorderPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                windowResizeButton.autosize();
                windowResizeButton.setLayoutX(getWidth() - windowResizeButton.getLayoutBounds().getWidth());
                windowResizeButton.setLayoutY(getHeight() - windowResizeButton.getLayoutBounds().getHeight());
            }
        };
        root.getStyleClass().add("application");

        root.setId("root");
        layerPane.setDepthTest(DepthTest.DISABLE);
        loadingPane = new LoadingPane();
        layerPane.getChildren().addAll(root, loadingPane);
        // create scene
        boolean is3dSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        Scene scene = new Scene(layerPane, 1020, 650, is3dSupported);
        if (is3dSupported) {
            //RT-13234
            scene.setCamera(new PerspectiveCamera());
        }
        scene.getStylesheets().add("/styles/application.css");
        // create modal dimmer, to dim screen when showing modal dialogs
        modalDimmer = new PopupDialog();

        layerPane.getChildren().add(modalDimmer);
        // create main toolbar
        ToolBar toolBar = new MainToolBar(stage);
        // add close min max

        ToolBar pageToolBar = new ToolBar();
        pageToolBar.setId("page-toolbar");
        pageToolBar.setMinHeight(29);
        pageToolBar.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);


        BreadcrumbBar breadcrumbBar = new BreadcrumbBar();
        pageToolBar.getItems().add(breadcrumbBar);
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);


        Button settingsButton = new Button(null, ImageFactory.createSetting22Icon());
        settingsButton.setId("SettingsButton");
        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showPopupDialog(getPreferenceDialog());
            }
        });
        settingsButton.setMaxHeight(Double.MAX_VALUE);
        pageToolBar.getItems().addAll(spacer3, settingsButton);
        // create page area
        pageArea = new Pane() {
            @Override
            protected void layoutChildren() {
                for (Node child : pageArea.getChildren()) {
                    child.resizeRelocate(0, 0, pageArea.getWidth(), pageArea.getHeight());
                }
            }
        };
        pageArea.setId("page-area");
        // create right split pane
        BorderPane rightSplitPane = new BorderPane();
        rightSplitPane.setTop(pageToolBar);
        rightSplitPane.setCenter(pageArea);
        // create split pane

        root.setTop(toolBar);
        root.setCenter(rightSplitPane);
        // add window resize button so its on top
        windowResizeButton.setManaged(false);
        root.getChildren().add(windowResizeButton);

        stage.setScene(scene);
        stage.show();


        pageManager = new PageManager(pageArea, breadcrumbBar);


        OrderListPage orderListPage = new OrderListPage();
        pageManager.register(new CategoryPage("IKEA", orderListPage, new ProductPage(), new UserPage(),
                new WarehousePage(), new CustomInvoicePage(), new IkeaParagonPage()));

        OrderViewPage orderViewPage = new OrderViewPage();
        orderViewPage.setBreadCrumbPath(orderListPage.getBreadCrumb());
        pageManager.register(orderViewPage);
        pageManager.goToPage("IKEA");

        Task<Void> initDbTask = DatabaseService.init();
        loadingPane.bindTask(initDbTask);
        initDbTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                pageManager.goToPage("IKEA/" + Pages.ORDERS.getTitle());
            }
        });
    }

    private BaseDialog getPreferenceDialog() {
        if (preferenceDialog == null)
            preferenceDialog = new ApplicationPreferenceDialog() {
                @Override
                public void onCancel() {
                    hidePopupDialog();
                }

                @Override
                public void onOk() {
                    super.onOk();
                    hidePopupDialog();
                }
            };
        return preferenceDialog;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DatabaseService.close();
    }

    public void showPopupDialog(BaseDialog node) {
        modalDimmer.showModalMessage(node, node.isAllowAutoHide());
        node.onShow();
    }

    public void hidePopupDialog() {
        modalDimmer.hideModalMessage();
    }

    public Stage getStage() {
        return stage;
    }

    public static void main(String... args) {
        launch(args);
    }


}


