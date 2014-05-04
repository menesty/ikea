package org.menesty.ikea.ui.controls.component;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.dialog.ParagonManageDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.search.WarehouseItemSearchData;
import org.menesty.ikea.ui.controls.search.WarehouseSearchBar;
import org.menesty.ikea.ui.controls.table.component.CheckBoxTableColumn;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpUtil;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WarehouseViewComponent extends BorderPane {
    private final LoadService loadService;
    private TableView<WarehouseItemDtoTableItem> tableView;
    private LoadingPane loadingPane;

    private List<WarehouseItemDto> items;

    private ParagonManageDialog paragonManageDialog;

    public WarehouseViewComponent() {
        paragonManageDialog = new ParagonManageDialog() {
            @Override
            public void onCreate() {
                load();
            }
        };

        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<WarehouseItemDto>>() {
            @Override
            public void onSucceeded(final List<WarehouseItemDto> value) {
                setItems(items = value);
            }
        });

        tableView = new TableView<>();
        {
            TableColumn<WarehouseItemDtoTableItem, Boolean> checked = new CheckBoxTableColumn<>();
            checked.setCellValueFactory(new PropertyValueFactory<WarehouseItemDtoTableItem, Boolean>("checked"));
            tableView.getColumns().add(checked);
        }

        {
            TableColumn<WarehouseItemDtoTableItem, String> column = new TableColumn<>("Product Number");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDtoTableItem, String>column("item.productNumber"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDtoTableItem, String> column = new TableColumn<>("Short Number");
            column.setMinWidth(250);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDtoTableItem, String>column("item.shortName"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDtoTableItem, Double> column = new TableColumn<>("Price");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDtoTableItem, Double>column("item.price"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDtoTableItem, Double> column = new TableColumn<>("Count");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDtoTableItem, Double>column("item.count"));
            tableView.getColumns().add(column);
        }

        tableView.setEditable(true);

        VBox controlBox = new VBox();

        {
            ToolBar control = new ToolBar();

            {
                Button button = new Button(null, ImageFactory.createReload32Icon());
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        load();
                    }
                });
                control.getItems().add(button);
            }

            {
                Button button = new Button(null, ImageFactory.createAdd32Icon());
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        paragonManageDialog.show(getChecked());
                        IkeaApplication.get().showPopupDialog(paragonManageDialog);
                    }
                });
                control.getItems().add(button);
            }

            controlBox.getChildren().add(control);
        }
        controlBox.getChildren().add(new WarehouseSearchBar() {
            @Override
            public void onSearch(WarehouseItemSearchData data) {
                setItems(filter(data));
            }
        });

        setCenter(tableView);
        setTop(controlBox);
    }

    private void setItems(List<WarehouseItemDto> items) {
        tableView.setItems(FXCollections.observableList(transform(items)));
    }

    private List<WarehouseItemDto> getChecked() {
        List<WarehouseItemDto> result = new ArrayList<>();

        for (WarehouseItemDtoTableItem item : tableView.getItems())
            if (item.isChecked())
                result.add(item.getItem());

        return result;
    }

    private List<WarehouseItemDtoTableItem> transform(List<WarehouseItemDto> items) {
        List<WarehouseItemDtoTableItem> result = new ArrayList<>();

        for (WarehouseItemDto item : items)
            result.add(new WarehouseItemDtoTableItem(false, item));

        return result;
    }

    public List<WarehouseItemDto> filter(WarehouseItemSearchData data) {
        if (data.price == null || data.price == 0)
            return items;

        List<WarehouseItemDto> result = new ArrayList<>();

        for (WarehouseItemDto item : items)
            if (item.price <= data.price)
                result.add(item);

        return result;
    }

    public void load() {
        loadingPane.bindTask(loadService);
        loadService.restart();
    }

    public void bindLoading(final LoadingPane loadingPane) {
        this.loadingPane = loadingPane;
    }

    public class WarehouseItemDtoTableItem {

        private BooleanProperty checked;

        private final WarehouseItemDto item;

        public WarehouseItemDtoTableItem(boolean checked, WarehouseItemDto item) {
            this.item = item;
            this.checked = new SimpleBooleanProperty(checked);

            this.checked.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                    System.out.println(oldValue + "=" + newValue);
                    WarehouseItemDtoTableItem.this.checked.getValue();

                }
            });
            this.checked.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    WarehouseItemDtoTableItem.this.checked.getValue();
                }
            });
        }

        public BooleanProperty checkedProperty() {
            return checked;
        }

        public boolean isChecked() {
            return checked.get();
        }

        public WarehouseItemDto getItem() {
            return item;
        }

    }

    class LoadService extends AbstractAsyncService<List<WarehouseItemDto>> {

        @Override
        protected Task<List<WarehouseItemDto>> createTask() {
            return new Task<List<WarehouseItemDto>>() {
                @Override
                protected List<WarehouseItemDto> call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/storage/load");
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    List<WarehouseItemDto> result = new ArrayList<>();
                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet(url.toURI());

                        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
                            String data = EntityUtils.toString(response.getEntity());

                            GsonBuilder gson = new GsonBuilder();
                            gson.registerTypeAdapter(WarehouseItemDto.class, new WarehouseItemDtoAdapter());

                            Type collectionType = new TypeToken<Collection<WarehouseItemDto>>() {
                            }.getType();

                            Collection<WarehouseItemDto> dataList = gson.create().fromJson(data, collectionType);
                            //filter not visible
                            for (WarehouseItemDto itemDto : dataList)
                                if (itemDto.visible)
                                    result.add(itemDto);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return result;
                }
            };
        }
    }

}

class WarehouseItemDtoAdapter implements JsonDeserializer<WarehouseItemDto> {

    @Override
    public WarehouseItemDto deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonElement = element.getAsJsonObject();

        WarehouseItemDto entity = new WarehouseItemDto();

        entity.price = jsonElement.get("price").getAsDouble();
        entity.count = jsonElement.get("count").getAsDouble();
        entity.orderId = jsonElement.get("orderId").getAsInt();
        entity.productId = jsonElement.get("productId").getAsString();
        entity.productNumber = jsonElement.get("productNumber").getAsString();
        entity.shortName = jsonElement.get("shortName").getAsString();
        entity.visible = jsonElement.get("visible").getAsInt() > 0;
        entity.allowed = jsonElement.get("allowed").getAsInt() > 0;
        entity.zestav = jsonElement.get("zestav").getAsInt() > 0;
        entity.weight = jsonElement.get("weight").getAsDouble();

        return entity;
    }
}

