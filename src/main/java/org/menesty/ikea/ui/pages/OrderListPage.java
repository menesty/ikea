package org.menesty.ikea.ui.pages;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.db.DatabaseService;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.PagingResult;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.AbstractAsyncService.SucceededListener;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.TaskProgress;
import org.menesty.ikea.ui.controls.MToolBar;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.dialog.OrderCreateDialog;
import org.menesty.ikea.ui.controls.dialog.OrderEditDialog;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class OrderListPage extends BasePage {

    private TableView<OrderTableItem> tableView;

    private OrderEditDialog editDialog;

    private LoadService loadService;

    private Pagination pagination;

    private static final int ITEM_PER_PAGE = 5;

    public OrderListPage() {
        super(Pages.ORDERS.getTitle());
    }

    @Override
    protected void initialize() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(new SucceededListener<PagingResult<CustomerOrder>>() {
            @Override
            public void onSucceeded(final PagingResult<CustomerOrder> value) {
                tableView.getItems().clear();
                tableView.getItems().addAll(transform(value.getData()));

                pagination.setPageCount(value.getCount() / ITEM_PER_PAGE);
            }
        });
    }

    @Override
    public Node createView() {
        tableView = createTableView();

        ToolBar control = createToolBar();

        pagination = new Pagination(1, 0);

        pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number pageIndex) {
                loadService.setPageIndex(pageIndex.intValue());
                loadService.restart();
            }
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);
        borderPane.setTop(control);
        borderPane.setBottom(pagination);

        return wrap(borderPane);
    }

    public void onActive(Object... params) {
        loadingPane.bindTask(loadService);
        loadService.setPageIndex(0);
        loadService.restart();
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
        orderName.setCellValueFactory(ColumnUtil.<OrderTableItem, String>column("order.name"));

        TableColumn<OrderTableItem, String> createdDate = new TableColumn<>();

        createdDate.setText("Created Date");
        createdDate.setMinWidth(200);
        createdDate.setCellValueFactory(ColumnUtil.<OrderTableItem>dateColumn("order.createdDate"));

        TableView<OrderTableItem> tableView = new BaseTableView<OrderTableItem>() {
            @Override
            protected void onRowRender(TableRow<OrderTableItem> row, OrderTableItem newValue) {
                row.getStyleClass().remove("productNotVerified");

                if (newValue != null && row.getItem().getOrder().isSynthetic())
                    row.getStyleClass().add("productNotVerified");
            }

            @Override
            protected void onRowDoubleClick(final TableRow<OrderTableItem> row) {
                if (row.getItem().getOrder().isSynthetic())
                    return;

                showPopupDialog(getEditDialog());
                getEditDialog().bind(row.getItem().getOrder(), new EntityDialogCallback<CustomerOrder>() {
                    @Override
                    public void onSave(CustomerOrder entity, Object... params) {
                        row.getItem().setOrder(ServiceFacade.getOrderService().save(entity));
                        hidePopupDialog();
                        update(row.getItem());
                    }

                    @Override
                    public void onCancel() {
                        hidePopupDialog();
                    }
                });
            }
        };

        tableView.getColumns().addAll(checked, orderName, createdDate);


        return tableView;
    }

    private OrderEditDialog getEditDialog() {
        if (editDialog == null)
            editDialog = new OrderEditDialog();

        return editDialog;
    }

    private ToolBar createToolBar() {
        ToolBar control = new MToolBar();
        Button addOrder = new Button(null, ImageFactory.createAdd48Icon());

        addOrder.setTooltip(new Tooltip("Create Order"));
        addOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showCreateOrderDialog();
            }
        });

        final Button editOrder = new Button(null, ImageFactory.creteInfo48Icon());
        editOrder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                IkeaApplication.getPageManager().goToPageByName("CustomerOrder", tableView.getSelectionModel().getSelectedItem().getOrder());
            }
        });
        editOrder.setTooltip(new Tooltip("View order"));
        editOrder.setDisable(true);

        final Button deleteBtn = new Button(null, ImageFactory.createDelete48Icon());
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Dialog.confirm("Are you sure want delete selected Order", new DialogCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onYes() {
                        OrderTableItem item = tableView.getSelectionModel().getSelectedItem();
                        ServiceFacade.getOrderService().remove(item.getOrder());
                        tableView.getItems().remove(item);
                    }
                });
            }
        });
        deleteBtn.setDisable(true);

        tableView.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                boolean selected = tableView.getSelectionModel().getSelectedItem() == null;
                editOrder.setDisable(selected);
                deleteBtn.setDisable(selected);
            }
        });

        control.getItems().addAll(addOrder, editOrder, deleteBtn);
        return control;
    }

    private void showCreateOrderDialog() {
        showPopupDialog(new OrderCreateDialog(getStage()) {
            @Override
            public void onCancel() {
                hidePopupDialog();
            }

            @Override
            public void onCreate(String orderName, int margin, String filePath) {
                hidePopupDialog();

                if (filePath == null) {
                    ServiceFacade.getOrderService().save(new CustomerOrder(orderName).setSynthetic(true).setMargin(margin));
                    loadService.restart();
                } else
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
        return ImageFactory.createOrders72Icon();
    }


    class LoadService extends AbstractAsyncService<PagingResult<CustomerOrder>> {
        private SimpleIntegerProperty pageIndex = new SimpleIntegerProperty();

        @Override
        protected Task<PagingResult<CustomerOrder>> createTask() {
            final int _pageIndex = pageIndex.get();

            return new Task<PagingResult<CustomerOrder>>() {
                @Override
                protected PagingResult<CustomerOrder> call() throws Exception {

                    return DatabaseService.runInTransaction(new Callable<PagingResult<CustomerOrder>>() {
                        @Override
                        public PagingResult<CustomerOrder> call() throws Exception {
                            PagingResult<CustomerOrder> result = new PagingResult<>();

                            result.setData(ServiceFacade.getOrderService().load(_pageIndex * ITEM_PER_PAGE, ITEM_PER_PAGE));
                            result.setCount((int) ServiceFacade.getOrderService().count());

                            return result;
                        }
                    });
                }
            };
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex.set(pageIndex);
        }
    }
}



