package org.menesty.ikea.core.component;

import javafx.stage.Stage;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.pages.BasePage;

/**
 * Created by Menesty on
 * 10/13/14.
 * 2:58.
 */
public interface DialogSupport {
    void showPopupDialog(BaseDialog node);

    void hidePopupDialog();

    void hidePopupDialog(boolean animate);

    Stage getStage();

    void navigate(PageDescription parent, Class<? extends BasePage> subPage, Object... prams);

    PageDescription getActivePage();
}
