package org.menesty.ikea.application;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.menesty.ikea.core.component.CategoryGroup;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.core.component.PageDescription;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumb;
import org.menesty.ikea.core.component.breadcrumb.BreadCrumbView;
import org.menesty.ikea.core.component.ui.ApplicationControlToolBar;
import org.menesty.ikea.core.component.ui.ApplicationWindow;
import org.menesty.ikea.core.component.ui.BreadCrumbToolBar;
import org.menesty.ikea.core.component.ui.WorkspaceArea;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.PopupDialog;
import org.menesty.ikea.ui.controls.dialog.ApplicationPreferenceDialog;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.dialog.ErrorConsoleDialog;
import org.menesty.ikea.ui.controls.dialog.InfoDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.Pages;
import org.menesty.ikea.ui.pages.ikea.contraget.ContragentPage;
import org.menesty.ikea.ui.pages.ikea.log.WarehouseScanLogPage;
import org.menesty.ikea.ui.pages.ikea.order.IkeaOrderViewPage;
import org.menesty.ikea.ui.pages.ikea.order.IkeaProcessOrderPage;
import org.menesty.ikea.ui.pages.ikea.product.ProductPage;
import org.menesty.ikea.ui.pages.ikea.reports.order.OrderSummaryReportPage;
import org.menesty.ikea.ui.pages.ikea.resumption.ResumptionDetailPage;
import org.menesty.ikea.ui.pages.ikea.resumption.ResumptionPage;
import org.menesty.ikea.ui.pages.ikea.warehouse.SiteWarehousePage;
import org.menesty.ikea.ui.pages.wizard.order.OrderCreateWizardPage;
import org.menesty.ikea.util.HttpUtil;

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
    Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
      ServiceFacade.getErrorConsole().add(throwable);
    });

    HttpUtil.initAuthenticator();

    ApplicationWindow rootPane = new ApplicationWindow(stage, 1020, 650);

    BorderPane mainPane = new BorderPane();
    mainPane.getStyleClass().add("application");
    mainPane.setId("root");

    mainPane.setTop(new ApplicationControlToolBar(stage));


    StackPane pageContainer = new StackPane();

    BreadCrumb breadCrumb = new BreadCrumb();
    pageArea = new WorkspaceArea(breadCrumb, this);


    pageArea.addCategory(createSiteGroup());

    BreadCrumbToolBar.ControlActionListener controlActionListener = new BreadCrumbToolBar.ControlActionListener() {
      @Override
      public void onSetting() {
        showPopupDialog(getPreferenceDialog());
      }

      @Override
      public void onInfo() {
        showPopupDialog(getInfoDialog());
      }
    };

    BreadCrumbView.OnBreadCrumbItemClickListener changeListener = pageArea::navigate;

    BreadCrumbToolBar breadCrumbToolBar = new BreadCrumbToolBar(breadCrumb, controlActionListener, changeListener);


    final Button errorConsoleButton = new Button(null, ImageFactory.createError22Icon());
    ServiceFacade.getErrorConsole().errorsProperty().get().addListener((ListChangeListener<Throwable>) c -> {
      if (!c.getList().isEmpty()) {
        Platform.runLater(() ->
            errorConsoleButton.setText("(" + c.getList().size() + ")"));
      }
    });
    errorConsoleButton.setOnAction(event -> showPopupDialog(getErrorDialog()));

    breadCrumbToolBar.addControlButton(errorConsoleButton);


    BorderPane pagePane = new BorderPane();
    pagePane.setTop(breadCrumbToolBar);


    pagePane.setCenter(pageArea);


    pageContainer.getChildren().addAll(pagePane, loadingPane = new LoadingPane());


    mainPane.setCenter(pageContainer);
    rootPane.addFirst(mainPane);
    rootPane.add(modalDimmer = new PopupDialog());

    show(stage, rootPane);
  }

  private CategoryGroup createSiteGroup() {
    CategoryGroup group = new CategoryGroup("Site");

    PageDescription ikeaOrderList = new PageDescription(Pages.SITE_ORDERS.getTitle(), ImageFactory.createSiteOrders72Icon(), IkeaProcessOrderPage.class);

    ikeaOrderList.addPage(new PageDescription(Pages.ORDER_WIZARD.getTitle(), OrderCreateWizardPage.class, false));
    ikeaOrderList.addPage(new PageDescription(Pages.ORDER_REPORT.getTitle(), OrderSummaryReportPage.class, false));
    ikeaOrderList.addPage(new PageDescription(Pages.ORDER_DETAIL.getTitle(), IkeaOrderViewPage.class, false));


    group.add(ikeaOrderList);

    group.add(new PageDescription(Pages.WAREHOUSE.getTitle(), ImageFactory.createWarehouseIcon72(), SiteWarehousePage.class));
    group.add(new PageDescription(Pages.CONTRAGENT.getTitle(), ImageFactory.createUsersIcon64(), ContragentPage.class));

    {
      PageDescription resumptionListPageDescription = new PageDescription(Pages.RESUMPTION.getTitle(), ImageFactory.createResumptionIcon72(), ResumptionPage.class);
      resumptionListPageDescription.addPage(new PageDescription(Pages.RESUMPTION_DETAIL.getTitle(), ResumptionDetailPage.class, false));

      group.add(resumptionListPageDescription);
    }

    group.add(new PageDescription(Pages.SCAN_LOG.getTitle(), ImageFactory.createLogIcon72(), WarehouseScanLogPage.class));
    group.add(new PageDescription(Pages.PRODUCTS.getTitle(), ImageFactory.createProducts72Icon(), ProductPage.class));

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
    preferenceDialog.setDefaultAction(dialog -> {
      dialog.setDefaultAction(null);
      hidePopupDialog();
    });

    return preferenceDialog;
  }

  private BaseDialog getErrorDialog() {
    ErrorConsoleDialog errorConsoleDialog = new ErrorConsoleDialog(getStage());
    errorConsoleDialog.setDefaultAction(dialog -> {
      dialog.setDefaultAction(null);
      hidePopupDialog();
    });

    return errorConsoleDialog;
  }

  private BaseDialog getInfoDialog() {
    InfoDialog dialog = new InfoDialog(getStage());
    dialog.setDefaultAction(dialog1 -> {
      dialog1.setDefaultAction(null);
      hidePopupDialog();
    });
    return dialog;
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
  }


  public static void main(String... args) {
    launch(args);
  }
}
