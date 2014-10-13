package org.menesty.ikea.ui.pages;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.dialog.UserDialog;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;

import java.util.List;


public class UserPage extends BasePage {

    private UserDialog userDialog;

    private LoadService loadService;

    private TableView<User> tableView;

    public UserPage() {
        super("Users");
    }

    @Override
    protected void initialize() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<User>>() {
            @Override
            public void onSucceeded(List<User> value) {
                tableView.getItems().clear();
                tableView.getItems().addAll(value);
            }
        });
    }

    @Override
    public Node createView() {

        tableView = new BaseTableView<User>() {
            @Override
            protected void onRowDoubleClick(TableRow<User> row) {
                showPopupDialog(userDialog);
                userDialog.bind(row.getItem(), new EntityDialogCallback<User>() {
                    @Override
                    public void onSave(User user, Object... params) {
                        ServiceFacade.getUserService().save(user);
                        hidePopupDialog();
                        update(user);
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }
        };

        {
            TableColumn<User, Number> column = new TableColumn<>();
            column.setMaxWidth(40);
            column.setCellValueFactory(ColumnUtil.<User>indexColumn());
            tableView.getColumns().add(column);
        }

        {
            TableColumn<User, String> column = new TableColumn<>("Login");
            column.setMinWidth(200);
            column.setCellValueFactory(ColumnUtil.<User, String>column("login"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<User, String> column = new TableColumn<>("Password");
            column.setMinWidth(200);
            column.setCellValueFactory(ColumnUtil.<User, String>column("password"));
            tableView.getColumns().add(column);
        }

        userDialog = new UserDialog(getStage());

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);


        ToolBar control = new ToolBar();

        {
            Button createUser = new Button(null, ImageFactory.createAdd48Icon());
            createUser.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    showPopupDialog(userDialog);
                    userDialog.bind(new User(), new EntityDialogCallback<User>() {
                        @Override
                        public void onSave(User user, Object... params) {
                            ServiceFacade.getUserService().save(user);
                            tableView.setItems(FXCollections.observableArrayList(ServiceFacade.getUserService().load()));
                            hidePopupDialog();
                        }

                        @Override
                        public void onCancel() {
                            hidePopupDialog();
                        }
                    });
                }
            });
            control.getItems().add(createUser);
        }

        {
            Button button = new Button(null, ImageFactory.createDelete48Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    final User item = tableView.getSelectionModel().getSelectedItem();

                    if (item == null)
                        return;

                    Dialog.confirm(getDialogSupport(), "Warning", "Are you sure that you want to delete this item ?", new DialogCallback() {
                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onYes() {
                            try {
                                ServiceFacade.getUserService().remove(item);
                                load();
                            } catch (Exception e) {
                                Dialog.alert(getDialogSupport(), "Waring", "Can not delete selected user it is used");
                            }

                        }
                    });
                }
            });

            control.getItems().add(button);
        }

        borderPane.setTop(control);

        return wrap(borderPane);
    }

    @Override
    public void onActive(Object... params) {
        load();
    }

    private void load() {
        loadService.restart();
    }

    class LoadService extends AbstractAsyncService<List<User>> {
        @Override
        protected Task<List<User>> createTask() {
            return new Task<List<User>>() {
                @Override
                protected List<User> call() throws Exception {
                    return ServiceFacade.getUserService().load();
                }
            };
        }
    }
}
