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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.Order;
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

public class OrderListPage extends BasePage {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private TableView<OrderTableItem> tableView;

    private OrderService orderService;

    private Region maskRegion;

    private ProgressIndicator progressIndicator;

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

        maskRegion = new Region();
        maskRegion.setVisible(false);
        maskRegion.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(150, 150);
        progressIndicator.setVisible(false);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(borderPane, maskRegion, progressIndicator);
        pane.getChildren().add(stack);

        return pane;
    }

    private TableView<OrderTableItem> createTableView() {


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
                return new PathProperty<>(item.getValue(), "order.name");
            }
        });
        orderName.setCellFactory(TextFieldTableCell.forTableColumn());
        orderName.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<OrderTableItem, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<OrderTableItem, String> t) {
                OrderTableItem orderTableItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                orderTableItem.setName(t.getNewValue());
                orderService.save(orderTableItem.getOrder());
            }
        });

        TableColumn createdDate = new TableColumn();

        createdDate.setText("Created Date");
        createdDate.setMinWidth(200);
        createdDate.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<OrderTableItem, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<OrderTableItem, String> item) {
                        return new SimpleStringProperty(DATE_FORMAT.format(item.getValue().getOrder().getCreatedDate()));
                    }
                });

        TableView tableView = new TableView();
        tableView.setItems(FXCollections.observableArrayList(transform(orderService.load())));
        tableView.setEditable(true);

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
                    Task<Void> task = new CreateOrderTask(orderName, new FileInputStream(new File(filePath)));
                    progressIndicator.progressProperty().bind(task.progressProperty());
                    maskRegion.visibleProperty().bind(task.runningProperty());
                    progressIndicator.visibleProperty().bind(task.runningProperty());

                    new Thread(task).start();
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
