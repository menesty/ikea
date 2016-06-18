package org.menesty.ikea.ui.controls.search;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.logistic.warehouse.WarehouseProfileDto;
import org.menesty.ikea.ui.controls.form.ComboBoxField;
import org.menesty.ikea.ui.controls.form.TextField;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WarehouseSearchBar extends ToolBar {
  private ComboBoxField<WarehouseProfileDto> profileComboBoxField;
  private TextField artNumberField;
  private Map<Long, WarehouseProfileDto> profilesMap;

  public WarehouseSearchBar() {

    profileComboBoxField = new ComboBoxField<>(null);
    profileComboBoxField.setItemLabel(WarehouseProfileDto::getName);
    profileComboBoxField.selectedItemProperty().addListener((observable, oldValue, newValue) -> applyFilter());


    artNumberField = new TextField();
    artNumberField.setDelay(1);
    artNumberField.setOnDelayAction(actionEvent -> applyFilter());
    artNumberField.setPromptText("Product ID #");

    getItems().addAll(artNumberField, profileComboBoxField);

    {
      Button reset = new Button(null, ImageFactory.createClear16Icon());
      reset.setOnAction(event -> {
        profileComboBoxField.setValue(null);
        artNumberField.setText(null);
        applyFilter();
      });
      getItems().add(reset);
    }
  }

  private WarehouseItemSearchData collectData() {
    WarehouseItemSearchData data = new WarehouseItemSearchData();
    data.setProfileId(Optional.ofNullable(profileComboBoxField.getValue()).isPresent() ? profileComboBoxField.getValue().getId() : null);
    data.setArtNumber(StringUtils.isNotBlank(artNumberField.getText()) ? artNumberField.getText() : null);
    return data;
  }

  private void applyFilter() {
    onSearch(collectData());
  }

  public void onSearch(WarehouseItemSearchData data) {

  }

  public void setProfiles(List<WarehouseProfileDto> profiles) {
    profileComboBoxField.setItems(profiles);

    profilesMap = profiles.stream()
        .collect(Collectors.toMap(WarehouseProfileDto::getId, Function.<WarehouseProfileDto>identity()));
  }

  public String getProfileById(Long profileId) {
    Optional<WarehouseProfileDto> item = Optional.ofNullable(profilesMap.get(profileId));
    return item.isPresent() ? item.get().getName() : "";
  }
}
