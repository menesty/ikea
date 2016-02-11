package org.menesty.ikea.ui.controls.component;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.domain.ProductInfo;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.processor.invoice.RawInvoiceProductItem;
import org.menesty.ikea.ui.controls.MToolBar;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.table.RawInvoiceTableView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RawInvoiceItemSearchComponent extends BorderPane {
  private RawInvoiceTableView rawInvoiceTableView;

  private TextField productIdField;

  private List<RawInvoiceProductItem> items;

  private TotalStatusPanel totalStatusPanel;

  public RawInvoiceItemSearchComponent(DialogSupport dialogSupport) {
    ToolBar toolBar = new MToolBar();

    toolBar.getItems().add(productIdField = new TextField());

    productIdField.textProperty().addListener(observable -> {
      filter();
    });

    {
      Button button = new Button(null, ImageFactory.createPaste32Icon());
      button.setOnAction(event -> {
        ArtNumberDialog dialog = new ArtNumberDialog(dialogSupport.getStage()) {
          @Override
          public void onOk() {
            dialogSupport.hidePopupDialog();
            multiArtNumberFilter(this.getArtNumbers());
          }
        };
        dialogSupport.showPopupDialog(dialog);
      });

      toolBar.getItems().add(button);
    }

    {
      Button button = new Button(null, ImageFactory.createClear32Icon());
      button.setOnAction(event -> updateTable(items));

      toolBar.getItems().add(button);
    }

    setTop(toolBar);

    rawInvoiceTableView = new RawInvoiceTableView(true);

    setCenter(rawInvoiceTableView);

    totalStatusPanel = new TotalStatusPanel();

    setBottom(totalStatusPanel);
  }

  public void setItems(List<RawInvoiceProductItem> items) {
    this.items = items;

    updateTable(items);
  }

  private void multiArtNumberFilter(String artNumber) {
    String[] artNumbers = artNumber.split("\n");
    List<String> list = new ArrayList<>();

    List<RawInvoiceProductItem> result = new ArrayList<>();

    for (String str : artNumbers) {
      list.add(str.trim());
    }

    result.addAll(items.stream().filter(item -> list.contains(item.getOriginalArtNumber())).collect(Collectors.toList()));

    updateTable(result);

  }

  private void updateTable(List<RawInvoiceProductItem> items) {
    BigDecimal total = items.stream()
        .map(rawInvoiceProductItem -> new BigDecimal(rawInvoiceProductItem.getTotal())
            .setScale(2, BigDecimal.ROUND_HALF_UP))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    totalStatusPanel.setTotal(total);

    rawInvoiceTableView.getItems().setAll(items);
  }

  private void filter() {
    String searchText = productIdField.getText();

    if (StringUtils.isNotBlank(searchText)) {
      String artNumber = ProductInfo.cleanProductId(searchText);
      List<RawInvoiceProductItem> result = items.stream().filter(item -> item.getOriginalArtNumber().contains(artNumber)).collect(Collectors.toList());

      updateTable(result);
    } else
      updateTable(items);
  }


}

class ArtNumberDialog extends BaseDialog {
  private TextArea textArea;

  public ArtNumberDialog(Stage stage) {
    super(stage);

    addRow(textArea = new TextArea(), bottomBar);
  }

  protected String getArtNumbers() {
    return textArea.getText();
  }
}
