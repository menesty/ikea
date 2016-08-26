package org.menesty.ikea.ui.pages.ikea.vk.dialog;

import javafx.stage.Stage;
import org.menesty.ikea.lib.domain.product.ProductShortInfo;
import org.menesty.ikea.ui.CallBack;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.pages.ikea.reports.aukro.AukroComponent;

import java.util.List;

/**
 * Created by Menesty on
 * 7/16/16.
 * 16:59.
 */
public class VKImportProductDialog extends BaseDialog {
  private AukroComponent aukroComponent;
  private CallBack<List<ProductShortInfo>> callback;

  public VKImportProductDialog(Stage stage) {
    super(stage);

    setMaxSize(700, USE_PREF_SIZE);
    addRow(aukroComponent = new AukroComponent(), bottomBar);

    loadingPane.bindTask(aukroComponent.getLoadService());
  }

  @Override
  public void onOk() {
    callback.onResult(aukroComponent.getItems());
  }

  public void setCallBack(CallBack<List<ProductShortInfo>> callBack) {
    this.callback = callBack;
  }

  @Override
  public void onShow() {
    super.onShow();

    aukroComponent.load();
  }
}
