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
                List<RawInvoiceProductItem> items = rawInvoiceItemTableView.getItems();
                exportToEpp(prepareData(items), getTotalPrice(items));
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

    private double getTotalPrice(List<RawInvoiceProductItem> items) {
        double price = 0;

        for (RawInvoiceProductItem item : items)
            price += NumberUtil.round(item.getPrice() * item.getCount());

        return NumberUtil.round(price);

    }

    public void setItems(List<RawInvoiceProductItem> items) {
        rawInvoiceItemTableView.setItems(FXCollections.observableArrayList(items));
    }

    public abstract void onRowDoubleClick(final TableRow<RawInvoiceProductItem> row);

    public abstract void exportToEpp(List<InvoiceItem> items, double price);

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
        int index = 1;
        double count = 0;
        for (RawInvoiceProductItem item : filtered) {
            double artPrice = NumberUtil.round(item.getPrice() * item.getCount());
            if ((price + artPrice) > 460) {
                //create zestav
                result.add(createZestav(index, price, count));
                //result.add(InvoiceItem.get("zestav " + index, String.format("zestav %1$s parts %2$s", index, count), String.format("zestav %1$s parts %2$s", index, count), price, 23, 1, 1, 1));
                index++;
                price = artPrice;
                count = item.getCount();
            } else {
                price = NumberUtil.round(price + artPrice);
                count = NumberUtil.round(count + item.getCount());
            }

        }
        if (price != 0)
            result.add(createZestav(index, price, count));

        return result;

    }

    private InvoiceItem createZestav(int index, double price, double count) {
        String name = String.format("zestav %1$s parts %2$s", index, (int) count);
        return InvoiceItem.get("zestav " + index, name, name, price, 23, 1, 1, 1);
    }

}
