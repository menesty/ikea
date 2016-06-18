package org.menesty.ikea.ui.pages.ikea.order.dialog.export;

import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.IkeaShop;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.pane.wizard.WizardPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 10/1/15.
 * 19:29.
 */
public class IkeaSiteExportDialog extends BaseDialog {
    private IkeaSiteExportInfo ikeaSiteExportInfo;
    private WizardPanel<IkeaSiteExportInfo> wizardPanel;

    public IkeaSiteExportDialog(Stage stage) {
        super(stage);
        setTitle(I18n.UA.getString(I18nKeys.IKEA_SITE_EXPORT));
        setAllowAutoHide(false);

        wizardPanel = new WizardPanel<>(ikeaSiteExportInfo = new IkeaSiteExportInfo());
        wizardPanel.addStep(new ExportStep1());
        wizardPanel.addStep(new ExportStep2());
        wizardPanel.setOnFinishListener(param -> onActionOk());

        Button button = new Button(I18n.UA.getString(I18nKeys.CLOSE));
        button.setOnAction(event -> onActionOk());
        wizardPanel.addButton(button);

        setMaxHeight(600);
        VBox.setVgrow(wizardPanel, Priority.ALWAYS);
        addRow(wizardPanel);

        cancelBtn.setVisible(false);
        okBtn.setVisible(false);
    }

    public void bind(List<IkeaClientOrderItemDto> ikeaClientOrderItemDtos) {
        ikeaSiteExportInfo.setIkeaClientOrderItemDtos(ikeaClientOrderItemDtos);
    }

    @Override
    public void onShow() {
        wizardPanel.start();
    }

    class IkeaSiteExportInfo {
        private List<String> users = new ArrayList<>();
        private List<IkeaShop> shops = new ArrayList<>();
        private boolean splitGroup = true;
        private List<IkeaOrderItem> itemsToExport = new ArrayList<>();
        private List<IkeaClientOrderItemDto> ikeaClientOrderItemDtos = new ArrayList<>();
        private List<Profile> profiles = new ArrayList<>();

        public List<IkeaOrderItem> getItemsToExport() {
            return itemsToExport;
        }

        public void setItemsToExport(List<IkeaOrderItem> itemsToExport) {
            this.itemsToExport = itemsToExport;
        }

        public List<String> getUsers() {
            return users;
        }

        public void setUsers(List<String> users) {
            this.users = users;
        }

        public List<IkeaShop> getShops() {
            return shops;
        }

        public void setShops(List<IkeaShop> shops) {
            this.shops = shops;
        }

        public boolean isSplitGroup() {
            return splitGroup;
        }

        public void setSplitGroup(boolean splitGroup) {
            this.splitGroup = splitGroup;
        }

        public List<IkeaClientOrderItemDto> getIkeaClientOrderItemDtos() {
            return ikeaClientOrderItemDtos;
        }

        public void setIkeaClientOrderItemDtos(List<IkeaClientOrderItemDto> ikeaClientOrderItemDtos) {
            this.ikeaClientOrderItemDtos = ikeaClientOrderItemDtos;
        }

        public void setProfiles(List<Profile> profiles) {
            this.profiles = profiles;
        }

        public List<Profile> getProfiles() {
            return profiles;
        }
    }
}


