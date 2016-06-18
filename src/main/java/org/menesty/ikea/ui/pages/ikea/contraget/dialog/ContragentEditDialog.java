package org.menesty.ikea.ui.pages.ikea.contraget.dialog;

import javafx.stage.Stage;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.ikea.logistic.Contragent;
import org.menesty.ikea.ui.controls.dialog.EntityDialog;
import org.menesty.ikea.ui.controls.form.TextField;

/**
 * Created by Menesty on
 * 2/1/16.
 * 14:34.
 */
public class ContragentEditDialog extends EntityDialog<Contragent> {

  public ContragentEditDialog(Stage stage) {
    super(stage);
    setTitle(I18n.UA.getString(I18nKeys.CONTRAGENT));
    addRow(getEntityForm(), bottomBar);
  }

  @Override
  protected EntityForm<Contragent> createForm() {
    return new ContragentForm();
  }

  class ContragentForm extends EntityForm<Contragent> {
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField documentNumberField;
    private TextField regionField;
    private TextField addressField;
    private TextField postCodeFiled;

    public ContragentForm() {
      add(firstNameField = new TextField(null, I18n.UA.getString(I18nKeys.FIRST_NAME), false));
      add(lastNameField = new TextField(null, I18n.UA.getString(I18nKeys.LAST_NAME), false));
      add(documentNumberField = new TextField(null, I18n.UA.getString(I18nKeys.DOCUMENT_NUMBER), false));
      add(regionField = new TextField(null, I18n.UA.getString(I18nKeys.REGION), false));
      add(addressField = new TextField(null, I18n.UA.getString(I18nKeys.ADDRESS), false));
      add(postCodeFiled = new TextField(null, I18n.UA.getString(I18nKeys.POST_CODE), false));
    }

    @Override
    protected Contragent collect(Contragent entity) {
      entity.setFirstName(firstNameField.getText());
      entity.setLastName(lastNameField.getText());
      entity.setDocumentNumber(documentNumberField.getText());
      entity.setRegion(regionField.getText());
      entity.setAddress(addressField.getText());
      entity.setPostCode(postCodeFiled.getText());

      return entity;
    }

    @Override
    protected void populate(Contragent entity) {
      firstNameField.setText(entity.getFirstName());
      lastNameField.setText(entity.getLastName());
      documentNumberField.setText(entity.getDocumentNumber());
      regionField.setText(entity.getRegion());
      addressField.setText(entity.getAddress());
      postCodeFiled.setText(entity.getPostCode());

    }
  }
}
