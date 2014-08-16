package org.menesty.ikea.ui.controls.pane;

import javafx.scene.layout.BorderPane;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;

/**
 * Created by Menesty on
 * 8/13/14.
 * 7:37.
 */
public class BaseBorderPane extends BorderPane {
    protected void showPopupDialog(BaseDialog node) {
        IkeaApplication.get().showPopupDialog(node);
    }

    protected void hidePopupDialog() {
        IkeaApplication.get().hidePopupDialog();
    }
}
