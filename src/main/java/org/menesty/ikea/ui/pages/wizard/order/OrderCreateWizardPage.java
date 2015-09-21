package org.menesty.ikea.ui.pages.wizard.order;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.menesty.ikea.lib.dto.DesktopOrderInfo;
import org.menesty.ikea.ui.controls.pane.wizard.WizardPanel;
import org.menesty.ikea.ui.pages.BasePage;
import org.menesty.ikea.ui.pages.wizard.order.step.Step1;
import org.menesty.ikea.ui.pages.wizard.order.step.Step2ParseDocuments;
import org.menesty.ikea.ui.pages.wizard.order.step.Step3OrderPreview;
import org.menesty.ikea.ui.pages.wizard.order.step.Step4PriceMismatch;

public class OrderCreateWizardPage extends BasePage {
    public OrderCreateWizardPage() {
        super("");
    }

    @Override
    protected Node createView() {
        WizardPanel<DesktopOrderInfo> wizardPanel = new WizardPanel<>(new DesktopOrderInfo());

        wizardPanel.addStep(new Step1(getStage()));
        wizardPanel.addStep(new Step2ParseDocuments());
        wizardPanel.addStep(new Step3OrderPreview(getDialogSupport()));
        wizardPanel.addStep(new Step4PriceMismatch(getDialogSupport()));
        wizardPanel.setOnFinishListener(param -> {

        });

        wizardPanel.start();

        StackPane mainPane = new StackPane();
        mainPane.getChildren().add(wizardPanel);

        return wrap(mainPane);
    }
}
