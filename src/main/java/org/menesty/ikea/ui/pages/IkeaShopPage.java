package org.menesty.ikea.ui.pages;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.domain.IkeaShop;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.SucceededListener;
import org.menesty.ikea.ui.controls.dialog.IkeaShopDialog;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;

import java.util.List;

/**
 * Created by Menesty on
 * 6/15/14.
 * 16:01.
 */
public class IkeaShopPage extends BasePage {
    private LoadService loadService;
    private TableView<IkeaShop> tableView;
    private IkeaShopDialog dialog;

    public IkeaShopPage() {
        super(Pages.SHOPS.getTitle());
    }

    @Override
    protected void initialize() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(value -> {
            tableView.getItems().clear();
            tableView.getItems().addAll(value);
        });

        dialog = new IkeaShopDialog(getStage());
    }

    @Override
    protected Node createView() {
        BorderPane main = new BorderPane();

        ToolBar toolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(actionEvent -> {
                showPopupDialog(dialog);
                dialog.bind(new IkeaShop(), new EntityDialogCallback<IkeaShop>() {
                    @Override
                    public void onSave(IkeaShop ikeaShop, Object... params) {
                        ServiceFacade.getIkeaShopService().save(ikeaShop);
                        hidePopupDialog();
                        load();
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            });

            toolBar.getItems().add(button);
        }

        tableView = new BaseTableView<IkeaShop>() {
            @Override
            protected void onRowDoubleClick(TableRow<IkeaShop> row) {
                showPopupDialog(dialog);
                dialog.bind(row.getItem(), new EntityDialogCallback<IkeaShop>() {
                    @Override
                    public void onSave(IkeaShop ikeaShop, Object... params) {
                        ServiceFacade.getIkeaShopService().save(ikeaShop);
                        hidePopupDialog();
                        update(ikeaShop);
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }
        };

        {
            TableColumn<IkeaShop, String> column = new TableColumn<>("Name");
            column.setCellValueFactory(ColumnUtil.<IkeaShop, String>column("name"));
            column.setMinWidth(200);

            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaShop, String> column = new TableColumn<>("Ikea Shop ID");
            column.setCellValueFactory(ColumnUtil.<IkeaShop>number("shopId"));
            column.setMinWidth(120);

            tableView.getColumns().add(column);
        }

        main.setCenter(tableView);
        main.setTop(toolBar);

        return wrap(main);
    }

    @Override
    public void onActive(Object... params) {
        load();
    }

    private void load() {
        loadingPane.bindTask(loadService);
        loadService.restart();
    }

}

class LoadService extends AbstractAsyncService<List<IkeaShop>> {
    @Override
    protected Task<List<IkeaShop>> createTask() {
        return new Task<List<IkeaShop>>() {
            @Override
            protected List<IkeaShop> call() throws Exception {
                return ServiceFacade.getIkeaShopService().load();
            }
        };
    }
}
