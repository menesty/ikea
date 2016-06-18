package org.menesty.ikea.ui.controls;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by Menesty on
 * 3/31/16.
 * 09:36.
 */
public class CustomTotalStatusPanel extends TotalStatusPanel{
  private Label currentTotal;

  public CustomTotalStatusPanel(){
    this("Current  :");
  }

  public CustomTotalStatusPanel(String currentLabel){
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    getItems().add(spacer);
    getItems().add(new Label(currentLabel));
    getItems().add(currentTotal = new Label());
  }

  public void setCurrentTotal(BigDecimal total) {
    currentTotal.setText(NumberFormat.getNumberInstance().format(total));
  }
}
