package org.menesty.ikea.ui.controls.component;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.domain.ParagonDto;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.dialog.ParagonViewDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.HttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParagonViewComponent extends BorderPane {
    private final LoadService loadService;

    private BaseTableView<ParagonDto> tableView;

    private LoadingPane loadingPane;

    private ParagonEppService paragonEppService;

    private ParagonEmailService paragonEmailService;

    private ParagonCancelService paragonCancelService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private ParagonViewDialog paragonViewDialog;

    public ParagonViewComponent(final Stage stage) {
        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<ParagonDto>>() {
            @Override
            public void onSucceeded(final List<ParagonDto> value) {
                tableView.setItems(FXCollections.observableList(value));
            }
        });

        paragonCancelService = new ParagonCancelService();
        paragonCancelService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<String>() {
            @Override
            public void onSucceeded(String value) {
                load();
            }
        });

        paragonEppService = new ParagonEppService();
        paragonEppService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<Result>() {
            @Override
            public void onSucceeded(final Result value) {
                loadingPane.hide();

                FileChooser fileChooser = FileChooserUtil.getEpp();
                fileChooser.setInitialFileName(value.name);

                File selectedFile = fileChooser.showSaveDialog(stage);

                if (selectedFile != null) {
                    String fileName = selectedFile.getAbsolutePath();

                    if (!fileName.endsWith(".epp"))
                        fileName += ".epp";

                    try {
                        new FileOutputStream(fileName).write(value.result.getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    FileChooserUtil.setDefaultDir(selectedFile);
                }
            }
        });

        paragonEmailService = new ParagonEmailService();

        paragonViewDialog = new ParagonViewDialog() {
            @Override
            public void onOk() {
                IkeaApplication.get().hidePopupDialog();
            }
        };

        tableView = new BaseTableView<ParagonDto>() {
            @Override
            protected void onRowDoubleClick(TableRow<ParagonDto> row) {
                if (row.getItem() != null) {
                    IkeaApplication.get().showPopupDialog(paragonViewDialog);
                    paragonViewDialog.show(row.getItem());
                }
            }

            @Override
            protected void onRowRender(TableRow<ParagonDto> row, final ParagonDto paragonDto) {
                row.getStyleClass().remove("greenRow");
                row.setContextMenu(null);

                if (paragonDto == null)
                    return;

                ContextMenu contextMenu = new ContextMenu();

                {
                    MenuItem menuItem = new MenuItem("Download Epp", ImageFactory.createDownload16Icon());
                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            loadingPane.bindTask(paragonEppService);
                            paragonEppService.setParagon(paragonDto);
                            paragonEppService.restart();
                        }
                    });
                    contextMenu.getItems().add(menuItem);
                }

                {
                    MenuItem menuItem = new MenuItem("Send to Email", ImageFactory.createEmailSend16Icon());
                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            loadingPane.bindTask(paragonEppService);
                            paragonEmailService.setParagonId(paragonDto.getId());
                            paragonEmailService.restart();
                        }
                    });
                    contextMenu.getItems().add(menuItem);
                }

                {
                    MenuItem menuItem = new MenuItem("Cancel paragon");
                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            loadingPane.bindTask(paragonCancelService);
                            paragonCancelService.setParagonId(paragonDto.getId());
                            paragonCancelService.restart();
                        }
                    });

                    contextMenu.getItems().add(menuItem);
                }

                row.setContextMenu(contextMenu);

                if (paragonDto.isDownloaded())
                    row.getStyleClass().add("greenRow");
            }
        };

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

        {
            TableColumn<ParagonDto, Integer> column = new TableColumn<>("Paragon ID");
            column.setMinWidth(180);
            column.setCellValueFactory(ColumnUtil.<ParagonDto, Integer>column("id"));
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

    class ParagonEppService extends AbstractAsyncService<Result> {
        private SimpleObjectProperty<ParagonDto> paragonDto = new SimpleObjectProperty<>();

        @Override
        protected Task<Result> createTask() {
            final ParagonDto _paragonDto = paragonDto.get();
            return new Task<Result>() {
                @Override
                protected Result call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/paragon/details/" + _paragonDto.getId() + "/epp");
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet(url.toURI());

                        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
                            _paragonDto.setDownloaded(true);
                            tableView.update(_paragonDto);

                            return new Result("paragon_" + _paragonDto.getId() + "_" + _paragonDto.getPrice() + ".epp",
                                    EntityUtils.toString(response.getEntity(), "ISO-8859-2"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        public void setParagon(ParagonDto paragonDto) {
            this.paragonDto.setValue(paragonDto);
        }
    }

    class ParagonEmailService extends AbstractAsyncService<String> {
        private SimpleIntegerProperty paragonId = new SimpleIntegerProperty();

        @Override
        protected Task<String> createTask() {
            final int _paragonId = paragonId.get();
            return new Task<String>() {
                @Override
                protected String call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/paragon/details/" + _paragonId + "/email");
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet(url.toURI());

                        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
                            return EntityUtils.toString(response.getEntity());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }
            };
        }

        public void setParagonId(int paragonId) {
            this.paragonId.setValue(paragonId);
        }
    }

    class ParagonCancelService extends AbstractAsyncService<String> {
        private SimpleIntegerProperty paragonId = new SimpleIntegerProperty();

        @Override
        protected Task<String> createTask() {
            final int _paragonId = paragonId.get();
            return new Task<String>() {
                @Override
                protected String call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/paragon/cancel/" + _paragonId);
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet(url.toURI());

                        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
                            return EntityUtils.toString(response.getEntity());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }
            };
        }

        public void setParagonId(int paragonId) {
            this.paragonId.setValue(paragonId);
        }
    }


    class LoadService extends AbstractAsyncService<List<ParagonDto>> {

        @Override
        protected Task<List<ParagonDto>> createTask() {
            return new Task<List<ParagonDto>>() {
                @Override
                protected List<ParagonDto> call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/paragon");
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

class Result {

    public Result(String name, String result) {
        this.name = name;
        this.result = result;
    }

    public String name;

    public String result;
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

        entity.setDownloaded(jsEl.get("downloaded").getAsInt() == 1);

        return entity;
    }
}
