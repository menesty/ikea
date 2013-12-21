package org.menesty.ikea.ui.pages;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.AbstractAsyncService.SucceededListener;
import org.menesty.ikea.service.ServiceFacade;
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

    private OrderEditDialog editDialog;

    private LoadService loadService;

    public OrderListPage() {
        super("CustomerOrder list");
        editDialog = new OrderEditDialog();
        loadService = new LoadService();
        loadService.setOnSucceededListener(new SucceededListener<List<CustomerOrder>>() {
            @Override
            public void onSucceeded(final List<CustomerOrder> value) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tableView.getItems().clear();
                        tableView.getItems().addAll(transform((value)));
                    }
                });
            }
        });
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

        loadingPane.bindTask(loadService);
        loadService.restart();
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

        tableView.setRowFactory(new Callback<TableView<OrderTableItem>, TableRow<OrderTableItem>>() {
            @Override
            public TableRow<OrderTableItem> call(final TableView<OrderTableItem> tableView) {
                final TableRow<OrderTableItem> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2 && row.getItem() != null) {
                            showPopupDialog(editDialog);
                            editDialog.bind(row.getItem().getOrder(), new EntityDialogCallback<CustomerOrder>() {
                                @Override
                                public void onSave(CustomerOrder user, Object... params) {
                                    ServiceFacade.getOrderService().save(user);
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
        Button addOrder = new Button(null, ImageFactory.createAdd48Img());

        addOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showCreateOrderDialog();
            }
        });

        final Button editOrder = new Button(null, ImageFactory.creteEdit48Img());
        editOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                IkeaApplication.getPageManager().goToPageByName("CustomerOrder", tableView.getSelectionModel().getSelectedItem().getOrder());
            }
        });
        editOrder.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                editOrder.setDisable(tableView.getSelectionModel().getSelectedItem() == null);
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

        private CustomerOrder order;


        public BooleanProperty checkedProperty() {
            return checked;
        }

        private OrderTableItem(boolean checked, CustomerOrder order) {
            this.order = order;
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                    System.out.println(OrderTableItem.this.order.getName() + " invited: " + t1);
                }
            });
        }

        public void setOrder(CustomerOrder order) {
            this.order = order;
        }

        public CustomerOrder getOrder() {
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
                ServiceFacade.getOrderService().createOrder(orderName, is, new TaskProgress() {
                    @Override
                    public void updateProgress(long l, long l1) {
                        CreateOrderTask.this.updateProgress(l, l1);
                    }
                });

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadService.restart();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private List<OrderTableItem> transform(List<CustomerOrder> orders) {
        List<OrderTableItem> result = new ArrayList<>();
        for (CustomerOrder order : orders)
            result.add(new OrderTableItem(false, order));

        return result;

    }

    @Override
    protected Node createIconContent() {
        return ImageFactory.createOrders72Img();
    }

}

class LoadService extends AbstractAsyncService<List<CustomerOrder>> {

    @Override
    protected Task<List<CustomerOrder>> createTask() {
        return new Task<List<CustomerOrder>>() {
            @Override
            protected List<CustomerOrder> call() throws Exception {
                return ServiceFacade.getOrderService().load();
            }
        };
    }
}

