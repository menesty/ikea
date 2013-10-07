package org.menesty.ikea;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.menesty.ikea.ui.ScreensController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {

       /* log.info("Starting Hello JavaFX and Maven demonstration application");

        String fxmlFile = "/fxml/intro-page.fxml";
        log.debug("Loading FXML for main view from: {}", fxmlFile);
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

        log.debug("Showing JFX scene");
        Scene scene = new Scene(rootNode, 400, 200);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Hello JavaFX and Maven");
        stage.setScene(scene);
        stage.show();*/



        ScreensController mainContainer = new ScreensController();
        mainContainer.loadScreen("main", "/fxml/intro-page.fxml");
        mainContainer.loadScreen("orders", "/fxml/orders-page.fxml");

        mainContainer.setScreen("main");

        Scene scene = new Scene(mainContainer, 400, 200);
        scene.getStylesheets().add("/styles/styles.css");


        stage.setTitle("Hello JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
    }
}
