package org.menesty.ikea.ui.controls.pane.wizard;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * Created by Menesty on
 * 9/7/15.
 * 03:20.
 */
public abstract class BaseWizardStep<T> extends AnchorPane implements WizardStep<T> {
    private WizardPanel<T> wizardPanel;

    public BaseWizardStep() {
        getStyleClass().add("application-pane");
    }

    public void setContent(Node node) {
        getChildren().clear();
        AnchorPane.setBottomAnchor(node, 0d);
        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);

        getChildren().add(node);
    }

    public void setWizardPanel(WizardPanel<T> wizardPanel) {
        this.wizardPanel = wizardPanel;
    }

    protected WizardPanel<T> getWizardPanel() {
        return wizardPanel;
    }
}
