package org.menesty.ikea.ui.pages.ikea.resumption;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.apache.http.client.methods.HttpDelete;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.resumption.Resumption;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.ikea.resumption.component.ResumptionItemComponent;
import org.menesty.ikea.ui.pages.ikea.resumption.component.ResumptionItemSearchComponent;
import org.menesty.ikea.ui.pages.ikea.resumption.dialog.ResumptionItemAddDialog;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 2/24/16.
 * 15:59.
 */
public class ResumptionPage extends BasePage {
  private BaseTableView<Resumption> resumptionTableView;
  private LoadService loadService;
  private DeleteService deleteService;
  private ResumptionItemSearchComponent resumptionItemSearchComponent;
  private ResumptionItemComponent resumptionItemComponent;

  @Override
  protected void initialize() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      resumptionTableView.getItems().setAll(value);
      resumptionItemSearchComponent.setResumptions(value);
    });

    deleteService = new DeleteService();
    deleteService.setOnSucceededListener(value -> {
      if (value) {
        loadService.restart();
      }
    });
  }

  @Override
  protected Node createView() {
    TabPane tabPane = new TabPane();

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.RESUMPTION));
      tab.setClosable(false);

      BorderPane main = new BorderPane();

      resumptionTableView = new BaseTableView<>();

      {
        TableColumn<Resumption, Number> column = new TableColumn<>();
        column.setMaxWidth(45);
        column.setCellValueFactory(ColumnUtil.<Resumption>indexColumn());
        resumptionTableView.getColumns().add(column);
      }

      {
        TableColumn<Resumption, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.CREATED_DATE));
        column.setMinWidth(150);
        column.getStyleClass().add("align-right");
        column.setCellValueFactory(ColumnUtil.<Resumption>dateColumn("createdDate"));
        resumptionTableView.getColumns().add(column);
      }

      resumptionTableView.setRowRenderListener((row, newValue1) -> {
        row.setContextMenu(null);

        if (newValue1 != null) {
          ContextMenu contextMenu = new ContextMenu();

          {
            MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.DELETE), ImageFactory.createDelete16Icon());
            menuItem.setOnAction(event -> {
              Dialog.confirm(getDialogSupport(), I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(I18nKeys.DELETE_CONFIRMATION_MESSAGE), new DialogCallback() {
                @Override
                public void onCancel() {
                  getDialogSupport().hidePopupDialog();
                }

                @Override
                public void onYes() {
                  getDialogSupport().hidePopupDialog();

                  deleteService.setResumptionId(newValue1.getId());
                  deleteService.restart();
                }
              });
            });

            contextMenu.getItems().add(menuItem);
          }

          row.setContextMenu(contextMenu);
        }
      });

      main.setCenter(resumptionTableView);


      ToolBar toolBar = new ToolBar();

      {
        Button button = new Button(null, ImageFactory.creteInfo48Icon());

        button.setOnAction(event -> navigateSubPage(ResumptionDetailPage.class, resumptionTableView.getSelectionModel().getSelectedItem()));
        button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.INFO)));
        button.setDisable(true);

        resumptionTableView.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> button.setDisable(newValue == null));
        toolBar.getItems().add(button);
      }

      main.setTop(toolBar);

      tab.setContent(main);

      tabPane.getTabs().add(tab);
    }

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.RESUMPTION_ITEM_SEARCH));
      tab.setClosable(false);

      tab.setContent(resumptionItemSearchComponent = new ResumptionItemSearchComponent(getDialogSupport()));
      tabPane.getTabs().add(tab);
    }

    {
      Tab tab = new Tab(I18n.UA.getString(I18nKeys.RESUMPTION_CORRECTION));

      tab.setClosable(false);

      tab.setContent(resumptionItemComponent = new ResumptionItemComponent(getDialogSupport()));
      tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue) {
          resumptionItemComponent.load();
        }
      });

      tabPane.getTabs().add(tab);
    }

    return wrap(tabPane);
  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(loadService);
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<Resumption>> {

    @Override
    protected Task<List<Resumption>> createTask() {
      return new Task<List<Resumption>>() {
        @Override
        protected List<Resumption> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/list");

          return request.getData(new TypeReference<List<Resumption>>() {
          });
        }
      };
    }
  }

  class DeleteService extends AbstractAsyncService<Boolean> {
    private LongProperty resumptionIdProperty = new SimpleLongProperty();

    @Override
    protected Task<Boolean> createTask() {
      final Long _resumptionId = resumptionIdProperty.get();

      return new Task<Boolean>() {
        @Override
        protected Boolean call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/resumption/" + _resumptionId + "/delete");

          return request.getData(Boolean.class, HttpDelete.METHOD_NAME);
        }
      };
    }

    public void setResumptionId(Long resumptionId) {
      resumptionIdProperty.setValue(resumptionId);
    }
  }
}
