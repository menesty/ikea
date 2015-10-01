package org.menesty.ikea.ui.pages;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.menesty.ikea.domain.CustomerOrder;
import org.menesty.ikea.domain.IkeaParagon;
import org.menesty.ikea.domain.PagingResult;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.SucceededListener;
import org.menesty.ikea.service.task.IkeaFamilyExportParagonTask;
import org.menesty.ikea.service.task.IkeaFamilyParagonTask;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;

/**
 * Created by Menesty on
 * 5/13/14.
 */
public class IkeaParagonPage extends BasePage {
    private static final int ITEM_PER_PAGE = 20;

    private ParseService parseService;

    private LoadService loadService;

    private Pagination pagination;

    private BaseTableView<IkeaParagon> tableView;

    private ExportService exportService;

    private ExportParagonService exportParagonService;

    public IkeaParagonPage() {
    }

    @Override
    protected void initialize() {
        parseService = new ParseService();
        parseService.setOnSucceededListener(value -> load());

        loadService = new LoadService();
        loadService.setOnSucceededListener(value -> {
            tableView.getItems().clear();
            tableView.getItems().addAll(value.getData());

            pagination.setPageCount(value.getCount() / ITEM_PER_PAGE);
        });

        exportService = new ExportService();


        exportParagonService = new ExportParagonService();
        exportParagonService.setOnSucceededListener(value -> tableView.update(exportParagonService.getParagon()));
    }

    @Override
    public Node createView() {
        ToolBar control = new ToolBar();

        final CustomerOrder order = ServiceFacade.getOrderService().getLatest();

        {
            Button button = new Button(null, ImageFactory.createReload32Icon());
            button.setOnAction(actionEvent -> {
                loadingPane.bindTask(parseService);
                parseService.restart();
            });

            control.getItems().add(button);
        }

        {
            Button button = new Button(null, ImageFactory.createXlsExport32Icon());
            button.setOnAction(actionEvent -> {
                FileChooser fileChooser = FileChooserUtil.getXls();

                File selectedFile = fileChooser.showSaveDialog(getStage());

                if (selectedFile != null) {
                    exportService.setPath(selectedFile.getAbsolutePath());
                    loadingPane.bindTask(exportService);

                    FileChooserUtil.setDefaultDir(selectedFile);

                    exportService.restart();
                }
            });

            control.getItems().add(button);
        }
        BorderPane main = new BorderPane();

        tableView = new BaseTableView<IkeaParagon>() {
            @Override
            protected void onRowRender(TableRow<IkeaParagon> row, final IkeaParagon item) {
                row.getStyleClass().remove("greenRow");
                row.setContextMenu(null);

                if (item != null && item.isUploaded())
                    row.getStyleClass().add("greenRow");

                if (item != null && !item.isUploaded() && order != null) {
                    ContextMenu menu = new ContextMenu();
                    {
                        MenuItem menuItem = new MenuItem("Export to Order");
                        menuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                exportParagonService.setParagon(item);
                                exportParagonService.setOrder(order);

                                loadingPane.bindTask(exportParagonService);
                                exportParagonService.restart();
                            }
                        });

                        menu.getItems().add(menuItem);
                    }

                    row.setContextMenu(menu);
                }
            }
        };

        {
            TableColumn<IkeaParagon, String> column = new TableColumn<>("Date");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<IkeaParagon>dateColumn("createdDate"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaParagon, String> column = new TableColumn<>("# paragon");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<IkeaParagon, String>column("name"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaParagon, String> column = new TableColumn<>("Shop");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<IkeaParagon, String>column("shopName"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaParagon, String> column = new TableColumn<>("Payment");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<IkeaParagon, String>column("paymentType"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<IkeaParagon, Double> column = new TableColumn<>("Price");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<IkeaParagon, Double>column("price"));
            tableView.getColumns().add(column);
        }

        main.setCenter(tableView);
        main.setTop(control);

        pagination = new Pagination(1, 0);

        pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number pageIndex) {
                loadService.setPageIndex(pageIndex.intValue());
                loadingPane.bindTask(loadService);
                loadService.restart();
            }
        });

        main.setBottom(pagination);

        return wrap(main);
    }

    public void onActive(Object... params) {
        load();
    }

    private void load() {
        loadingPane.bindTask(loadService);
        loadService.setPageIndex(0);
        loadService.restart();
    }


    class LoadService extends AbstractAsyncService<PagingResult<IkeaParagon>> {
        private SimpleIntegerProperty pageIndex = new SimpleIntegerProperty();

        @Override
        protected Task<PagingResult<IkeaParagon>> createTask() {
            final int _pageIndex = pageIndex.get();

            return new Task<PagingResult<IkeaParagon>>() {
                @Override
                protected PagingResult<IkeaParagon> call() throws Exception {
                    PagingResult<IkeaParagon> result = new PagingResult<>();
                    result.setData(ServiceFacade.getIkeaParagonService().load(_pageIndex * ITEM_PER_PAGE, ITEM_PER_PAGE));
                    result.setCount((int) ServiceFacade.getIkeaParagonService().count());
                    return result;
                }
            };
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex.set(pageIndex);
        }
    }

    class ParseService extends AbstractAsyncService<Boolean> {
        @Override
        protected Task<Boolean> createTask() {
            return new IkeaFamilyParagonTask();
        }
    }

    class ExportService extends AbstractAsyncService<Void> {
        private SimpleStringProperty path = new SimpleStringProperty();

        @Override
        protected Task<Void> createTask() {
            final String _path = path.get();
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    ServiceFacade.getIkeaParagonService().exportToXls(_path);
                    return null;
                }
            };
        }

        public void setPath(String path) {
            this.path.set(path);
        }
    }

    class ExportParagonService extends AbstractAsyncService<Boolean> {
        private SimpleObjectProperty<IkeaParagon> paragon = new SimpleObjectProperty<>();
        private SimpleObjectProperty<CustomerOrder> order = new SimpleObjectProperty<>();

        @Override
        protected Task<Boolean> createTask() {
            final IkeaParagon _paragon = paragon.get();
            final CustomerOrder _order = order.get();

            return new IkeaFamilyExportParagonTask(_paragon, _order);
        }

        public void setParagon(IkeaParagon paragon) {
            this.paragon.set(paragon);
        }

        public void setOrder(CustomerOrder order) {
            this.order.set(order);
        }

        public IkeaParagon getParagon() {
            return paragon.get();
        }
    }

}