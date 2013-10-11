package org.menesty.ikea;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.menesty.ikea.ui.controls.*;
import org.menesty.ikea.ui.pages.CategoryPage;
import org.menesty.ikea.ui.pages.OrderListPage;
import org.menesty.ikea.ui.pages.PageManager;

/**
 * User: Menesty
 * Date: 10/9/13
 * Time: 6:11 PM
 */
public class IkeaApplication extends Application {
    private WindowResizeButton windowResizeButton;
    private BorderPane root;
    private Scene scene;
    private ToolBar toolBar;
    private Pane pageArea;
    private PopupDialog modalDimmer;

    private ToolBar pageToolBar;
    private BreadcrumbBar breadcrumbBar;

    private static PageManager pageManager;
    private static IkeaApplication instance;
    private Stage stage;

    public static PageManager getPageManager() {
        return pageManager;
    }

    public static IkeaApplication get(){
        return instance;
    }

    @Override
    public void start(final Stage stage) throws Exception {
        this.instance = this;
        this.stage = stage;
        stage.setTitle("Ensemble");
        // set default docs location
        StackPane layerPane = new StackPane();
        stage.initStyle(StageStyle.UNDECORATED);
        // create window resize button
        windowResizeButton = new WindowResizeButton(stage, 1020, 700);
        // create root
        root = new BorderPane() {
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
        layerPane.getChildren().add(root);
        // create scene
        boolean is3dSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        scene = new Scene(layerPane, 1020, 700, is3dSupported);
        if (is3dSupported) {
            //RT-13234
            scene.setCamera(new PerspectiveCamera());
        }
        scene.getStylesheets().add("/styles/application.css");
        // create modal dimmer, to dim screen when showing modal dialogs
        modalDimmer = new PopupDialog();

        layerPane.getChildren().add(modalDimmer);
        // create main toolbar
        toolBar = new MainToolBar(stage);
        // add close min max

        pageToolBar = new ToolBar();
        pageToolBar.setId("page-toolbar");
        pageToolBar.setMinHeight(29);
        pageToolBar.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);


        breadcrumbBar = new BreadcrumbBar();
        pageToolBar.getItems().add(breadcrumbBar);
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        Button settingsButton = new Button();
        settingsButton.setId("SettingsButton");
        settingsButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/styles/images/settings.png"))));
        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // showProxyDialog();
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

        this.root.setTop(toolBar);
        this.root.setCenter(rightSplitPane);
        // add window resize button so its on top
        windowResizeButton.setManaged(false);
        this.root.getChildren().add(windowResizeButton);
        // expand first level of the tree
        // goto initial page
        // show stage
        stage.setScene(scene);
        stage.show();

        pageManager = new PageManager(pageArea, breadcrumbBar);

        CategoryPage main = new CategoryPage("IKEA", new OrderListPage());
        pageManager.register(main);

        pageManager.goToPage("IKEA/Order list");
    }

    public void showPopupDialog(Node node){
        modalDimmer.showModalMessage(node);
    }

    public void hidePopupDialog(){
        modalDimmer.hideModalMessage();
    }

    public Stage getStage(){
        return stage;
    }

    public static void main(String... args) {
        launch(args);
    }


}


