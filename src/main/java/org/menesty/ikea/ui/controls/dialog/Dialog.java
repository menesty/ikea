package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.Label;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.ui.pages.DialogCallback;

/**
 * User: Menesty
 * Date: 11/6/13
 * Time: 9:36 PM
 */
public class Dialog {
    private static ConfirmDialog confirmDialog;
    private static AlertDialog alertDialog;

    public static ConfirmDialog confirm(DialogSupport dialogSupport, String message, DialogCallback callback) {
        return confirm(dialogSupport, "Warning", message, callback);
    }

    public static ConfirmDialog confirm(DialogSupport dialogSupport, String title, String message, DialogCallback callback) {
        if (confirmDialog == null)
            confirmDialog = new ConfirmDialog(dialogSupport);

        confirmDialog.show(title, message, callback);
        return confirmDialog;
    }

    public static void alert(DialogSupport dialogSupport, String title, String message) {
        if (alertDialog == null)
            alertDialog = new AlertDialog(dialogSupport);

        alertDialog.show(title, message);
    }

    static class AlertDialog extends BaseDialog {
        private Label message;

        private final DialogSupport dialogSupport;

        public AlertDialog(final DialogSupport dialogSupport) {
            super(dialogSupport.getStage());
            this.dialogSupport = dialogSupport;

            setTitle("Warning");
            okBtn.setText("Ok");
            cancelBtn.setVisible(false);
            addRow(message = new Label(), bottomBar);
            okBtn.setDefaultButton(true);
            setAllowAutoHide(false);
        }

        @Override
        public void onOk() {
            dialogSupport.hidePopupDialog(false);
        }

        public void show(String title, String message) {
            setTitle(title);
            this.message.setText(message);

            dialogSupport.showPopupDialog(this);
        }
    }

    static class ConfirmDialog extends BaseDialog {
        private Label message;

        private DialogCallback callback;

        private final DialogSupport dialogSupport;

        public ConfirmDialog(DialogSupport dialogSupport) {
            super(dialogSupport.getStage());
            this.dialogSupport = dialogSupport;
            setTitle("Warning");
            okBtn.setText("Yes");
            cancelBtn.setText("No");
            addRow(message = new Label(), bottomBar);
            cancelBtn.setDefaultButton(true);
            okBtn.setDefaultButton(false);
            setAllowAutoHide(false);
        }


        public void show(String title, String message, DialogCallback callback) {
            setTitle(title);
            this.message.setText(message);
            this.callback = callback;

            dialogSupport.showPopupDialog(this);
        }

        @Override
        public void onCancel() {
            dialogSupport.hidePopupDialog(false);

            if (callback != null) callback.onCancel();

        }

        @Override
        public void onOk() {
            dialogSupport.hidePopupDialog(false);

            if (callback != null) callback.onYes();
        }
    }
}

