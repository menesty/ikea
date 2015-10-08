package org.menesty.ikea.ui.pages.ikea.order.dialog.export;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.ui.TaskProgressLog;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;

/**
 * Created by Menesty on
 * 10/4/15.
 * 16:20.
 */
public class ExportStep2 extends BaseWizardStep<IkeaSiteExportDialog.IkeaSiteExportInfo> implements TaskProgressLog {
    private LoadService loadService;

    private VBox logPanel;
    private Label activeItem;

    public ExportStep2() {
        loadService = new LoadService();
        loadService.setOnSucceededListener(value -> getWizardPanel().unLockButtons());
        setContent(logPanel = new VBox());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean canSkip(IkeaSiteExportDialog.IkeaSiteExportInfo param) {
        return false;
    }

    @Override
    public void collect(IkeaSiteExportDialog.IkeaSiteExportInfo param) {

    }

    @Override
    public void onActive(IkeaSiteExportDialog.IkeaSiteExportInfo param) {
        logPanel.getChildren().clear();
        getWizardPanel().lockButtons();
        loadService.setExportInfo(param);
        loadService.restart();
    }

    @Override
    public void addLog(String log) {
        activeItem = new Label(log);

        final Label currentActive = activeItem;
        Platform.runLater(() -> logPanel.getChildren().add(currentActive));
    }

    @Override
    public void updateLog(String log) {
        if (activeItem == null) addLog(log);
        else {
            final Label currentActive = activeItem;

            Platform.runLater(() -> currentActive.setText(log));
        }
    }

    @Override
    public void done() {

    }

    class LoadService extends AbstractAsyncService<Void> {
        private ObjectProperty<IkeaSiteExportDialog.IkeaSiteExportInfo> exportInfoObjectProperty = new SimpleObjectProperty<>();

        @Override
        protected Task<Void> createTask() {
            final IkeaSiteExportDialog.IkeaSiteExportInfo _exportInfo = exportInfoObjectProperty.get();
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    ServiceFacade.getIkeaExportService().export(_exportInfo.getItemsToExport(), _exportInfo.getUsers(), _exportInfo.getShops(), _exportInfo.isSplitGroup(), ExportStep2.this);
                    return null;
                }
            };
        }

        public void setExportInfo(IkeaSiteExportDialog.IkeaSiteExportInfo info) {
            exportInfoObjectProperty.set(info);
        }
    }
}


