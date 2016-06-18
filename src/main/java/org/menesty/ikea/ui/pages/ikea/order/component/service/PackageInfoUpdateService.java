package org.menesty.ikea.ui.pages.ikea.order.component.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.lib.domain.ikea.IkeaPackageInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

/**
 * Created by Menesty on
 * 2/11/16.
 * 18:42.
 */
public class PackageInfoUpdateService extends AbstractAsyncService<Void> {
  private ObjectProperty<IkeaPackageInfo> packageInfoProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<Void> createTask() {
    final IkeaPackageInfo _ikeaPackageInfo = packageInfoProperty.get();

    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        APIRequest request = HttpServiceUtil.get("/packageinfo/update");
        request.postData(_ikeaPackageInfo);

        return null;
      }
    };
  }

  public void setPackageInfo(IkeaPackageInfo packageInfo) {
    packageInfoProperty.setValue(packageInfo);
  }
}
