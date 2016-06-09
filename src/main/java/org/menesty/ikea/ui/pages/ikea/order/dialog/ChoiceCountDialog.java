package org.menesty.ikea.ui.pages.ikea.order.dialog;

import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.dto.ikea.order.NewOrderItemInfo;
import org.menesty.ikea.ui.controls.dialog.BaseDialog;
import org.menesty.ikea.ui.controls.form.ComboBoxField;
import org.menesty.ikea.ui.controls.form.FormPane;
import org.menesty.ikea.ui.controls.form.LabelField;
import org.menesty.ikea.ui.controls.form.NumberTextField;
import org.menesty.ikea.ui.pages.EntityDialogCallback;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Menesty on
 * 11/2/15.
 * 06:08.
 */
public class ChoiceCountDialog extends BaseDialog {
    private NumberTextField countField;
    private ComboBoxField<Profile> profileComboBoxField;
    private EntityDialogCallback<NewOrderItemInfo> callback;
    private LabelField artNumberField;
    private FormPane formPane;
    private Long invoiceItemId;

    public ChoiceCountDialog(Stage stage) {
        super(stage);
        setTitle(I18n.UA.getString(I18nKeys.CHOICE_COUNT));

        formPane = new FormPane();

        formPane.add(artNumberField = new LabelField(I18n.UA.getString(I18nKeys.ART_NUMBER)));
        formPane.add(profileComboBoxField = new ComboBoxField<>(I18n.UA.getString(I18nKeys.CLIENT)));
        profileComboBoxField.setItemLabel(item -> item.getFirstName() + " " + item.getLastName());
        profileComboBoxField.setAllowBlank(false);

        formPane.add(countField = new NumberTextField(I18n.UA.getString(I18nKeys.COUNT), false));
        countField.setMinValue(BigDecimal.ZERO);

        addRow(formPane, bottomBar);
    }

    public void maxValue(String artNumber, Long invoiceItemId, List<Profile> profiles, BigDecimal maxValue, EntityDialogCallback<NewOrderItemInfo> callback) {
        profileComboBoxField.setItems(profiles);
        countField.setMaxValue(maxValue);
        countField.setNumber(maxValue);
        artNumberField.setText(artNumber);
        this.invoiceItemId = invoiceItemId;

        this.callback = callback;
    }

    @Override
    public void onCancel() {
        callback.onCancel();
    }

    @Override
    public void onOk() {
        if (formPane.isValid()) {
            callback.onSave(new NewOrderItemInfo( profileComboBoxField.getValue().getId(), invoiceItemId, countField.getNumber()));
        }
    }


}
