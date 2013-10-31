package org.menesty.ikea.ui.pages;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.service.OrderService;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.controls.dialog.OrderCreateDialog;
import org.menesty.ikea.ui.controls.dialog.OrderEditDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderListPage extends BasePage {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private TableView<OrderTableItem> tableView;

    private OrderService orderService;

    private OrderEditDialog editDialog;

    public OrderListPage() {
        super("Order list");
        orderService = new OrderService();
        editDialog = new OrderEditDialog();

    }

    @Override
    public Node createView() {
        tableView = createTableView();

        ToolBar control = createToolBar();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);
        borderPane.setTop(control);

        StackPane pane = createRoot();
        pane.getChildren().add(0, borderPane);

        return pane;
    }

    private TableView<OrderTableItem> createTableView() {


        TableColumn<OrderTableItem, Boolean> checked = new TableColumn<>();

        checked.setMinWidth(50);
        checked.setCellValueFactory(new PropertyValueFactory<OrderTableItem, Boolean>("checked"));
        checked.setCellFactory(new Callback<TableColumn<OrderTableItem, Boolean>, TableCell<OrderTableItem, Boolean>>() {
            public TableCell<OrderTableItem, Boolean> call(TableColumn<OrderTableItem, Boolean> p) {
                return new CheckBoxTableCell<>();
            }
        });

        TableColumn<OrderTableItem, String> orderName = new TableColumn<>();

        orderName.setText("Name");
        orderName.setMinWidth(200);
        orderName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderTableItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderTableItem, String> item) {
                return new PathProperty<>(item.getValue(), "order.name");
            }
        });


        TableColumn<OrderTableItem, String> createdDate = new TableColumn<>();

        createdDate.setText("Created Date");
        createdDate.setMinWidth(200);
        createdDate.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<OrderTableItem, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderTableItem, String> item) {
                        return new SimpleStringProperty(DATE_FORMAT.format(item.getValue().getOrder().getCreatedDate()));
                    }
                });

        TableView<OrderTableItem> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(transform(orderService.load())));

        tableView.setRowFactory(new Callback<TableView<OrderTableItem>, TableRow<OrderTableItem>>() {
            @Override
            public TableRow<OrderTableItem> call(final TableView<OrderTableItem> tableView) {
                final TableRow<OrderTableItem> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2 && row.getItem() != null) {
                            showPopupDialog(editDialog);
                            editDialog.bind(row.getItem().getOrder(), new DialogCallback<Order>() {
                                @Override
                                public void onSave(Order user, Object... params) {
                                    orderService.save(user);
                                    hidePopupDialog();
                                    row.setItem(null);
                                }

                                @Override
                                public void onCancel() {
                                    hidePopupDialog();
                                }
                            });
                        }
                    }
                });
                return row;
            }
        });

        tableView.getColumns().addAll(checked, orderName, createdDate);


        return tableView;
    }

    private ToolBar createToolBar() {
        ToolBar control = new ToolBar();
        ImageView imageView = new ImageView(new Image("/styles/images/icon/add1-48x48.png"));
        Button addOrder = new Button("", imageView);
        addOrder.setContentDisplay(ContentDisplay.RIGHT);

        addOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showCreateOrderDialog();
            }
        });

        imageView = new ImageView(new Image("/styles/images/icon/edit-48x48.png"));
        final Button editOrder = new Button("", imageView);
        editOrder.setContentDisplay(ContentDisplay.RIGHT);
        editOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                IkeaApplication.getPageManager().goToPageByName("Order", tableView.getSelectionModel().selectedItemProperty().getValue().getOrder());
            }
        });
        editOrder.setDisable(true);

        tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> change) {
                editOrder.setDisable(change.getList().size() == 0);
            }

        });

        control.getItems().add(addOrder);
        control.getItems().add(editOrder);
        return control;
    }

    private void showCreateOrderDialog() {
        showPopupDialog(new OrderCreateDialog(getStage()) {
            @Override
            public void onCancel() {
                hidePopupDialog();
            }

            @Override
            public void onCreate(String orderName, String filePath) {
                hidePopupDialog();
                try {
                    runTask(new CreateOrderTask(orderName, new FileInputStream(new File(filePath))));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class OrderTableItem {

        private BooleanProperty checked;

        private Order order;


        public BooleanProperty checkedProperty() {
            return checked;
        }

        private OrderTableItem(boolean checked, Order order) {
            this.order = order;
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    System.out.println(OrderTableItem.this.order.getName() + " invited: " + t1);
                }
            });
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public Order getOrder() {
            return order;
        }

        public void setName(String name) {
            order.setName(name);
        }


    }

    private class CreateOrderTask extends Task<Void> {

        private String orderName;

        private InputStream is;

        public CreateOrderTask(String orderName, InputStream is) {
            this.orderName = orderName;
            this.is = is;
        }

        @Override
        protected Void call() throws InterruptedException {
            try {
                Order order = orderService.createOrder(orderName, is, new TaskProgress() {
                    @Override
                    public void updateProgress(long l, long l1) {
                        CreateOrderTask.this.updateProgress(l, l1);
                    }
                });
                orderService.save(order);
                tableView.getItems().clear();
                tableView.getItems().addAll(transform(orderService.load()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private List<OrderTableItem> transform(List<Order> orders) {
        List<OrderTableItem> result = new ArrayList<>();
        for (Order order : orders)
            result.add(new OrderTableItem(false, order));
        return result;

    }
}
