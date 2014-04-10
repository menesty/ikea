package org.menesty.ikea.ui.controls.dialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.menesty.ikea.domain.ParagonDto;
import org.menesty.ikea.domain.ParagonItemDto;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpUtil;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.List;

public class ParagonViewDialog extends BaseDialog {

    private TableView<ParagonItemDto> tableView;

    private LoadService loadService;

    public ParagonViewDialog() {
        setMinWidth(530);
        loadService = new LoadService();
        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<ParagonItemDto>>() {
            @Override
            public void onSucceeded(List<ParagonItemDto> value) {
                tableView.getItems().clear();

                if (value != null)
                    tableView.getItems().addAll(value);
            }
        });

        loadingPane.bindTask(loadService);

        setTitle("Paragon details");

        tableView = new TableView<>();

        {
            TableColumn<ParagonItemDto, String> column = new TableColumn<>("Name");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<ParagonItemDto, String>column("productNumber"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonItemDto, String> column = new TableColumn<>("Short Name");
            column.setMinWidth(210);
            column.setCellValueFactory(ColumnUtil.<ParagonItemDto, String>column("shortName"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonItemDto, String> column = new TableColumn<>("Count");
            column.setMaxWidth(50);
            column.setCellValueFactory(ColumnUtil.<ParagonItemDto>number("count"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ParagonItemDto, Double> column = new TableColumn<>("Price");
            column.setMaxWidth(55);
            column.setCellValueFactory(ColumnUtil.<ParagonItemDto, Double>column("price"));
            tableView.getColumns().add(column);
        }

        addRow(tableView);

        cancelBtn.setVisible(false);
    }

    public void show(ParagonDto paragonDto) {
        tableView.getItems().clear();
        loadService.setParagon(paragonDto);
        loadService.restart();

    }

    class LoadService extends AbstractAsyncService<List<ParagonItemDto>> {
        private SimpleObjectProperty<ParagonDto> paragon = new SimpleObjectProperty<>();

        @Override
        protected Task<List<ParagonItemDto>> createTask() {
            final int _paragonId = paragon.get().getId();

            return new Task<List<ParagonItemDto>>() {
                @Override
                protected List<ParagonItemDto> call() throws Exception {
                    URL url = new URL(ServiceFacade.getApplicationPreference().getWarehouseHost() + "/paragon/details/" + _paragonId);
                    HttpHost targetHost = new HttpHost(url.getHost());

                    CredentialsProvider credsProvider = HttpUtil.credentialsProvider(targetHost);

                    try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build()) {
                        HttpClientContext localContext = HttpUtil.context(targetHost);

                        HttpGet httpPost = new HttpGet(url.toURI());

                        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
                            String content = EntityUtils.toString(response.getEntity());

                            Gson gson = new Gson();
                            Type collectionType = new TypeToken<Collection<ParagonItemDto>>() {
                            }.getType();
                            return gson.fromJson(content, collectionType);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        public void setParagon(ParagonDto paragon) {
            this.paragon.set(paragon);
        }
    }

}
