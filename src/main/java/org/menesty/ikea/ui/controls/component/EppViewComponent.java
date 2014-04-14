package org.menesty.ikea.ui.controls.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.menesty.ikea.domain.InvoicePdf;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;
import org.menesty.ikea.ui.controls.table.InvoiceEppInvisibleTableView;
import org.menesty.ikea.ui.controls.table.InvoiceEppTableView;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.NumberUtil;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Menesty on 12/22/13.
 */
public abstract class EppViewComponent extends StackPane {

    private InvoiceEppTableView invoiceEppTableView;

    private InvoiceEppInvisibleTableView invoiceEppInvisibleTableView;

    private LoadService loadService;

    private LoadingPane loadingPane;

    private InvoicePdf currentInvoicePdf;

    private ToolBar eppToolBar;

    private Button delBtn;

    private Button saveBtn;

    private List<TableRow<InvoiceItem>> rows;

    private StatusPanel eppStatusPanel;

    private BigDecimal invoicePrice;

    private String artPrefix = "";
    private Button exportEppBtn;

    private InvalidationListener saveBtnUpdater;

    private static final int MAX_ZESTAV_PRICE = 460;

    public EppViewComponent(final Stage stage) {
        loadingPane = new LoadingPane();
        loadService = new LoadService();

        SplitPane splitPane = new SplitPane();
        loadingPane.bindTask(loadService);

        loadService.setOnSucceededListener(new AbstractAsyncService.SucceededListener<List<InvoiceItem>>() {
            @Override
            public void onSucceeded(final List<InvoiceItem> value) {
                updateViews(value);
            }
        });

        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPosition(1, 0.40);

        splitPane.getItems().addAll(initInvoiceEppTableView(stage), invoiceEppInvisibleTableView = new InvoiceEppInvisibleTableView() {
            public void onRowDoubleClick(TableRow<InvoiceItem> row) {
                InvoiceItem item = row.getItem();
                item.setVisible(true);
                item.setPrice(item.basePrice);

                invoiceEppInvisibleTableView.getItems().remove(item);
                invoiceEppTableView.getItems().add(item);
            }
        });

        getChildren().addAll(splitPane, loadingPane);
    }

