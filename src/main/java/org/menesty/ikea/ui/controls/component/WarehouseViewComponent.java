package org.menesty.ikea.ui.controls.component;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpUtil;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WarehouseViewComponent extends BorderPane {
    private final LoadService loadService;
    private TableView<WarehouseItemDto> tableView;
    private LoadingPane loadingPane;

    public WarehouseViewComponent() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<WarehouseItemDto>>() {
            @Override
            public void onSucceeded(final List<WarehouseItemDto> value) {
                tableView.setItems(FXCollections.observableList(value));
            }
        });


        tableView = new TableView<>();
        {
            TableColumn<WarehouseItemDto, String> column = new TableColumn<>("Product Number");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, String>column("productNumber"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDto, String> column = new TableColumn<>("Short Number");
            column.setMinWidth(250);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, String>column("shortName"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDto, Double> column = new TableColumn<>("Price");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, Double>column("price"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDto, Double> column = new TableColumn<>("Count");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, Double>column("count"));
            tableView.getColumns().add(column);
        }

        ToolBar control = new ToolBar();
        Button refresh = new Button(null, ImageFactory.createReload32Icon());
        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                load();
            }
        });
        control.getItems().add(refresh);

        setCenter(tableView);
        setTop(control);
    }

    public void load() {
        loadingPane.bindTask(loadService);
        loadService.restart();
    }

    public void bindLoading(final LoadingPane loadingPane) {
        this.loadingPane = loadingPane;
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

