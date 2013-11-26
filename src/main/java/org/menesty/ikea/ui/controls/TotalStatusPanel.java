package org.menesty.ikea.ui.controls;

import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.util.NumberUtil;

import java.text.NumberFormat;

public class TotalStatusPanel extends ToolBar {
    private Label totalLabel;

    public TotalStatusPanel() {
        this("Total");
    }

    public TotalStatusPanel(String totalLabel) {
        getItems().add(new Label(totalLabel + " :"));
        getItems().add(this.totalLabel = new Label());
    }

    public void setTotal(double total) {
        totalLabel.setText(NumberFormat.getNumberInstance().format(NumberUtil.round(total)));
    }
}
