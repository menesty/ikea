package org.menesty.ikea.ui.controls;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import org.menesty.ikea.util.ClipboardUtil;
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
        this.totalLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2)
                    ClipboardUtil.copy(TotalStatusPanel.this.totalLabel.getText());
            }
        });
    }

    public void setTotal(double total) {
        totalLabel.setText(NumberFormat.getNumberInstance().format(NumberUtil.round(total)));
    }
}
