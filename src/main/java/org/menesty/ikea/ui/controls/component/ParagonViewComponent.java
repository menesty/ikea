package org.menesty.ikea.ui.controls.component;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.ParagonDto;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpUtil;

import java.lang.reflect.Type;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParagonViewComponent extends BorderPane {

    private final LoadService loadService;
    private TableView<ParagonDto> tableView;

    private LoadingPane loadingPane;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public ParagonViewComponent() {

        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<ParagonDto>>() {
            @Override
            public void onSucceeded(final List<ParagonDto> value) {
                tableView.setItems(FXCollections.observableList(value));
            }
        });


        tableView = new TableView<>();

        {
            TableColumn<ParagonDto, Number> column = new TableColumn<>();
            column.setMaxWidth(45);
            column.setCellValueFactory(ColumnUtil.<ParagonDto>indexColumn());
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonDto, String> column = new TableColumn<>("Date");
            column.setMinWidth(150);
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ParagonDto, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ParagonDto, String> item) {
                    return new SimpleStringProperty(sdf.format(item.getValue().getCreatedDate()));
                }
            });
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonDto, Double> column = new TableColumn<>("Price");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<ParagonDto, Double>column("price"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonDto, Integer> column = new TableColumn<>("Order ID");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<ParagonDto, Integer>column("orderId"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonDto, Integer> column = new TableColumn<>("Driver ID");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<ParagonDto, Integer>column("driverId"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonDto, Integer> column = new TableColumn<>("Counter Party ID");
            column.setMinWidth(180);
            column.setCellValueFactory(ColumnUtil.<ParagonDto, Integer>column("counterPartyId"));
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


    class LoadService extends AbstractAsyncService<List<ParagonDto>> {

        @Override
        protected Task<List<ParagonDto>> createTask() {
            return new Task<List<ParagonDto>>() {
                @Override
                protected List<ParagonDto> call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/storage/paragons");
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    List<ParagonDto> result = new ArrayList<>();
                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet(url.toURI());

                        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
                            String data = EntityUtils.toString(response.getEntity());

                            GsonBuilder gson = new GsonBuilder();
                            gson.registerTypeAdapter(ParagonDto.class, new ParagonDtoAdapter());

                            Type collectionType = new TypeToken<Collection<ParagonDto>>() {
                            }.getType();

                            return gson.create().fromJson(data, collectionType);
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

class ParagonDtoAdapter implements JsonDeserializer<ParagonDto> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public ParagonDto deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsEl = element.getAsJsonObject();

        ParagonDto entity = new ParagonDto();

        entity.setId(jsEl.get("id").getAsInt());
        entity.setDriverId(jsEl.get("driver_id").getAsInt());
        entity.setCounterPartyId(jsEl.get("counterparty_id").getAsInt());

        try {
            entity.setCreatedDate(sdf.parse(jsEl.get("createdDate").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!jsEl.get("order_id").isJsonNull())
            entity.setOrderId(jsEl.get("order_id").getAsInt());

        if (!jsEl.get("price").isJsonNull())
            entity.setPrice(jsEl.get("price").getAsDouble());

        return entity;
    }
}