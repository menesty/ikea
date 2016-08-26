package org.menesty.ikea.ui.pages.ikea.reports.aukro;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.menesty.ikea.beans.property.SimpleBigDecimalProperty;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.product.Product;
import org.menesty.ikea.lib.domain.product.ProductShortInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.wizard.order.step.service.AbstractExportAsyncService;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.ColumnUtil;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.HttpServiceUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 6/18/16.
 * 20:14.
 */
public class AukroReportPage extends BasePage {
  private AukroComponent aukroComponent;

  private AbstractExportAsyncService<List<ProductShortInfo>> xlsResultExportAsyncService;

  @Override
  protected void initialize() {


    xlsResultExportAsyncService = new AbstractExportAsyncService<List<ProductShortInfo>>() {
      @Override
      protected void export(File file, List<ProductShortInfo> items) {
        ServiceFacade.getXlsExportService().exportProductShortInfos(file, items);
      }
    };
  }

  @Override
  protected Node createView() {
    aukroComponent = new AukroComponent();

    BorderPane main = new BorderPane();
    main.setCenter(aukroComponent);


    ToolBar toolBar = new ToolBar();
    {
      Button button = new Button(null, ImageFactory.createXlsExport32Icon());

      button.setOnAction(event -> {
        File selectedFile = FileChooserUtil.getXls().showSaveDialog(getDialogSupport().getStage());

        if (selectedFile != null) {
          xlsResultExportAsyncService.setFile(selectedFile);
          xlsResultExportAsyncService.setParam(aukroComponent.getItems());
          xlsResultExportAsyncService.restart();
        }
      });

      toolBar.getItems().add(button);
    }

    main.setTop(toolBar);

    return wrap(main);
  }

  @Override
  public void onActive(Object... params) {
    loadingPane.bindTask(aukroComponent.getLoadService());
    aukroComponent.load();
  }


}


