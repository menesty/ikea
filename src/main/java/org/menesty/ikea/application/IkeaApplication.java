package org.menesty.ikea.application;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.menesty.ikea.core.component.CategoryGroup;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.core.component.PageDescription;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumb;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumbItem;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumbView;
import org.menesty.ikea.core.component.ui.ApplicationControlToolBar;
import org.menesty.ikea.core.component.ui.ApplicationWindow;
import org.menesty.ikea.core.component.ui.BreadCrumbToolBar;
import org.menesty.ikea.core.component.ui.WorkspaceArea;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.ui.controls.PopupDialog;
import org.menesty.ikea.ui.controls.dialog.ApplicationPreferenceDialog;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.*;

/**
 * Created by Menesty on
 * 10/10/14.
 * 20:48.
 */
public class IkeaApplication extends Application implements DialogSupport {
    private PopupDialog modalDimmer;
    private LoadingPane loadingPane;
    private WorkspaceArea pageArea;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        ApplicationWindow rootPane = new ApplicationWindow(stage, 1020, 650);

        BorderPane mainPane = new BorderPane();
        mainPane.getStyleClass().add("application");
        mainPane.setId("root");

        mainPane.setTop(new ApplicationControlToolBar(stage));


        StackPane pageContainer = new StackPane();

        BreadCrumb breadCrumb = new BreadCrumb();
        pageArea = new WorkspaceArea(breadCrumb, this);


        pageArea.addCategory(createIkeaCategoryGroup());


        EventHandler<ActionEvent> settingsListener = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showPopupDialog(getPreferenceDialog());
            }
        };

        BreadCrumbView.OnBreadCrumbItemClickListener changeListener = new BreadCrumbView.OnBreadCrumbItemClickListener() {
            @Override
            public void onItemClick(BreadCrumbItem item) {
                pageArea.navigate(item);
            }
        };

        BreadCrumbToolBar breadCrumbToolBar = new BreadCrumbToolBar(breadCrumb, settingsListener, changeListener);


        BorderPane pagePane = new BorderPane();
        pagePane.setTop(breadCrumbToolBar);


        pagePane.setCenter(pageArea);


        pageContainer.getChildren().addAll(pagePane, loadingPane = new LoadingPane());


        mainPane.setCenter(pageContainer);
        rootPane.addFirst(mainPane);
        rootPane.add(modalDimmer = new PopupDialog());

        show(stage, rootPane);
        initDatabase();
    }

    private void initDatabase() {
        Task<Void> initDbTask = DatabaseService.init();
        loadingPane.bindTask(initDbTask);
        initDbTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                pageArea.navigate(pageArea.getCategories().get(0).getItems().get(0));
            }
        });

    }

    private CategoryGroup createIkeaCategoryGroup() {
        CategoryGroup group = new CategoryGroup("IKEA");
        PageDescription orderList = new PageDescription(Pages.ORDERS.getTitle(), ImageFactory.createOrders72Icon(), OrderListPage.class);
        orderList.addPage(new PageDescription(Pages.CUSTOMER_ORDER.getTitle(), OrderViewPage.class, false));

        group.add(orderList);
        group.add(new PageDescription(Pages.PRODUCTS.getTitle(), ImageFactory.createProducts72Icon(), ProductPage.class));
        group.add(new PageDescription(Pages.USERS.getTitle(), ImageFactory.createUsersIcon64(), UserPage.class));
        group.add(new PageDescription(Pages.WAREHOUSE.getTitle(), ImageFactory.createWarehouseIcon72(), WarehousePage.class));
        group.add(new PageDescription(Pages.INVOICE.getTitle(), ImageFactory.createInvoice72Icon(), CustomInvoicePage.class));
        group.add(new PageDescription(Pages.IKEA_PARAGONS.getTitle(), ImageFactory.createIkea72Icon(), IkeaParagonPage.class));
        group.add(new PageDescription(Pages.SHOPS.getTitle(), ImageFactory.createShopIcon72(), IkeaShopPage.class));

        return group;
    }

    private void show(Stage stage, Pane rootPane) {
        boolean is3dSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        Scene scene = new Scene(rootPane, 1020, 650, is3dSupported);
        scene.getStylesheets().add("/styles/application.css");

        if (is3dSupported) {
            scene.setCamera(new PerspectiveCamera());
        }
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
    }

    private BaseDialog getPreferenceDialog() {
        ApplicationPreferenceDialog preferenceDialog = new ApplicationPreferenceDialog(getStage());
        preferenceDialog.setDefaultAction(new BaseDialog.DefaultAction() {
            @Override
            public void defaultAction(BaseDialog dialog) {
                dialog.setDefaultAction(null);
                hidePopupDialog();
            }
        });

        return preferenceDialog;
    }

    @Override
    public void showPopupDialog(BaseDialog node) {
        modalDimmer.showModalMessage(node, node.isAllowAutoHide());
        node.onShow();
    }

    @Override
    public void hidePopupDialog() {
        hidePopupDialog(true);
    }

    @Override
    public void hidePopupDialog(boolean animate) {
        modalDimmer.hideModalMessage(animate);
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void navigate(PageDescription parent, Class<? extends BasePage> subPage, Object... params) {
        pageArea.navigate(parent, subPage, params);
    }

    @Override
    public PageDescription getActivePage() {
        return pageArea.getActivePage();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DatabaseService.close();
    }


    public static void main(String... args) {
        launch(args);
    }
}
