package org.menesty.ikea.ui.pages;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class WarehousePage extends BasePage {

    public WarehousePage() {
        super("Warehouse");
    }

    @Override
    public Node createView() {
        final TableView<WarehouseItemDto> tableView = new TableView<>();
        {
            TableColumn<WarehouseItemDto, String> column = new TableColumn<>("Product Number");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, String>column("productNumber"));
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


        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);

        StackPane pane = createRoot();
        pane.getChildren().add(0, borderPane);

        return pane;
    }

    class LoadService extends AbstractAsyncService<List<WarehouseItemDto>> {

        @Override
        protected Task<List<WarehouseItemDto>> createTask() {
            return new Task<List<WarehouseItemDto>>() {
                @Override
                protected List<WarehouseItemDto> call() throws Exception {
                    HttpHost targetHost = new HttpHost(ServiceFacade.getApplicationPreference().getWarehouseHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet("/storage/load");

                        try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
                            EntityUtils.toString(response.getEntity());

                            Gson gson = new Gson();
                            Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
                            Collection<Integer> ints2 = gson.fromJson("", collectionType);
                        }
                    }

                    return null;
                }
            };
        }
    }
}
