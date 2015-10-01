package org.menesty.ikea.ui.pages.ikea.order.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import org.menesty.ikea.lib.domain.IkeaShop;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.controls.pane.wizard.WizardPanel;

import java.util.List;

/**
 * Created by Menesty on
 * 10/1/15.
 * 19:29.
 */
public class IkeaSiteExportDialog extends BaseDialog {
    private IkeaSiteExportInfo ikeaSiteExportInfo;

    public IkeaSiteExportDialog(Stage stage) {
        super(stage);
        WizardPanel<IkeaSiteExportInfo> wizardPanel = new WizardPanel<>(ikeaSiteExportInfo);

        addRow(wizardPanel);
    }

    public void show(){

    }

    class IkeaSiteExportInfo {
        private List<String> users;
        private List<IkeaShop> shops;
        private boolean splitGroup;

        private List<IkeaClientOrderItemDto> ikeaClientOrderItemDtos;

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
    }
}

class Step1 extends BaseWizardStep<IkeaSiteExportDialog.IkeaSiteExportInfo> {
    private ListView<ProfileModel> listView;

    public Step1() {
        FormPane formPane = new FormPane();
        listView = new ListView<>();
        listView.setCellFactory(CheckBoxListCell.forListView(ProfileModel::checkedProperty));

        formPane.addRow(listView);

        getChildren().add(formPane);
    }

    @Override
    public boolean isValid() {
        return false;
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

    }

    class ProfileModel {
        private BooleanProperty checked = new SimpleBooleanProperty();
        private final Profile profile;

        public ProfileModel(Profile profile) {
            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }

        public boolean isChecked() {
            return checked.get();
        }

        public BooleanProperty checkedProperty() {
            return checked;
        }
    }
}
