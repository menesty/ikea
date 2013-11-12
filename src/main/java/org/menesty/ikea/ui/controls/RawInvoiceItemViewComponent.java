package org.menesty.ikea.ui.controls;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.menesty.ikea.processor.invoice.InvoiceItem;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;
import org.menesty.ikea.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class RawInvoiceItemViewComponent extends BorderPane {

    private final RawInvoiceTableView rawInvoiceItemTableView;

    public RawInvoiceItemViewComponent(final Stage stage) {
        ToolBar rawInvoiceControl = new ToolBar();

        ImageView imageView = new ImageView(new javafx.scene.image.Image("/styles/images/icon/epp-32x32.png"));
        Button exportEppButton = new Button("", imageView);
        exportEppButton.setContentDisplay(ContentDisplay.RIGHT);
        exportEppButton.setTooltip(new Tooltip("Export to EPP"));
        exportEppButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                exportToEpp(prepareData(rawInvoiceItemTableView.getItems()));
            }
        });
        rawInvoiceControl.getItems().add(exportEppButton);

        rawInvoiceItemTableView = new RawInvoiceTableView() {
            @Override
            public void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row) {
                RawInvoiceItemViewComponent.this.onRowDoubleClick(row);

            }
        };

        setTop(rawInvoiceControl);
        setCenter(rawInvoiceItemTableView);
    }

    public void setItems(List<RawInvoiceProductItem> items) {
        rawInvoiceItemTableView.setItems(FXCollections.observableArrayList(items));
    }

    public abstract void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row);

    public abstract void exportToEpp(List<InvoiceItem> items);

    private List<InvoiceItem> prepareData(List<RawInvoiceProductItem> items) {
        List<InvoiceItem> result = new ArrayList<>();
        List<RawInvoiceProductItem> filtered = new ArrayList<>();

        for (RawInvoiceProductItem item : items)
            if (item.getProductInfo().getPackageInfo().getWeight() > 3000 || item.getPrice() * item.getCount() > 200)
                result.addAll(InvoiceItem.get(item.getProductInfo(), item.getCount()));
            else
                filtered.add(item);

        //create zestavs
        double price = 0;
        int index = 0;
        double count = 0;
        for (RawInvoiceProductItem item : filtered) {
            double artPrice = NumberUtil.round(item.getPrice() * item.getCount());
            if ((price + artPrice) > 460) {
                //create zestav
                result.add(InvoiceItem.get("zestav " + index, "zestav " + index, "zestav " + index, price, 23, NumberUtil.round(count), 1, 1));
                index++;
                price = artPrice;
                count = item.getCount();
            } else {
                price = NumberUtil.round(price + artPrice);
                count = NumberUtil.round(count + item.getCount());
            }

        }
        index = 0;
        for (InvoiceItem invoiceItem : result)
            invoiceItem.setIndex(++index);
        return result;

    }

}
