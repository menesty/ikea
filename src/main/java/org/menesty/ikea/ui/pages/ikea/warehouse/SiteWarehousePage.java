package org.menesty.ikea.ui.pages.ikea.warehouse;

import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.ui.pages.BasePage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 12/11/15.
 * 18:04.
 */
public class SiteWarehousePage extends BasePage {
  private WarehouseViewComponent warehouseViewComponent;
  private ParagonViewComponent paragonViewComponent;

  public SiteWarehousePage() {
  }

  @Override
  public void onActive(Object... params) {
    List<Worker<?>> workers = new ArrayList<>();

    workers.addAll(paragonViewComponent.getWorkers());
    workers.addAll(warehouseViewComponent.getWorkers());

    loadingPane.bindTask(workers.toArray(new Worker<?>[workers.size()]));
    warehouseViewComponent.load();
    paragonViewComponent.load();
  }

  @Override
  protected Node createView() {
    warehouseViewComponent = new WarehouseViewComponent(getDialogSupport());
    paragonViewComponent = new ParagonViewComponent(getDialogSupport());

    TabPane tabPane = new TabPane();

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.WAREHOUSE));
      tab.setClosable(false);
      tab.setContent(warehouseViewComponent);
      tabPane.getTabs().add(tab);
    }

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.PARAGONS));
      tab.setClosable(false);
      tab.setContent(paragonViewComponent);
      tabPane.getTabs().add(tab);
    }

    return wrap(tabPane);
  }
}
