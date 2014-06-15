package org.menesty.ikea.ui.pages;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.dialog.UserDialog;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;


public class UserPage extends BasePage {

    private UserDialog userDialog;

    public UserPage() {
        super("Users");
    }

    @Override
    public Node createView() {

        final TableView<User> tableView = new BaseTableView<User>() {
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


        tableView.setItems(FXCollections.observableArrayList(ServiceFacade.getUserService().load()));

        userDialog = new UserDialog();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);


        ToolBar control = new ToolBar();

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

        borderPane.setTop(control);

        return wrap(borderPane);
    }

    @Override
    protected Node createIconContent() {
        return ImageFactory.createUsersIcon64();
    }
}