    private BorderPane initInvoiceEppTableView(final Stage stage) {
        invoiceEppTableView = new InvoiceEppTableView() {
            @Override
            public void onEdit(InvoiceItem item) {
                saveBtn.setDisable(false);
            }
        };
        rows = new ArrayList<>();

        final InvalidationListener invalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                eppStatusPanel.setCurrentTotal(calculatePrice(invoiceEppTableView.getItems()).doubleValue());
            }
        };

        eppToolBar = new ToolBar();
        saveBtn = new Button(null, ImageFactory.createSave32Icon());
        saveBtn.setDisable(true);
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ServiceFacade.getInvoiceItemService().save(invoiceEppTableView.getItems());
                ServiceFacade.getInvoiceItemService().save(invoiceEppInvisibleTableView.getItems());
                saveBtn.setDisable(true);
                onChange(currentInvoicePdf);

            }
        });

        exportEppBtn = new Button(null, ImageFactory.createEppExport32Icon());
        exportEppBtn.setContentDisplay(ContentDisplay.RIGHT);
        exportEppBtn.setTooltip(new Tooltip("Export to EPP"));
        exportEppBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                FileChooser fileChooser = FileChooserUtil.getEpp();

                File selectedFile = fileChooser.showSaveDialog(stage);

                if (selectedFile != null) {
                    List<InvoiceItem> list = invoiceEppTableView.getItems();
                    int index = 0;

                    for (InvoiceItem invoiceItem : list)
                        invoiceItem.setIndex(++index);

                    export(list, selectedFile.getAbsolutePath());

                    FileChooserUtil.setDefaultDir(selectedFile);
                }

            }
        });


        eppToolBar.getItems().addAll(saveBtn, exportEppBtn, new Separator());

        Button addBtn = new Button(null, ImageFactory.createPlus32Icon());
        addBtn.setTooltip(new Tooltip("Add Row"));
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                InvoiceItem item = new InvoiceItem();
                item.setArtNumber("New");
                item.setName("New");
                item.setShortName("New");
                item.setCount(1);
                item.setZestav(true);
                item.setVisible(true);
                item.invoicePdf = currentInvoicePdf;
                invoiceEppTableView.getItems().add(item);
            }
        });

        eppToolBar.getItems().add(addBtn);

        delBtn = new Button(null, ImageFactory.createMinus32Icon());
        delBtn.setTooltip(new Tooltip("Delete Row"));
        delBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                InvoiceItem item = invoiceEppTableView.getSelectionModel().getSelectedItem();
                if (!item.isZestav()) {
                    item.setVisible(false);
                    invoiceEppInvisibleTableView.getItems().add(item);
                }
                invoiceEppTableView.getItems().remove(item);
            }
        });
        delBtn.setDisable(true);
        eppToolBar.getItems().add(delBtn);

        Button balanceBtn = new Button(null, ImageFactory.createBalance32Icon());
        balanceBtn.setTooltip(new Tooltip("Auto balance Price"));
        balanceBtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                List<InvoiceItem> items = new ArrayList<>(invoiceEppTableView.getItems());
                Iterator<InvoiceItem> iterator = items.iterator();

                BigDecimal zestavPrice = BigDecimal.ZERO;

                while (iterator.hasNext()) {
                    InvoiceItem item = iterator.next();

                    if (item.isZestav()) {
                        iterator.remove();
                        zestavPrice = zestavPrice.add(item.getTotalWatPrice());
                    } else
                        item.setPrice(item.basePrice);

                }

                BigDecimal currentItemsPrice = calculatePrice(items);
                BigDecimal diff = invoicePrice.subtract(zestavPrice);

                BigDecimal cof = diff.doubleValue() == 0 ? BigDecimal.ONE : BigDecimal.valueOf(diff.doubleValue() / currentItemsPrice.doubleValue());

                for (InvoiceItem item : items)
                    item.setPrice(BigDecimal.valueOf(item.getPriceWat()).multiply(cof).setScale(2, BigDecimal.ROUND_CEILING).doubleValue());

                BigDecimal currentPrice = calculatePrice(items).add(zestavPrice);
                diff = invoicePrice.subtract(currentPrice);
                if (diff.doubleValue() != 0) {
                    //search with 1 element
                    InvoiceItem updateItem = null;
                    for (InvoiceItem item : items)
                        if (item.getCount() == 1) {
                            updateItem = item;
                            break;
                        }

                    if (updateItem != null)
                        updateItem.setPrice(BigDecimal.valueOf(updateItem.getPriceWat()).add(diff).doubleValue());

                }

                for (TableRow<InvoiceItem> row : rows)
                    row.setItem(null);

                invalidationListener.invalidated(null);

            }
        });

        eppToolBar.getItems().add(balanceBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        eppToolBar.getItems().add(spacer);

        Button reloadBtn = new Button(null, ImageFactory.createReload32Icon());
        reloadBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Dialog.confirm("All balanced items will be deleted", new DialogCallback() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onYes() {
                        ServiceFacade.getInvoiceItemService().deleteBy(currentInvoicePdf);
                        updateViews(prepareData(currentInvoicePdf.getProducts()));
                        saveBtn.setDisable(false);
                        onChange(currentInvoicePdf);
                    }
                });

            }
        });

        eppToolBar.getItems().add(reloadBtn);

        invoiceEppTableView.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                delBtn.setDisable(invoiceEppTableView.getSelectionModel().getSelectedItem() == null);
            }
        });

        invoiceEppTableView.setRowFactory(new Callback<TableView<InvoiceItem>, TableRow<InvoiceItem>>() {
            @Override
            public TableRow<InvoiceItem> call(TableView<InvoiceItem> invoiceItemTableView) {
                TableRow<InvoiceItem> row = new TableRow<>();
                rows.add(row);
                return row;
            }
        });

        invoiceEppTableView.itemsProperty().addListener(invalidationListener);
        invoiceEppTableView.editingCellProperty().addListener(invalidationListener);

        BorderPane pane = new BorderPane();
        pane.setTop(eppToolBar);
        pane.setCenter(invoiceEppTableView);
        pane.setBottom(eppStatusPanel = new StatusPanel());

        saveBtnUpdater = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                saveBtn.setDisable(false);
                eppStatusPanel.setCurrentTotal(calculatePrice(invoiceEppTableView.getItems()).doubleValue());
            }
        };

        return pane;
    }

    public abstract void onChange(InvoicePdf invoicePdf);

    public abstract void export(List<InvoiceItem> items, String path);

    private BigDecimal calculatePrice(List<InvoiceItem> items) {
        BigDecimal price = BigDecimal.ZERO;

        for (InvoiceItem item : items)
            price = price.add(item.getTotalWatPrice());

        return price;

    }

    private void updateViews(List<InvoiceItem> items) {
        List<InvoiceItem> visible = new ArrayList<>();
        List<InvoiceItem> invisible = new ArrayList<>();
        rows = new ArrayList<>();

        for (InvoiceItem item : items)
            if (item.isVisible())
                visible.add(item);
            else
                invisible.add(item);

        invoiceEppTableView.setItems(FXCollections.observableList(visible));
        invoiceEppInvisibleTableView.setItems(FXCollections.observableList(invisible));

        invoiceEppTableView.getItems().addListener(saveBtnUpdater);
        exportEppBtn.setDisable(items.isEmpty());
    }

    public void setActive(final InvoicePdf invoicePdf) {
        currentInvoicePdf = invoicePdf;

        if (invoicePdf != null) {
            artPrefix = ((int) NumberUtil.parse(invoicePdf.customerOrder.getName())) + "";
            loadService.setInvoicePdf(invoicePdf);
            loadService.restart();
            invoicePrice = InvoicePdf.getTotalPrice(currentInvoicePdf.getProducts());
            eppStatusPanel.setTotal(invoicePrice.doubleValue());
        } else
            updateViews(new ArrayList<InvoiceItem>());

        eppToolBar.setDisable(invoicePdf == null);
    }

    private List<InvoiceItem> prepareData(List<RawInvoiceProductItem> items) {
        List<InvoiceItem> result = new ArrayList<>();
        List<RawInvoiceProductItem> filtered = new ArrayList<>();

        for (RawInvoiceProductItem item : items)
            if (item.isSeparate())
                result.addAll(InvoiceItem.get(item.getProductInfo(), artPrefix, item.getCount()));
            else {
                filtered.add(item);
                InvoiceItem invoiceItem = InvoiceItem.get(item.getProductInfo(), artPrefix, item.getCount(), 1, 1);
                invoiceItem.setVisible(false);
                result.add(invoiceItem);
            }

        BigDecimal totalPrice = BigDecimal.ZERO;

        Map<ProductInfo.Group, Integer> groupMap = new HashMap<>();

        for (RawInvoiceProductItem item : filtered) {
            totalPrice = totalPrice.add(BigDecimal.valueOf(item.getTotal()));

            Integer groupCount = groupMap.get(item.getProductInfo().getGroup());

            if (groupCount == null)
                groupCount = 1;
            else
                groupCount++;

            groupMap.put(item.getProductInfo().getGroup(), groupCount);

        }

        int zestavCount = (int) (totalPrice.doubleValue() / MAX_ZESTAV_PRICE);

        for (int i = 0; i < zestavCount; i++)
            result.add(createZestav(groupMap, i, MAX_ZESTAV_PRICE));

        BigDecimal diff = totalPrice.subtract(BigDecimal.valueOf(zestavCount * MAX_ZESTAV_PRICE));

        if (diff.doubleValue() > 0)
            result.add(createZestav(groupMap, zestavCount, diff.doubleValue()));

        for (InvoiceItem item : result)
            item.invoicePdf = currentInvoicePdf;

        return result;
    }

    private InvoiceItem createZestav(Map<ProductInfo.Group, Integer> groupMap, int index, double price) {
        String subName = "";
        int maxIndex = 0;

        for (Map.Entry<ProductInfo.Group, Integer> entry : groupMap.entrySet())
            if (entry.getValue() > maxIndex) {
                maxIndex = entry.getValue();
                subName = entry.getKey().getTitle().equals("") ? entry.getKey().name() : entry.getKey().getTitle();
            }

        String name = String.format("Zestaw IKEA %1$s", subName);
        String artNumber = artPrefix + "_" + subName.substring(0, 2) + "_" + (index + 1);

        return InvoiceItem.get(artNumber, null, name, name, price, 23, "", 1, 1, 1, 1).setZestav(true);
    }

    class LoadService extends AbstractAsyncService<List<InvoiceItem>> {
        private SimpleObjectProperty<InvoicePdf> invoicePdf = new SimpleObjectProperty<>();

        public void setInvoicePdf(InvoicePdf invoicePdf) {
            this.invoicePdf.setValue(invoicePdf);
        }

        @Override
        protected Task<List<InvoiceItem>> createTask() {
            final InvoicePdf _invoicePdf = invoicePdf.get();

            return new Task<List<InvoiceItem>>() {
                @Override
                protected List<InvoiceItem> call() throws Exception {
                    return ServiceFacade.getInvoiceItemService().loadBy(_invoicePdf);
                }
            };
        }
    }
}

class StatusPanel extends TotalStatusPanel {
    private Label currentTotal;

    public StatusPanel() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getItems().add(spacer);
        getItems().add(new Label("Current  :"));
        getItems().add(currentTotal = new Label());
    }

    public void setCurrentTotal(double total) {
        currentTotal.setText(NumberFormat.getNumberInstance().format(total));
    }
}