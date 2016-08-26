package org.menesty.ikea.util;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.menesty.ikea.beans.property.SimpleBigDecimalProperty;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.WarehouseAvailableItem;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.PathProperty;
import org.menesty.ikea.ui.pages.ikea.resumption.dialog.ResumptionItemAddDialog;

import javax.swing.text.html.parser.Entity;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ColumnUtil {
  public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";

  public static <Entity, Value> Callback<TableColumn.CellDataFeatures<Entity, Value>, ObservableValue<Value>> column(final String propertyName) {
    try {
      return item -> new PathProperty<>(item.getValue(), propertyName);
    } catch (Exception e) {
      ServiceFacade.getErrorConsole().add(e);
    }

    return item -> new SimpleObjectProperty<>();
  }

  public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, Number>, ObservableValue<Number>> indexColumn() {
    return item -> new SimpleIntegerProperty(item.getTableView().getItems().indexOf(item.getValue()) + 1);
  }

  public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, String>, ObservableValue<String>> dateColumn(final String propertyName) {
    return dateColumn(DEFAULT_DATE_FORMAT, propertyName);
  }

  public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, String>, ObservableValue<String>> dateColumn(String pattern, final String propertyName) {
    return item -> {
      Date date = new PathProperty<Entity, Date>(item.getValue(), propertyName).get();
      if (date != null) {
        return new SimpleStringProperty(new SimpleDateFormat(pattern).format(date));
      }

      return new SimpleStringProperty("");
    };
  }

  public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, String>, ObservableValue<String>> number(final String propertyName) {
    return item -> {
      try {
        Number number = new PathProperty<Entity, Number>(item.getValue(), propertyName).get();
        return number != null ? new SimpleStringProperty(NumberUtil.toString(number.doubleValue())) : null;
      } catch (Exception e) {
        ServiceFacade.getErrorConsole().add(e);
      }

      return new SimpleStringProperty("Error");
    };
  }

  public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, String>, ObservableValue<String>> grToKg(String propertyName) {
    return item -> {
      try {
        Number number = new PathProperty<Entity, Number>(item.getValue(), propertyName).get();

        if (number != null && number.intValue() != 0) {
          return new SimpleStringProperty(number.intValue() / 1000 + " kg");
        } else {
          return new SimpleStringProperty("0 kg");
        }
      } catch (Exception e) {
        ServiceFacade.getErrorConsole().add(e);
      }

      return new SimpleStringProperty("Error");
    };
  }

  public static <Entity> Callback<TableColumn.CellDataFeatures<Entity, Number>, ObservableValue<Number>> bigDecimal(String propertyName) {
    return item -> {
      try {
        BigDecimal number = new PathProperty<Entity, BigDecimal>(item.getValue(), propertyName).get();
        if (number != null) {
          return new SimpleBigDecimalProperty(number);
        }
      } catch (Exception e) {
        ServiceFacade.getErrorConsole().add(e);
      }

      return new SimpleBigDecimalProperty();
    };
  }
}
