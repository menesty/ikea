package org.menesty.ikea.ui.pages.ikea.order.dialog.export;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.IkeaShop;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.order.IkeaClientOrderItemDto;
import org.menesty.ikea.lib.dto.IkeaOrderItem;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.ListEditField;
import org.menesty.ikea.ui.controls.form.ListViewField;
import org.menesty.ikea.ui.controls.form.WrapField;
import org.menesty.ikea.ui.controls.form.provider.CachedAsyncDataProvider;
import org.menesty.ikea.ui.controls.form.provider.DataProvider;
import org.menesty.ikea.ui.controls.pane.wizard.BaseWizardStep;
import org.menesty.ikea.ui.pages.ikea.order.component.RawOrderViewComponent;
import org.menesty.ikea.util.APIRequest;
import org.menesty.ikea.util.HttpServiceUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 10/1/15.
 * 23:04.
 */
class ExportStep1 extends BaseWizardStep<IkeaSiteExportDialog.IkeaSiteExportInfo> {
    private ListView<ProfileModel> listView;
    private ListEditField<IkeaShop, IkeaShop> ikeaShopView;
    private ListViewField usersField;
    private FormPane formPane;
    private CheckBox splitGroup;

    public ExportStep1() {
        getStyleClass().clear();

        formPane = new FormPane();
        listView = new ListView<>();
        listView.setCellFactory(CheckBoxListCell.forListView(ProfileModel::checkedProperty, new StringConverter<ProfileModel>() {
            @Override
            public String toString(ProfileModel object) {
                return object != null ? object.getProfile().getFirstName() + " " + object.getProfile().getLastName() : "";
            }

            @Override
            public ProfileModel fromString(String string) {
                return null;
            }
        }));
        listView.setMaxHeight(150);


        formPane.add(new WrapField<ListView<ProfileModel>>(I18n.UA.getString(I18nKeys.CLIENTS), listView) {
            @Override
            public boolean isValid() {
                return listView.getItems().stream().filter(ProfileModel::isChecked).findFirst().isPresent();
            }

            @Override
            public void reset() {
                node.getItems().forEach(profileModel -> profileModel.setChecked(false));
            }
        });

        formPane.add(usersField = new ListViewField(I18n.UA.getString(I18nKeys.USERS), false));
        formPane.add(ikeaShopView = new ListEditField<>(I18n.UA.getString(I18nKeys.SHOPS), false));
        ikeaShopView.setItemLabel(IkeaShop::getName);
        usersField.setMaxHeight(200);
        ikeaShopView.setMaxHeight(200);

        formPane.add(new WrapField<CheckBox>(I18n.UA.getString(I18nKeys.IKEA_SITE_EXPORT_SPLIT_GROUP), splitGroup = new CheckBox()) {
            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public void reset() {
                node.setSelected(true);
            }
        });

        CachedAsyncDataProvider<IkeaShop> ikeaListCachedAsyncDataProvider = new CachedAsyncDataProvider<>(new AbstractAsyncService<List<IkeaShop>>() {
            @Override
            protected Task<List<IkeaShop>> createTask() {
                return new Task<List<IkeaShop>>() {
                    @Override
                    protected List<IkeaShop> call() throws Exception {
                        APIRequest apiRequest = HttpServiceUtil.get("/ikea-shops");

                        return apiRequest.getList(new TypeReference<List<IkeaShop>>() {
                        });
                    }
                };
            }
        });
        ikeaShopView.getLoadingPane().bindTask(ikeaListCachedAsyncDataProvider.getService());
        ikeaListCachedAsyncDataProvider.getData(new DataProvider.CallBack<IkeaShop>() {
            @Override
            public void onData(List<IkeaShop> data) {
                ikeaShopView.setChoiceList(data);
            }

            @Override
            public void onError() {

            }
        });


        setContent(formPane);
    }

    @Override
    public boolean isValid() {
        return formPane.isValid();
    }

    @Override
    public boolean canSkip(IkeaSiteExportDialog.IkeaSiteExportInfo param) {
        return false;
    }

    @Override
    public void collect(IkeaSiteExportDialog.IkeaSiteExportInfo param) {
        List<Profile> checked = listView.getItems().stream()
                .filter(ProfileModel::isChecked)
                .map(ProfileModel::getProfile).collect(Collectors.toList());

        List<IkeaOrderItem> items = param.getIkeaClientOrderItemDtos().stream()
                .filter(clientOrderItemDto -> checked.contains(clientOrderItemDto.getProfile()))
                .map(IkeaClientOrderItemDto::getIkeaOrderItems)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        items = RawOrderViewComponent.groupItem(items);

        param.setItemsToExport(items);
        param.setUsers(usersField.getValues());
        param.setShops(ikeaShopView.getValues());
        param.setSplitGroup(splitGroup.isSelected());
        param.setProfiles(checked);
    }

    @Override
    public void onActive(IkeaSiteExportDialog.IkeaSiteExportInfo param) {
        List<ProfileModel> profileModels = param.getIkeaClientOrderItemDtos().stream()
                .map(IkeaClientOrderItemDto::getProfile)
                .distinct()
                .map(profile -> {
                    ProfileModel profileModel = new ProfileModel(profile);

                    profileModel.setChecked(param.getProfiles().contains(profile));

                    return profileModel;
                })
                .collect(Collectors.toList());

        listView.getItems().clear();
        listView.getItems().addAll(profileModels);

        splitGroup.setSelected(param.isSplitGroup());
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

        public void setChecked(boolean checked) {
            checkedProperty().set(checked);
        }
    }
}
