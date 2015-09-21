package org.menesty.ikea.ui.controls.pane.wizard;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Menesty on
 * 9/7/15.
 * 02:58.
 */
public class WizardPanel<T> extends BorderPane {
    public interface OnFinishListener<T> {
        void onFinish(T param);
    }

    public interface ActionButton {
        void onNext();

        void onFinish();

        void onPrevious();
    }

    private List<BaseWizardStep<T>> steps;
    private T wizardParameter;
    private BaseWizardStep<T> activeStep;
    private WizardButtonBar wizardButtonBar;
    private OnFinishListener<T> onFinishListener;

    public WizardPanel(T wizardParameter) {
        this.wizardParameter = wizardParameter;
        steps = new ArrayList<>();
        setBottom(wizardButtonBar = new WizardButtonBar(new ActionButton() {
            @Override
            public void onNext() {
                if (activeStep.isValid()) {
                    activeStep.collect(wizardParameter);
                    setStep(getNext().get());
                }
            }

            @Override
            public void onFinish() {
                if (activeStep.isValid()) {
                    activeStep.collect(wizardParameter);

                    if (onFinishListener != null) {
                        onFinishListener.onFinish(wizardParameter);
                    }
                }
            }

            @Override
            public void onPrevious() {
                setStep(getPrevious().get());
            }
        }));
    }

    public void addStep(BaseWizardStep<T> step) {
        steps.add(step);
        step.setWizardPanel(this);
        updateControlButtons();
    }

    public void setOnFinishListener(OnFinishListener<T> listener) {
        this.onFinishListener = listener;
    }

    public void lockButtons() {
        wizardButtonBar.setDisable(true);
    }

    public void unLockButtons() {
        wizardButtonBar.setDisable(false);
    }

    public void start() {
        if (!steps.isEmpty()) {
            Optional<BaseWizardStep<T>> firstStep = steps
                    .stream()
                    .filter(tBaseWizardStep -> !tBaseWizardStep.canSkip(wizardParameter))
                    .findFirst();

            if (firstStep.isPresent()) {
                setStep(firstStep.get());
            } else {
                throw new RuntimeException("No first step for start");
            }
        }
    }

    private void setStep(BaseWizardStep<T> step) {
        setCenter(activeStep = step);
        activeStep.onActive(wizardParameter);
        updateControlButtons();
    }

    private Optional<BaseWizardStep<T>> getNext() {
        int index = steps.indexOf(activeStep);
        List<BaseWizardStep<T>> subList = steps.subList(index + 1, steps.size());

        if (subList.isEmpty()) {
            return Optional.ofNullable(null);
        }

        return subList.stream().filter(tBaseWizardStep -> !tBaseWizardStep.canSkip(wizardParameter)).findFirst();
    }

    private Optional<BaseWizardStep<T>> getPrevious() {
        int index = steps.indexOf(activeStep);
        List<BaseWizardStep<T>> subList = new ArrayList<>(steps.subList(0, index));

        if (subList.isEmpty()) {
            return Optional.ofNullable(null);
        }

        Collections.reverse(subList);

        return subList.stream().filter(tBaseWizardStep -> !tBaseWizardStep.canSkip(wizardParameter)).findFirst();
    }

    private void updateControlButtons() {
        int index = steps.indexOf(activeStep);

        if (index != -1) {
            Optional<BaseWizardStep<T>> previous = getPrevious();

            if (!previous.isPresent()) {
                wizardButtonBar.hidePrevious();
            } else {
                wizardButtonBar.showPrevious();
            }

            Optional<BaseWizardStep<T>> next = getNext();

            if (!next.isPresent()) {
                wizardButtonBar.showFinish();
            } else {
                wizardButtonBar.showNext().hideFinish();
            }
        }
    }
}

class WizardButtonBar extends ToolBar {

    private Button next;
    private Button finish;
    private Button previous;

    public WizardButtonBar(WizardPanel.ActionButton actionButton) {
        next = new Button(I18n.UA.getString(I18nKeys.NEXT));
        next.setOnAction(event -> actionButton.onNext());

        finish = new Button(I18n.UA.getString(I18nKeys.FINISH));
        finish.setOnAction(event -> actionButton.onFinish());

        previous = new Button(I18n.UA.getString(I18nKeys.PREVIOUS));
        previous.setOnAction(event -> actionButton.onPrevious());

        getItems().addAll(previous, next, finish);
    }

    public WizardButtonBar showFinish() {
        finish.setVisible(true);
        hideNext();
        return this;
    }

    public WizardButtonBar showNext() {
        next.setVisible(true);
        return this;
    }

    public WizardButtonBar showPrevious() {
        previous.setVisible(true);
        return this;
    }

    public WizardButtonBar hideFinish() {
        finish.setVisible(false);
        return this;
    }

    public WizardButtonBar hidePrevious() {
        previous.setVisible(false);
        return this;
    }

    public WizardButtonBar hideNext() {
        next.setVisible(false);
        return this;
    }
}

