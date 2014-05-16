package org.menesty.ikea.ui.pages;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.domain.IkeaParagon;
import org.menesty.ikea.domain.PagingResult;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.task.IkeaParagonTask;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;


/**
 * Created by Menesty on 5/13/14.
 */
public class IkeaParagonPage extends BasePage {
    private static final int ITEM_PER_PAGE = 20;

    private ParseService parseService;

    private LoadService loadService;

    private Pagination pagination;

    private TableView<IkeaParagon> tableView;

    public IkeaParagonPage() {
        super("Ikea paragons");

        parseService = new ParseService();
        parseService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<Boolean>() {
            @Override
            public void onSucceeded(Boolean value) {
                load();
            }
        });

        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<PagingResult<IkeaParagon>>() {
            @Override
            public void onSucceeded(PagingResult<IkeaParagon> value) {
                tableView.getItems().clear();
                tableView.getItems().addAll(value.getData());

                pagination.setPageCount(value.getCount() / ITEM_PER_PAGE);
            }
        });
    }

    @Override
    public Node createView() {
        ToolBar control = new ToolBar();

        {
            Button button = new Button(null, ImageFactory.createReload32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    loadingPane.bindTask(parseService);
                    parseService.restart();
                }
            });
            control.getItems().add(button);
        }

        BorderPane main = new BorderPane();

        tableView = new BaseTableView<IkeaParagon>() {
            @Override
            protected void onRowRender(TableRow<IkeaParagon> row, IkeaParagon item) {
                row.getStyleClass().remove("greenRow");

                if (item != null && item.isUploaded())
                    row.getStyleClass().add("greenRow");
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


    @Override
    protected Node createIconContent() {
        return ImageFactory.createIkea72Icon();
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
            return new IkeaParagonTask();
        }
    }

}