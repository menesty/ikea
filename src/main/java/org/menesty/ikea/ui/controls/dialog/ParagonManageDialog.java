package org.menesty.ikea.ui.controls.dialog;

import com.google.gson.Gson;
import javafx.beans.property.SimpleStringProperty;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on 4/29/14.
 */
public abstract class ParagonManageDialog extends BaseDialog {
    private TableView<WarehouseItemDto> tableView;

    private UploadTaskService uploadTaskService;

    public ParagonManageDialog() {
        setMinWidth(530);

        BorderPane content = new BorderPane();

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

        content.setCenter(tableView);

        ToolBar toolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createMinus32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    WarehouseItemDto item = tableView.getSelectionModel().getSelectedItem();

                    if (item != null)
                        tableView.getItems().remove(item);
                }
            });

            button.disabledProperty().isEqualTo(tableView.getSelectionModel().selectedItemProperty().isNull());

            toolBar.getItems().add(button);

        }

        content.setTop(toolBar);

        setAllowAutoHide(false);
        okBtn.setText("Create");
        addRow(content, bottomBar);

        uploadTaskService = new UploadTaskService();
        loadingPane.bindTask(uploadTaskService);


    }

    public abstract void onCreate();

    @Override
    public void onOk() {
        uploadTaskService.setData(serialize(tableView.getItems()));
        uploadTaskService.restart();
    }

    public void show(List<WarehouseItemDto> items) {
        tableView.getItems().clear();
        tableView.getItems().addAll(items);
    }

    class UploadTaskService extends AbstractAsyncService<Void> {
        private SimpleStringProperty data = new SimpleStringProperty();

        @Override
        protected Task<Void> createTask() {
            final String _data = data.get();
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/paragon/executeExport/");
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credProvider = HttpUtil.credentialsProvider(targetHost);

                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpPost httpPost = new HttpPost(url.toURI());
                        httpPost.setEntity(new StringEntity(_data, ContentType.APPLICATION_JSON));

                        try (CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, localContext)) {
                            String responseData = EntityUtils.toString(response.getEntity());
                            System.out.println(responseData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    HttpUtil.context(targetHost);
                    return null;
                }
            };
        }

        public void setData(String data) {
            this.data.setValue(data);
        }
    }

    private String serialize(List<WarehouseItemDto> items) {
        if (items == null || items.size() == 0)
            throw new RuntimeException("List can't be empty");

        Request request = new Request();
        request.driverId = 10;
        request.addParagon(items.get(0).orderId, 10, items);

        Gson gson = new Gson();
        return gson.toJson(request);
    }
}

class Request {
    public int driverId;

    private List<ParagonModel> paragons = new ArrayList<>();

    public void addParagon(int orderId, int userId, List<WarehouseItemDto> items) {
        if (items.size() == 0)
            return;

        ParagonModel paragon = new ParagonModel();
        paragon.orderId = orderId;
        paragon.userId = userId;

        ItemModel[] iModels = new ItemModel[items.size()];

        for (int i = 0; i < items.size(); i++) {
            WarehouseItemDto item = items.get(i);
            iModels[i] = new ItemModel(item.productNumber, item.price, item.count, item.weight);
        }

        paragon.items = iModels;

        paragons.add(paragon);
    }
}

class ParagonModel {
    public int orderId;

    public int userId;

    public ItemModel[] items;
}

class ItemModel {
    ItemModel(String productNumber, double price, double count, double weight) {
        this.productNumber = productNumber;
        this.price = price;
        this.count = count;
        this.weight = weight;
    }

    public String productNumber;

    public double price;

    public double count;

    public double weight;
}
