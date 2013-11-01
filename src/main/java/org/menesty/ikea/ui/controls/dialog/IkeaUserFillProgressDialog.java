package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.menesty.ikea.ui.TaskProgressLog;

public class IkeaUserFillProgressDialog extends BaseDialog implements TaskProgressLog {

    private VBox logBox;

    private Label activeItem;

    public IkeaUserFillProgressDialog() {
        cancelBtn.setVisible(false);
        getChildren().addAll(logBox = new VBox(), bottomBar);

    }


    @Override
    public void addLog(String log) {
        activeItem = new Label(log);
        logBox.getChildren().add(activeItem);
    }

    @Override
    public void updateLog(String log) {
        if (activeItem == null) addLog(log);
        else
            activeItem.setText(log);
    }

    @Override
    public void done() {

    }
}


