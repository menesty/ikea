package org.menesty.ikea.ui.controls.component;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.MToolBar;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;

import java.util.ArrayList;
import java.util.List;

public class RawInvoiceItemSearchComponent extends BorderPane {
    private RawInvoiceTableView rawInvoiceTableView;

    private TextField productIdField;

    private List<RawInvoiceProductItem> items;

    public RawInvoiceItemSearchComponent() {
        ToolBar toolBar = new MToolBar();

        toolBar.getItems().add(productIdField = new TextField());

        productIdField.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                filter();
            }
        });

        setTop(toolBar);

        rawInvoiceTableView = new RawInvoiceTableView(true);

        setCenter(rawInvoiceTableView);
    }

    public void setItems(List<RawInvoiceProductItem> items) {
        this.items = items;

        rawInvoiceTableView.setItems(FXCollections.observableList(items));
    }

    private void filter() {
        String searchText = productIdField.getText();

        if (StringUtils.isNotBlank(searchText)) {
            String artNumber = ProductInfo.cleanProductId(searchText);
            List<RawInvoiceProductItem> result = new ArrayList<>();

            for (RawInvoiceProductItem item : items)
                if (item.getOriginalArtNumber().contains(artNumber))
                    result.add(item);

            rawInvoiceTableView.setItems(FXCollections.observableList(result));
        } else
            rawInvoiceTableView.setItems(FXCollections.observableList(items));
    }

}
