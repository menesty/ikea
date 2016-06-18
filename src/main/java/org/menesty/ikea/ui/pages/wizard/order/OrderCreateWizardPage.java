package org.menesty.ikea.ui.pages.wizard.order;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.lib.domain.order.IkeaProcessOrder;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.pane.wizard.WizardPanel;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.ikea.order.IkeaOrderViewPage;
import org.menesty.ikea.ui.pages.wizard.order.step.Step1;
import org.menesty.ikea.ui.pages.wizard.order.step.Step2ParseDocuments;
import org.menesty.ikea.ui.pages.wizard.order.step.Step3OrderPreview;
import org.menesty.ikea.ui.pages.wizard.order.step.Step4PriceMismatch;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

public class OrderCreateWizardPage extends BasePage {
  private CreateOrderService createOrderService;

  @Override
  protected void initialize() {
    createOrderService = new CreateOrderService();
  }

  @Override
  protected Node createView() {
    WizardPanel<DesktopOrderInfo> wizardPanel = new WizardPanel<>(new DesktopOrderInfo());

    wizardPanel.addStep(new Step1(getStage()));
    wizardPanel.addStep(new Step2ParseDocuments());
    wizardPanel.addStep(new Step3OrderPreview(getDialogSupport()));
    wizardPanel.addStep(new Step4PriceMismatch(getDialogSupport()));
    wizardPanel.setOnFinishListener(param -> {
      createOrderService.property.set(param);
      loadingPane.bindTask(createOrderService);
      createOrderService.restart();
    });
    createOrderService.setOnSucceededListener(value -> {
      navigate(IkeaOrderViewPage.class, value);
    });

    wizardPanel.start();

    StackPane mainPane = new StackPane();
    mainPane.getChildren().add(wizardPanel);
    mainPane.setPadding(new Insets(0, 0, 5, 0));
    mainPane.getStyleClass().add("wizard-pane");

    return wrap(mainPane);
  }

  class CreateOrderService extends AbstractAsyncService<IkeaProcessOrder> {
    private ObjectProperty<DesktopOrderInfo> property = new SimpleObjectProperty<>();

    @Override
    protected Task<IkeaProcessOrder> createTask() {
      DesktopOrderInfo _desktopOrderInfo = property.get();
      return new Task<IkeaProcessOrder>() {
        @Override
        protected IkeaProcessOrder call() throws Exception {
          APIRequest apiRequest = HttpServiceUtil.get("/order/create");

          return apiRequest.postData(_desktopOrderInfo, IkeaProcessOrder.class);
        }
      };
    }
  }
}
