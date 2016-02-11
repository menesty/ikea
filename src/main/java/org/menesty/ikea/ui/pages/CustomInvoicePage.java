package org.menesty.ikea.ui.pages;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.task.InvoicePdfSyncService;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.dialog.InvoicePdfDialog;
import org.menesty.ikea.ui.controls.table.CustomInvoiceComponent;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class CustomInvoicePage extends BasePage {

    private InvoicePdfDialog invoicePdfDialog;

    private CustomInvoiceComponent customInvoiceComponent;

    private LoadService loadService;

    private InvoicePdfSyncService invoicePdfSyncService;

    private BaseTableView<InvoicePdf> invoicePdfTable;

    private Button deleteButton;

    private Button editButton;

    public CustomInvoicePage() {
    }

    @Override
    protected void initialize() {
        invoicePdfDialog = new InvoicePdfDialog(getStage());

        loadService = new LoadService();
        loadService.setOnSucceededListener(value -> {
            invoicePdfTable.getItems().clear();
            invoicePdfTable.getItems().addAll(value);
        });


        invoicePdfSyncService = new InvoicePdfSyncService();
        invoicePdfSyncService.setOnSucceededListener(value -> {
            if (value) {
                InvoicePdf invoicePdf = invoicePdfSyncService.getInvoice();
                invoicePdf.setSync(true);
                ServiceFacade.getInvoicePdfService().save(invoicePdf);
                invoicePdfTable.update(invoicePdf);
            }
        });
    }

    @Override
    public Node createView() {
        BorderPane container = new BorderPane();
        container.setCenter(createInvoicePdfPane());

        customInvoiceComponent = new CustomInvoiceComponent(getDialogSupport()) {
            @Override
            protected void update(InvoicePdf invoicePdf) {
                invoicePdfTable.update(invoicePdf);
            }

            @Override
            protected void startWork() {
                loadingPane.show();
            }

            @Override
            protected void endWork() {
                loadingPane.hide();
            }

            @Override
            protected void beforeLoad(Worker<?> task) {
                loadingPane.bindTask(task);
            }
        };
        customInvoiceComponent.setPrefHeight(250);

        container.setBottom(customInvoiceComponent);

        return wrap(container);
    }

    private void showAddEditDialog(InvoicePdf invoicePdf) {
        invoicePdfDialog.bind(invoicePdf, new EntityDialogCallback<InvoicePdf>() {
            @Override
            public void onSave(InvoicePdf invoicePdf, Object... params) {
                ServiceFacade.getInvoicePdfService().save(invoicePdf);
                hidePopupDialog();
                loadingPane.bindTask(loadService);
                loadService.restart();
            }

            @Override
            public void onCancel() {
                hidePopupDialog();
            }
        });

        showPopupDialog(invoicePdfDialog);
    }

    private BorderPane createInvoicePdfPane() {
        BorderPane pane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        {
            Button button = new Button(null, ImageFactory.createAdd32Icon());
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    showAddEditDialog(new InvoicePdf());
                }
            });

            toolBar.getItems().add(button);
        }

        editButton = new Button(null, ImageFactory.createEdit32Icon());
        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                InvoicePdf invoicePdf = invoicePdfTable.getSelectionModel().getSelectedItem();

                if (invoicePdf != null)
                    showAddEditDialog(invoicePdf);
            }
        });
        editButton.setDisable(true);

        toolBar.getItems().add(editButton);

        deleteButton = new Button(null, ImageFactory.createDelete32Icon());
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Dialog.confirm(getDialogSupport(), "Warning", "Are you sure want delete selected Invoice", new DialogCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onYes() {
                        InvoicePdf invoicePdf = invoicePdfTable.getSelectionModel().getSelectedItem();

                        if (invoicePdf != null && !invoicePdf.isSync()) {
                            ServiceFacade.getInvoicePdfService().remove(invoicePdf);
                            customInvoiceComponent.setInvoicePdf(null);
                            loadingPane.bindTask(loadService);
                            loadService.restart();
                        }
                    }
                });
            }
        });

        toolBar.getItems().add(deleteButton);
        deleteButton.setDisable(true);

        pane.setTop(toolBar);


        invoicePdfTable = new BaseTableView<InvoicePdf>() {
            @Override
            protected void onRowRender(TableRow<InvoicePdf> row, final InvoicePdf newValue) {
                row.getStyleClass().remove("greenRow");
                row.setContextMenu(null);

                ContextMenu contextMenu = new ContextMenu();

                if (newValue != null && newValue.isSync())
                    row.getStyleClass().add("greenRow");
                else {
                    {
                        MenuItem menuItem = new MenuItem("Upload", ImageFactory.createUpload16Icon());
                        menuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                invoicePdfSyncService.setInvoice(newValue);

                                loadingPane.bindTask(invoicePdfSyncService);

                                invoicePdfSyncService.restart();
                            }
                        });

                        contextMenu.getItems().add(menuItem);
                    }

                }

                {
                    MenuItem menuItem = new MenuItem("Epp", ImageFactory.createDownload16Icon());
                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            InvoicePdf invoicePdf = invoicePdfTable.getSelectionModel().getSelectedItem();

                            FileChooser fileChooser = FileChooserUtil.getEpp();
                            fileChooser.setInitialFileName(invoicePdf.getInvoiceNumber().replaceAll("[/,-\\\\]", "_") + ".epp");

                            File selectedFile = fileChooser.showSaveDialog(getStage());

                            if (selectedFile != null) {
                                List<InvoiceItem> list = customInvoiceComponent.getItems();
                                int index = 0;

                                for (InvoiceItem invoiceItem : list)
                                    invoiceItem.setIndex(++index);

                                ServiceFacade.getInvoiceService().exportToEpp("CustomInvoice",BigDecimal.valueOf(20), invoicePdf.getInvoiceNumber(), list, selectedFile.getAbsolutePath());
                            }
                        }
                    });

                    contextMenu.getItems().add(menuItem);
                }


                row.setContextMenu(contextMenu);

            }
        };

        {
            TableColumn<InvoicePdf, String> column = new TableColumn<>("Name");
            column.setMinWidth(160);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf, String>column("name"));
            invoicePdfTable.getColumns().add(column);

        }

        {
            TableColumn<InvoicePdf, String> column = new TableColumn<>("Number");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf, String>column("invoiceNumber"));
            invoicePdfTable.getColumns().add(column);
        }

        {
            TableColumn<InvoicePdf, Double> column = new TableColumn<>();
            column.setText("Price");
            column.setMinWidth(60);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf, Double>column("price"));
            invoicePdfTable.getColumns().add(column);
        }

        {
            TableColumn<InvoicePdf, String> column = new TableColumn<>();
            column.setText("Created Date");
            column.setMinWidth(100);
            column.setCellValueFactory(ColumnUtil.<InvoicePdf>dateColumn("createdDate"));
            invoicePdfTable.getColumns().add(column);
        }

        invoicePdfTable.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                InvoicePdf invoicePdf = invoicePdfTable.getSelectionModel().getSelectedItem();

                customInvoiceComponent.setInvoicePdf(invoicePdf);
                deleteButton.setDisable(invoicePdf == null || invoicePdf.isSync());
                editButton.setDisable(invoicePdf == null);
            }
        });

        pane.setCenter(invoicePdfTable);

        return pane;

    }

    @Override
    public void onActive(Object... params) {
        customInvoiceComponent.setInvoicePdf(null);
        loadingPane.bindTask(loadService);
        loadService.restart();
    }

    class LoadService extends AbstractAsyncService<List<InvoicePdf>> {
        @Override
        protected Task<List<InvoicePdf>> createTask() {
            deleteButton.setDisable(true);
            editButton.setDisable(true);
            return new Task<List<InvoicePdf>>() {
                @Override
                protected List<InvoicePdf> call() throws Exception {
                    return ServiceFacade.getInvoicePdfService().loadBy(null);
                }
            };
        }
    }
}