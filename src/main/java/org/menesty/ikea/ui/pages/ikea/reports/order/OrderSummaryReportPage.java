package org.menesty.ikea.ui.pages.ikea.reports.order;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.ui.controls.pane.wizard.WizardPanel;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.ikea.order.IkeaProcessOrderPage;
import org.menesty.ikea.ui.pages.ikea.reports.order.step.Step1;
import org.menesty.ikea.ui.pages.ikea.reports.order.step.Step2ParseDocuments;
import org.menesty.ikea.ui.pages.ikea.reports.order.step.Step3OrderReportPreview;

/**
 * Created by Menesty on
 * 5/24/16.
 * 15:41.
 */
public class OrderSummaryReportPage extends BasePage {
  @Override
  protected Node createView() {
    WizardPanel<OrderReportInfo> wizardPanel = new WizardPanel<>(new OrderReportInfo());

    wizardPanel.addStep(new Step1(getStage()));
    wizardPanel.addStep(new Step2ParseDocuments());
    wizardPanel.addStep(new Step3OrderReportPreview(getDialogSupport()));
    wizardPanel.setOnFinishListener(param -> navigate(IkeaProcessOrderPage.class));

    wizardPanel.start();

    StackPane mainPane = new StackPane();
    mainPane.getChildren().add(wizardPanel);
    mainPane.setPadding(new Insets(0, 0, 5, 0));
    mainPane.getStyleClass().add("wizard-pane");

    return wrap(mainPane);
  }
}
