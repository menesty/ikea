package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.Label;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.ui.pages.DialogCallback;

/**
 * User: Menesty
 * Date: 11/6/13
 * Time: 9:36 PM
 */
public class Dialog {
    private static ConfirmDialog confirmDialog;
    private static AlertDialog alertDialog;

    public static ConfirmDialog confirm(String message, DialogCallback callback) {
        return confirm("Warning", message, callback);
    }

    public static ConfirmDialog confirm(String title, String message, DialogCallback callback) {
        if (confirmDialog == null)
            confirmDialog = new ConfirmDialog();

        confirmDialog.show(title, message, callback);
        return confirmDialog;
    }

    public static void alert(String title, String message) {
        if (alertDialog == null)
            alertDialog = new AlertDialog();

        alertDialog.show(title, message);
    }

    static class AlertDialog extends BaseDialog {
        private Label message;

        public AlertDialog() {
            setTitle("Warning");
            okBtn.setText("Ok");
            cancelBtn.setVisible(false);
            addRow(message = new Label(), bottomBar);
            okBtn.setDefaultButton(true);
            setAllowAutoHide(false);
        }

        @Override
        public void onOk() {
            IkeaApplication.get().hidePopupDialog(false);
        }

        public void show(String title, String message) {
            setTitle(title);
            this.message.setText(message);

            IkeaApplication.get().showPopupDialog(this);
        }
    }

    static class ConfirmDialog extends BaseDialog {
        private Label message;

        private DialogCallback callback;

        public ConfirmDialog() {
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

            IkeaApplication.get().showPopupDialog(this);
        }

        @Override
        public void onCancel() {
            IkeaApplication.get().hidePopupDialog(false);

            if (callback != null) callback.onCancel();

        }

        @Override
        public void onOk() {
            IkeaApplication.get().hidePopupDialog(false);

            if (callback != null) callback.onYes();
        }
    }
}

