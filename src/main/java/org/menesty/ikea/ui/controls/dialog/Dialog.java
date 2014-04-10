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

    public static ConfirmDialog confirm(String message, DialogCallback callback) {
        return confirm("Warning", message, callback);
    }

    public static ConfirmDialog confirm(String title, String message, DialogCallback callback) {
        if (confirmDialog == null)
            confirmDialog = new ConfirmDialog();

        confirmDialog.show(title, message, callback);
        return confirmDialog;
    }

    static class ConfirmDialog extends BaseDialog {
        private Label message;

        private boolean isReleased = true;

        private DialogCallback callback;

        public ConfirmDialog() {
            setTitle("Warning");
            okBtn.setText("Yes");
            cancelBtn.setText("No");
            addRow(message = new Label(), bottomBar);
            cancelBtn.setDefaultButton(true);
            okBtn.setDefaultButton(false);
        }


        public void show(String title, String message, DialogCallback callback) {
            setTitle(title);
            this.message.setText(message);
            this.callback = callback;
            isReleased = false;

            IkeaApplication.get().showPopupDialog(this);
        }

        @Override
        public void onCancel() {
            if (callback != null) callback.onCancel();
            isReleased = true;
            IkeaApplication.get().hidePopupDialog();
        }

        @Override
        public void onOk() {
            if (callback != null) callback.onYes();
            isReleased = true;
            IkeaApplication.get().hidePopupDialog();
        }

    }
}

