package org.menesty.ikea.ui.pages;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.menesty.ikea.domain.User;
import org.menesty.ikea.service.UserService;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.UserDialog;


public class UserPage extends BasePage {

    private UserDialog userDialog;

    private UserService userService;

    public UserPage() {
        super("Users");
        userService = new UserService();
    }

    @Override
    public Node createView() {

        final TableView<User> tableView = new TableView<>();
        {
            TableColumn<User, String> column = new TableColumn<>("Login");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> item) {
                    return new PathProperty<>(item.getValue(), "login");
                }
            });
            tableView.getColumns().add(column);
        }

        {
            TableColumn<User, String> column = new TableColumn<>("Password");
            column.setMinWidth(200);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> item) {
                    return new PathProperty<>(item.getValue(), "password");
                }
            });
            tableView.getColumns().add(column);
        }

        tableView.setRowFactory(new Callback<TableView<User>, TableRow<User>>() {
            @Override
            public TableRow<User> call(final TableView<User> rawInvoiceProductItemTableView) {
                final TableRow<User> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2)
                            showPopupDialog(userDialog);
                        userDialog.bind(row.getItem(), new DialogCallback<User>() {
                            @Override
                            public void onSave(User user, Object... params) {
                                userService.save(user);
                                hidePopupDialog();
                                row.setItem(null);
                            }

                            @Override
                            public void onCancel() {
                                hidePopupDialog();
                            }
                        });
                    }
                });
                return row;
            }
        });

        tableView.setItems(FXCollections.observableArrayList(userService.load()));

        userDialog = new UserDialog();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);


        ToolBar control = new ToolBar();
        ImageView imageView = new ImageView(new Image("/styles/images/icon/add1-48x48.png"));
        Button createUser = new Button("", imageView);
        createUser.setContentDisplay(ContentDisplay.RIGHT);

        createUser.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showPopupDialog(userDialog);
                userDialog.bind(new User(), new DialogCallback<User>() {
                    @Override
                    public void onSave(User user, Object... params) {
                        userService.save(user);
                        tableView.setItems(FXCollections.observableArrayList(userService.load()));
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

        StackPane pane = createRoot();
        pane.getChildren().add(0, borderPane);

        return pane;
    }

    @Override
    protected Node createIconContent() {
        return new ImageView(new javafx.scene.image.Image("/styles/images/icon/users-64x64.png"));
    }
}
