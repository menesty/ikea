package org.menesty.ikea.ui.controls;

import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.util.ClipboardUtil;
import org.menesty.ikea.util.NumberUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class TotalStatusPanel extends ToolBar {
    private Label totalLabel;
    private BigDecimal total;

    public TotalStatusPanel() {
        this(I18n.UA.getString(I18nKeys.TOTAL_AMOUNT));
    }

    public TotalStatusPanel(String totalLabel) {
        getItems().add(new Label(totalLabel + " :"));
        getItems().add(this.totalLabel = new Label());
        this.totalLabel.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                ClipboardUtil.copy(TotalStatusPanel.this.totalLabel.getText());
            }
        });
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
        setTotal(total.doubleValue());
    }

    public void setTotal(double total) {
        totalLabel.setText(NumberFormat.getNumberInstance().format(NumberUtil.round(total)));
    }

    protected BigDecimal getTotal() {
        return total;
    }
}
