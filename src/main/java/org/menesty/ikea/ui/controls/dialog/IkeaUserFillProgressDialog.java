package org.menesty.ikea.ui.controls.dialog;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.menesty.ikea.ui.TaskProgressLog;

public class IkeaUserFillProgressDialog extends BaseDialog implements TaskProgressLog {

    private VBox logBox;

    private Label activeItem;

    public IkeaUserFillProgressDialog() {
        setAllowAutoHide(false);
        cancelBtn.setVisible(false);
        okBtn.setVisible(false);
        addRow(logBox = new VBox(), bottomBar);

    }


    @Override
    public void addLog(String log) {
        activeItem = new Label(log);

        final Label currentActive = activeItem;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                logBox.getChildren().add(currentActive);
            }
        });

    }

    @Override
    public void updateLog(final String log) {
        if (activeItem == null) addLog(log);
        else {
            final Label currentActive = activeItem;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    currentActive.setText(log);
                }
            });
        }

    }

    @Override
    public void done() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                okBtn.setVisible(true);
            }
        });
    }
}


