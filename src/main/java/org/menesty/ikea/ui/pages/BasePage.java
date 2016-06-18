package org.menesty.ikea.ui.pages;

import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.pane.LoadingPane;

public abstract class BasePage {
    protected LoadingPane loadingPane;

    private boolean initialized;

    private DialogSupport dialogSupport;

    public BasePage() {
    }

    protected abstract Node createView();

    private StackPane createRoot() {
        StackPane pane = new StackPane() {
            @Override
            protected void layoutChildren() {
                double width = getWidth();
                ///System.out.println("width = " + width);
                double height = getHeight();
                ///System.out.println("height = " + height);
                double top = getInsets().getTop();
                double right = getInsets().getRight();
                double left = getInsets().getLeft();
                double bottom = getInsets().getBottom();
                for (Node child : getManagedChildren()) {
                    layoutInArea(child, left, top,
                            width - left - right, height - top - bottom,
                            0, Insets.EMPTY, true, true, HPos.CENTER, VPos.CENTER);
                }
            }

        };

        VBox.setVgrow(pane, Priority.ALWAYS);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setMaxHeight(Double.MAX_VALUE);

        pane.getChildren().add(loadingPane = new LoadingPane());
        return pane;
    }

    protected StackPane wrap(Node container) {
        StackPane pane = createRoot();
        pane.getChildren().add(0, container);
        return pane;
    }

    protected <T> void runTask(Task<T> task) {
        loadingPane.bindTask(task);
        new Thread(task).start();
    }

    protected void showPopupDialog(BaseDialog node) {
        dialogSupport.showPopupDialog(node);
    }

    protected void hidePopupDialog() {
        dialogSupport.hidePopupDialog();
    }

    protected void navigateSubPage(Class<? extends BasePage> pageClass, Object... params) {
        dialogSupport.navigate(dialogSupport.getActivePage(), pageClass, params);
    }

    protected void navigate(Class<? extends BasePage> pageClass, Object... params) {
        dialogSupport.navigate(null, pageClass, params);
    }

    protected DialogSupport getDialogSupport() {
        return dialogSupport;
    }

    protected Stage getStage() {
        return dialogSupport.getStage();
    }

    public void onActive(Object... params) {

    }

    protected void initialize() {

    }

    public void onDeactivate() {

    }

    public void setDialogSupport(DialogSupport dialogSupport) {
        this.dialogSupport = dialogSupport;
    }

    public Node getView() {
        if (!initialized) {
            initialize();
            initialized = true;
        }

        return createView();
    }

    @SuppressWarnings("unchecked")
    protected <T> T cast(Object object) {
        return (T) object;
    }
}