package org.menesty.ikea.ui.pages.ikea.product;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.IkeaProduct;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.IkeaProductUpdateService;
import org.menesty.ikea.ui.pages.ikea.order.dialog.product.ProductEditDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.*;

/**
 * Created by Menesty on
 * 4/6/16.
 * 23:12.
 */
public class ProductPage extends BasePage {
  private ProductEditDialog productEditDialog;
  private TextField artNumberField;
  private LoadService loadService;
  private BaseTableView<IkeaProduct> tableView;
  private IkeaProductUpdateService ikeaProductUpdateService;
  private Pagination pagination;

  @Override
  protected void initialize() {
    ikeaProductUpdateService = new IkeaProductUpdateService();
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      tableView.getItems().setAll(value.getData());
      pagination.setPageCount(PaginationUtil.getPageCount(value.getCount(), value.getLimit()));
    });
  }

  @Override
  protected Node createView() {
    BorderPane main = new BorderPane();

    ToolBar filterToolBar = new ToolBar();

    {
      artNumberField = new TextField();
      artNumberField.setDelay(1);
      artNumberField.setOnDelayAction(actionEvent -> filter());
      artNumberField.setPromptText("Product ID #");

      filterToolBar.getItems().add(artNumberField);
    }

    {
      Button button = new Button(null, ImageFactory.createClear16Icon());
      button.setOnAction(event -> {
        artNumberField.setText(null);
      });
      filterToolBar.setTooltip(ToolTipUtil.create(I18n.UA.getString(I18nKeys.CLEAR)));
      filterToolBar.getItems().add(button);
    }

    main.setTop(filterToolBar);

    tableView = new BaseTableView<>();

    {
      TableColumn<IkeaProduct, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));
      column.setMinWidth(130);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));
      column.setCellFactory(ArtNumberCell::new);

      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaProduct, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));
      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.number("price"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<IkeaProduct, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));
      column.setMinWidth(300);
      column.setCellValueFactory(ColumnUtil.column("shortName"));

      tableView.getColumns().add(column);
    }


    tableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.EDIT));
          menuItem.setOnAction(event -> {
            ProductEditDialog productEditDialog = getProductEditDialog(getDialogSupport().getStage());
            productEditDialog.bind(newValue, new EntityDialogCallback<IkeaProduct>() {
              @Override
              public void onSave(IkeaProduct ikeaProduct, Object... params) {
                tableView.update(newValue);

                ikeaProductUpdateService.setIkeaProduct(ikeaProduct);
                ikeaProductUpdateService.restart();

                getDialogSupport().hidePopupDialog();
              }

              @Override
              public void onCancel() {
                getDialogSupport().hidePopupDialog();
              }
            });

            getDialogSupport().showPopupDialog(productEditDialog);
          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });
    main.setCenter(tableView);


    pagination = new Pagination(1, 0);

    pagination.currentPageIndexProperty().addListener((observable, oldValue, pageIndex) -> {
      loadService.setPage(PaginationUtil.getPageNumber(pageIndex.intValue()));
      loadService.restart();
    });

    main.setBottom(pagination);

    return wrap(main);
  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(loadService, ikeaProductUpdateService);
    loadService.restart();
  }

  private void filter() {
    loadService.setPage(1);
    loadService.restart();

  }

  private ProductEditDialog getProductEditDialog(Stage stage) {
    if (productEditDialog == null) {
      productEditDialog = new ProductEditDialog(stage);
    }

    return productEditDialog;
  }

  class LoadService extends AbstractAsyncService<PageResult<IkeaProduct>> {
    private IntegerProperty pageProperty = new SimpleIntegerProperty();

    @Override
    protected Task<PageResult<IkeaProduct>> createTask() {
      final int _page = pageProperty.get();
      final String _artNumber = artNumberField.getText();

      return new Task<PageResult<IkeaProduct>>() {
        @Override
        protected PageResult<IkeaProduct> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/products/" + (_artNumber != null ? _artNumber + "/" : "") + _page);

          return request.getData(new TypeReference<PageResult<IkeaProduct>>() {
          });
        }
      };
    }

    public void setPage(int page) {
      this.pageProperty.set(page);
    }
  }
}
