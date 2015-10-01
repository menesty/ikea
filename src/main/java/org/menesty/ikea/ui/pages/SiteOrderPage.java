package org.menesty.ikea.ui.pages;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.menesty.ikea.domain.StorageComboLack;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ClientOrder;
import org.menesty.ikea.lib.dto.PageResult;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.Field;
import org.menesty.ikea.util.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Menesty on
 * 7/3/15.
 * 17:54.
 */
public class SiteOrderPage extends BasePage {

    private SiteOrderAsyncService siteOrderAsyncService;
    private TableView<ClientOrder> tableView;
    private Pagination pagination;

    public SiteOrderPage() {
    }

    @Override
    protected void initialize() {
        siteOrderAsyncService = new SiteOrderAsyncService();
        siteOrderAsyncService.setOnSucceededListener(value -> {
            tableView.getItems().clear();
            tableView.getItems().addAll(value.getData());

            pagination.setPageCount(value.pages());
        });
    }

    @Override
    protected Node createView() {
        pagination = new Pagination(1, 0);

        pagination.currentPageIndexProperty().addListener((observable, oldValue, pageIndex) -> {
            siteOrderAsyncService.setPageIndex(pageIndex.intValue());
            siteOrderAsyncService.restart();
        });

        BorderPane borderPane = new BorderPane();

        borderPane.setCenter(tableView = createTableView());
        borderPane.setTop(createToolBar());
        borderPane.setBottom(pagination);

        return wrap(borderPane);
    }

    private TableView<ClientOrder> createTableView() {
        TableView<ClientOrder> tableView = new TableView<>();

        {
            TableColumn<ClientOrder, String> column = new TableColumn<>();

            column.setText(I18n.UA.getString(I18nKeys.CREATED_DATE));
            column.setMinWidth(130);
            column.setCellValueFactory(ColumnUtil.<ClientOrder>dateColumn(Fields.CREATED_DATE));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ClientOrder, String> column = new TableColumn<>();

            column.setText(I18n.UA.getString(I18nKeys.AMOUNT));
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.number(Fields.AMOUNT));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ClientOrder, String> column = new TableColumn<>();

            column.setText(I18n.UA.getString(I18nKeys.FIRST_NAME));
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.column(FieldsUtil.build(Fields.CLIENT_ORDER_INFORMATION, Fields.CLIENT_RECIPIENT, Fields.FIRST_NAME)));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ClientOrder, String> column = new TableColumn<>();

            column.setText(I18n.UA.getString(I18nKeys.LAST_NAME));
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.column(FieldsUtil.build(Fields.CLIENT_ORDER_INFORMATION, Fields.CLIENT_RECIPIENT, Fields.LAST_NAME)));

            tableView.getColumns().add(column);
        }

        {
            TableColumn<ClientOrder, String> column = new TableColumn<>();

            column.setText(I18n.UA.getString(I18nKeys.ORDER_STATUS));
            column.setMinWidth(150);
            column.setCellValueFactory(cellFactory -> new SimpleStringProperty(Fields.ORDER_STATUS) {
                @Override
                public String get() {
                    String value = super.get();

                    if (value != null) {
                        I18n.UA.getString(value);
                    }

                    return null;
                }
            });

            tableView.getColumns().add(column);
        }
        return tableView;
    }

    private VBox createToolBar() {
        VBox controlBox = new VBox();

        ToolBar toolBar = new ToolBar();

        Button createSiteOrder = new Button(null, ImageFactory.createAdd32Icon());
        createSiteOrder.setTooltip(new Tooltip(I18n.UA.getString(I18nKeys.CREATE_SITE_ORDER)));
        createSiteOrder.setOnAction(eh -> {

        });

        toolBar.getItems().add(createSiteOrder);

        controlBox.getChildren().add(toolBar);

        return controlBox;
    }

    @Override
    public void onActive(Object... params) {
        siteOrderAsyncService.restart();
    }

    class SiteOrderAsyncService extends AbstractAsyncService<PageResult<ClientOrder>> {
        private SimpleIntegerProperty pageIndex = new SimpleIntegerProperty(1);

        @Override
        protected Task<PageResult<ClientOrder>> createTask() {
            final int _pageIndex = pageIndex.get();
            return new Task<PageResult<ClientOrder>>() {
                @Override
                protected PageResult<ClientOrder> call() throws Exception {
                    APIRequest apiRequest = HttpServiceUtil.get("/site-order/page/" + _pageIndex);

                    return apiRequest.getData(new TypeReference<PageResult<ClientOrder>>() {
                    });
                }
            };
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex.set(pageIndex);
        }
    }
}
