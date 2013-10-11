package org.menesty.ikea.ui.pages;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.menesty.ikea.domain.Order;
import org.menesty.ikea.order.OrderItem;
import org.menesty.ikea.service.OrderService;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.PathProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class OrderListPage extends BasePage {

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private TableView<OrderTableItem> tableView;

    private OrderService orderService;

    public OrderListPage() {
        super("Order list");
        orderService = new OrderService();
    }

    @Override
    public Node createView() {
        tableView = createTableView();

        ToolBar control = createToolBar();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);
        borderPane.setTop(control);

        Pane pane = createRoot();
        pane.getChildren().add(borderPane);

        return pane;
    }

    private TableView<OrderTableItem> createTableView() {
        final ObservableList<OrderTableItem> data = FXCollections.observableArrayList(
                new OrderTableItem(false, new Order("Test", new Date())),
                new OrderTableItem(true, new Order("Test 2", new Date())),
                new OrderTableItem(false, new Order("Test 3", new Date()))
        );

        TableColumn checked = new TableColumn<OrderTableItem, Boolean>();

        checked.setMinWidth(50);
        checked.setCellValueFactory(new PropertyValueFactory("checked"));
        checked.setCellFactory(new Callback<TableColumn<OrderTableItem, Boolean>, TableCell<OrderTableItem, Boolean>>() {
            public TableCell<OrderTableItem, Boolean> call(TableColumn<OrderTableItem, Boolean> p) {
                return new CheckBoxTableCell<>();
            }
        });

        TableColumn orderName = new TableColumn();

        orderName.setText("Name");
        orderName.setMinWidth(200);
        orderName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OrderTableItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderTableItem, String> item) {
                return new PathProperty(item.getValue(), "order.name", String.class);
            }
        });
        orderName.setCellFactory(TextFieldTableCell.forTableColumn());
        orderName.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<OrderTableItem, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<OrderTableItem, String> t) {
                OrderTableItem orderTableItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                orderTableItem.setName(t.getNewValue());
            }
        });

        TableColumn createdDate = new TableColumn();

        createdDate.setText("Created Date");
        createdDate.setMinWidth(200);
        createdDate.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<OrderTableItem, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderTableItem, String> item) {
                        return new SimpleStringProperty(dateFormat.format(item.getValue().getOrder().getCreatedDate()));
                    }
                });

        TableView tableView = new TableView();
        tableView.setItems(data);
        tableView.setEditable(true);
        tableView.getColumns().addAll(checked, orderName, createdDate);
        return tableView;
    }

    private ToolBar createToolBar() {
        ToolBar control = new ToolBar();
        ImageView imageView = new ImageView(new Image("/styles/images/icon/add1-48x48.png"));
        Button addOrder = new Button("", imageView);
        addOrder.setContentDisplay(ContentDisplay.RIGHT);
        control.getItems().add(addOrder);
        addOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showCreateOrderDialog();
            }
        });

        ProgressIndicator p1 = new ProgressIndicator();
        p1.setProgress(0.5F);
        control.getItems().add(p1);
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
                    new Thread(new GetDailySalesTask(orderName, new FileInputStream(new File(filePath)))).start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class OrderTableItem {
        private BooleanProperty checked;

        private Order order;

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

    private class GetDailySalesTask extends Task<ObservableList<OrderTableItem>> {

        private String orderName;

        private InputStream is;

        public GetDailySalesTask(String orderName, InputStream is) {
            this.orderName = orderName;
            this.is = is;
        }

        @Override
        protected ObservableList<OrderTableItem> call() throws InterruptedException {
            orderService.createOrder(orderName, is, new TaskProgress() {
                @Override
                public void updateProgress(long l, long l1) {
                    GetDailySalesTask.this.updateProgress(l, l1);

                }
            });

            ObservableList<OrderTableItem> orderTableItems = FXCollections.observableArrayList();
            orderTableItems.addAll(transform(orderService.load()));
            return orderTableItems;
        }
    }

    private List<OrderTableItem> transform(List<Order> orders) {
        List<OrderTableItem> result = new ArrayList<>();
        for (Order order : orders)
            result.add(new OrderTableItem(false, order));
        return result;

    }
}
