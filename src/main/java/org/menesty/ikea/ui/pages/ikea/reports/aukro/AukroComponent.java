package org.menesty.ikea.ui.pages.ikea.reports.aukro;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.beans.property.SimpleBigDecimalProperty;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.domain.product.ProductShortInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 7/16/16.
 * 16:38.
 */
public class AukroComponent extends BorderPane {
  private TableView<ProductShortInfo> tableView;
  private LoadService loadService;
  private List<ProductShortInfo> items;

  public AukroComponent() {
    loadService = new LoadService();
    loadService.setOnSucceededListener(value -> {
      items = value;
      tableView.getItems().setAll(value);
    });

    tableView = new TableView<>();

    {
      TableColumn<ProductShortInfo, Number> column = new TableColumn<>();
      column.setMaxWidth(50);
      column.setCellValueFactory(ColumnUtil.<ProductShortInfo>indexColumn());

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

      column.setMinWidth(120);
      column.setCellFactory(ArtNumberCell::new);
      column.setCellValueFactory(ColumnUtil.column("artNumber"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE));

      column.setCellValueFactory(ColumnUtil.column("price"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, Number> column = new TableColumn<>(I18n.UA.getString(I18nKeys.WEIGHT));

      column.setCellValueFactory(param -> {
        if (param.getValue() != null && param.getValue().getWeight() != null && param.getValue().getWeight().compareTo(BigDecimal.ZERO) != 0) {
          return new SimpleBigDecimalProperty(param.getValue().getWeight().divide(BigDecimal.valueOf(1000), 3, BigDecimal.ROUND_HALF_UP));
        }

        return null;
      });

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PRICE_WEIGHT_COF));

      column.setCellValueFactory(ColumnUtil.column("priceWeightCof"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.BUY_COUNT));

      column.setCellValueFactory(ColumnUtil.column("productStatistic"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, Integer> column = new TableColumn<>(I18n.UA.getString(I18nKeys.PURCHASED_COUNT));

      column.setCellValueFactory(ColumnUtil.column("purchasedCount"));
      column.setMinWidth(100);

      tableView.getColumns().add(column);
    }


    {
      TableColumn<ProductShortInfo, Product.Group> column = new TableColumn<>(I18n.UA.getString(I18nKeys.GROUP_NAME));

      column.setMinWidth(110);
      column.setCellValueFactory(ColumnUtil.column("productGroup"));

      tableView.getColumns().add(column);
    }

    {
      TableColumn<ProductShortInfo, String> column = new TableColumn<>();

      column.setMinWidth(90);
      column.setCellValueFactory(ColumnUtil.dateColumn("priceUpdate"));

      tableView.getColumns().add(column);
    }

    setTop(new FilterToolBar() {
      @Override
      protected void filter(BigDecimal weight, BigDecimal priceWeightCof) {
        onFilter(weight, priceWeightCof);
      }
    });

    setCenter(tableView);
  }


  private void onFilter(BigDecimal weight, BigDecimal priceWeightCof) {
    List<ProductShortInfo> items = this.items;

    if ((weight != null && weight.compareTo(BigDecimal.ZERO) != 0) || (priceWeightCof != null && priceWeightCof.compareTo(BigDecimal.ZERO) != 0)) {

      BigDecimal convertedWeight = weight != null && BigDecimal.ZERO.compareTo(weight) != 0 ? weight.multiply(BigDecimal.valueOf(1000)) : null;

      items = this.items.stream().filter(productShortInfo -> {
        boolean result = true;

        if (convertedWeight != null) {
          result = productShortInfo.getWeight() != null && convertedWeight.compareTo(productShortInfo.getWeight()) >= 0;
        }

        if (result && priceWeightCof != null) {
          result = productShortInfo.getPriceWeightCof() != null && priceWeightCof.compareTo(productShortInfo.getPriceWeightCof()) <= 0;
        }

        return result;
      }).collect(Collectors.toList());
    }

    tableView.getItems().setAll(items);

  }

  public LoadService getLoadService() {
    return loadService;
  }

  public List<ProductShortInfo> getItems() {
    return tableView.getItems();
  }

  public void load() {
    loadService.restart();
  }

  class LoadService extends AbstractAsyncService<List<ProductShortInfo>> {
    @Override
    protected Task<List<ProductShortInfo>> createTask() {
      return new Task<List<ProductShortInfo>>() {
        @Override
        protected List<ProductShortInfo> call() throws Exception {
          APIRequest request = HttpServiceUtil.get("/aukro/ua");

          return request.getData(new TypeReference<List<ProductShortInfo>>() {
          });
        }
      };
    }
  }
}

abstract class FilterToolBar extends ToolBar {
  private NumberTextField weightField;
  private NumberTextField priceWeightCofField;

  public FilterToolBar() {
    getItems().add(new Label(I18n.UA.getString(I18nKeys.WEIGHT) + " <= :"));
    getItems().add(weightField = new NumberTextField(null, true));
    getItems().add(new Label(I18n.UA.getString(I18nKeys.PRICE_WEIGHT_COF) + " >= :"));
    getItems().add(priceWeightCofField = new NumberTextField(null, true));

    Button filter;
    getItems().add(filter = new Button(I18n.UA.getString(I18nKeys.FILTER)));

    filter.setOnAction(event -> filter(weightField.getNumber(), priceWeightCofField.getNumber()));
  }

  protected abstract void filter(BigDecimal weight, BigDecimal priceWeightCof);
}