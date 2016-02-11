package org.menesty.ikea.ui.pages.ikea.contraget;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.Contragent;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.contraget.dialog.ContragentEditDialog;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.ToolTipUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 1/31/16.
 * 21:57.
 */
public class ContragentPage extends BasePage {
  private BaseTableView<Contragent> tableView;

  private LoadService loadService;
  private SaveService saveService;

  private ContragentEditDialog contragentEditDialog;

  @Override
  protected void initialize() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> tableView.getItems().setAll(value));

    saveService = new SaveService();
    saveService.setOnSucceededListener(value -> tableView.getItems().setAll(value));
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    tableView = new BaseTableView<>();

    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.FIRST_NAME));
      column.setMinWidth(150);
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("firstName"));
      tableView.getColumns().add(column);
    }
    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.LAST_NAME));
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("lastName"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.DOCUMENT_NUMBER));
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("documentNumber"));
      column.setMinWidth(150);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.REGION));
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("region"));
      column.setMinWidth(200);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    {
      TableColumn<Contragent, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ADDRESS));
      column.setCellValueFactory(ColumnUtil.<Contragent, String>column("address"));
      column.setMinWidth(200);
      column.getStyleClass().add("align-right");
      tableView.getColumns().add(column);
    }

    tableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.EDIT), ImageFactory.createEdit16Icon());
          menuItem.setOnAction(event -> {
            ContragentEditDialog dialog = getContragentEditDialog();

            dialog.bind(row.getItem(), new EntityDialogCallback<Contragent>() {
              @Override
              public void onSave(Contragent contragent, Object... params) {
                saveService.setContragent(contragent);
                saveService.restart();

                getDialogSupport().hidePopupDialog();
              }

              @Override
              public void onCancel() {
                getDialogSupport().hidePopupDialog();
              }
            });

            getDialogSupport().showPopupDialog(dialog);
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });

    main.setCenter(tableView);

    ToolBar toolBar = new ToolBar();

    {
      Button button = new Button(null, ImageFactory.createAdd32Icon());
      button.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.ADD)));
      button.setOnAction(event -> {
        ContragentEditDialog dialog = getContragentEditDialog();

        dialog.bind(new Contragent(), new EntityDialogCallback<Contragent>() {
          @Override
          public void onSave(Contragent contragent, Object... params) {
            saveService.setContragent(contragent);
            saveService.restart();

            getDialogSupport().hidePopupDialog();
          }

          @Override
          public void onCancel() {
            getDialogSupport().hidePopupDialog();
          }
        });

        getDialogSupport().showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    main.setTop(toolBar);

    return wrap(main);
  }

  protected ContragentEditDialog getContragentEditDialog() {
    if (contragentEditDialog == null) {
      contragentEditDialog = new ContragentEditDialog(getStage());
    }

    return contragentEditDialog;
  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(loadService, saveService);
    loadService.restart();
  }
}

class LoadService extends AbstractAsyncService<List<Contragent>> {

  @Override
  protected Task<List<Contragent>> createTask() {
    return new Task<List<Contragent>>() {
      @Override
      protected List<Contragent> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/contragents");

        return request.getList(new TypeReference<List<Contragent>>() {
        });
      }
    };
  }
}

class SaveService extends AbstractAsyncService<List<Contragent>> {
  private ObjectProperty<Contragent> contragentProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<List<Contragent>> createTask() {
    Contragent _contragent = contragentProperty.get();

    return new Task<List<Contragent>>() {
      @Override
      protected List<Contragent> call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/contragent/save");

        return request.postData(_contragent, new TypeReference<List<Contragent>>() {
        });
      }
    };
  }

  public void setContragent(Contragent contragent) {
    contragentProperty.setValue(contragent);
  }
}
