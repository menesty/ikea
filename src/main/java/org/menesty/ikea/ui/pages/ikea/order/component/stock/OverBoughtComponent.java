package org.menesty.ikea.ui.pages.ikea.order.component.stock;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.menesty.ikea.core.component.DialogSupport;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.Profile;
import org.menesty.ikea.lib.domain.ikea.logistic.stock.StockItemDto;
import org.menesty.ikea.lib.dto.ikea.order.NewOrderItemInfo;
import org.menesty.ikea.ui.controls.TotalStatusPanel;
import org.menesty.ikea.ui.controls.dialog.Dialog;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.table.EntityCheckBoxHolder;
import org.menesty.ikea.ui.controls.table.component.BaseTableView;
import org.menesty.ikea.ui.controls.table.component.EntityCheckBoxTableColumn;
import org.menesty.ikea.ui.pages.DialogCallback;
import org.menesty.ikea.ui.pages.EntityDialogCallback;
import org.menesty.ikea.ui.pages.ikea.order.component.service.AddComboPartService;
import org.menesty.ikea.ui.pages.ikea.order.component.service.AddToOrderItemService;
import org.menesty.ikea.ui.pages.ikea.order.component.service.ReturnItemService;
import org.menesty.ikea.ui.pages.ikea.order.dialog.ChoiceCountDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.combo.OrderViewComboDialog;
import org.menesty.ikea.ui.pages.ikea.order.dialog.returnitem.ReturnItemDialog;
import org.menesty.ikea.ui.table.ArtNumberCell;
import org.menesty.ikea.util.ColumnUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 6/14/16.
 * 15:53.
 */
public abstract class OverBoughtComponent extends BorderPane {
  private final TotalStatusPanel overBoughTotalStatusPanel;
  private final TextField artNumberField;
  private BaseTableView<EntityCheckBoxHolder<StockItemDto>> overBoughTableView;
  private OrderViewComboDialog orderViewComboDialog;
  private ReturnItemDialog returnItemDialog;
  private  Long ikeaProcessOrderId;

  private List<EntityCheckBoxHolder<StockItemDto>> items;

  public OverBoughtComponent(final DialogSupport dialogSupport,
                             final AddToOrderItemService addToOrderItemService,
                             final AddComboPartService addComboPartService,
                             final ReturnItemService returnItemService) {

    overBoughTableView = new BaseTableView<>();
    overBoughTableView.setEditable(true);
    overBoughTableView.setRowRenderListener((row, newValue) -> {
      row.setContextMenu(null);

      if (newValue != null) {
        ContextMenu contextMenu = new ContextMenu();

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_TO_ORDER));
          menuItem.setOnAction(event -> {
            ChoiceCountDialog choiceCountDialog = new ChoiceCountDialog(dialogSupport.getStage());
            choiceCountDialog.maxValue(newValue.getItem().getArtNumber(), newValue.getItem().getInvoiceItemId(), getProfiles(), newValue.getItem().getCount(), new EntityDialogCallback<NewOrderItemInfo>() {
              @Override
              public void onSave(NewOrderItemInfo newOrderItemInfo, Object... params) {
                dialogSupport.hidePopupDialog();

                addToOrderItemService.setChoiceCountResult(ikeaProcessOrderId, Collections.singletonList(newOrderItemInfo));
                addToOrderItemService.restart();
              }

              @Override
              public void onCancel() {
                dialogSupport.hidePopupDialog();
              }
            });

            dialogSupport.showPopupDialog(choiceCountDialog);
          });

          contextMenu.getItems().add(menuItem);
        }
        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.RETURN_BACK));
          menuItem.setOnAction(event ->
              {
                ReturnItemDialog returnItemDialog = getReturnItemDialog(dialogSupport);
                returnItemDialog.setEntity(newValue.getItem(), new EntityDialogCallback<BigDecimal>() {
                  @Override
                  public void onSave(BigDecimal count, Object... params) {
                    returnItemService.setData(ikeaProcessOrderId, newValue.getItem().getInvoiceItemId(), count);
                    returnItemService.restart();
                    dialogSupport.hidePopupDialog();
                  }

                  @Override
                  public void onCancel() {
                    dialogSupport.hidePopupDialog();
                  }
                });

                dialogSupport.showPopupDialog(returnItemDialog);
              }
          );
          contextMenu.getItems().add(menuItem);
        }

        {
          MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_TO_COMBO));
          menuItem.setOnAction(event -> {
            OrderViewComboDialog dialog = getOrderViewComboDialog(dialogSupport.getStage());

            dialog.loadCombos(ikeaProcessOrderId, newValue.getItem().getArtNumber(), new EntityDialogCallback<OrderViewComboDialog.ComboSelectResult>() {
              @Override
              public void onSave(OrderViewComboDialog.ComboSelectResult comboSelectResult, Object... params) {
                addComboPartService.setData(comboSelectResult);
                addComboPartService.restart();
                dialogSupport.hidePopupDialog();
              }

              @Override
              public void onCancel() {
                dialogSupport.hidePopupDialog();
              }
            });

            dialogSupport.showPopupDialog(dialog);

          });

          contextMenu.getItems().add(menuItem);
        }

        row.setContextMenu(contextMenu);
      }
    });

    {
      TableColumn<EntityCheckBoxHolder<StockItemDto>, Boolean> checked = new EntityCheckBoxTableColumn<>();
      checked.setCellValueFactory(new PropertyValueFactory<>("checked"));
      overBoughTableView.getColumns().add(checked);
    }

    {
      TableColumn<EntityCheckBoxHolder<StockItemDto>, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.ART_NUMBER));

      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("item.artNumber"));
      column.setCellFactory(ArtNumberCell::new);
      overBoughTableView.getColumns().add(column);
    }

    {
      TableColumn<EntityCheckBoxHolder<StockItemDto>, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.SHORT_NAME));

      column.setMinWidth(140);
      column.setCellValueFactory(ColumnUtil.column("item.shortName"));

      overBoughTableView.getColumns().add(column);
    }

    {
      TableColumn<EntityCheckBoxHolder<StockItemDto>, String> column = new TableColumn<>(I18n.UA.getString(I18nKeys.COUNT));

      column.setMinWidth(60);
      column.setCellValueFactory(ColumnUtil.number("item.count"));

      overBoughTableView.getColumns().add(column);
    }

    ToolBar overBoughToolBar = new ToolBar();

    {
      SplitMenuButton button = new SplitMenuButton();
      button.setText("Actions");

      {
        MenuItem menuItem = new MenuItem(I18n.UA.getString(I18nKeys.ADD_CHECKED_TO_ORDER));
        menuItem.setOnAction(event -> {
          //TODO FIX ME SHOW DIALOG TO CHOOSE PROFILE
          Profile profile = getProfiles().get(0);

          List<NewOrderItemInfo> newOrderItemInfos = overBoughTableView.getItems().stream()
              .filter(EntityCheckBoxHolder::isChecked)
              .map(stockItemDtoEntityCheckBoxHolder -> {
                StockItemDto itemDto = stockItemDtoEntityCheckBoxHolder.getItem();

                return new NewOrderItemInfo(profile.getId(), itemDto.getInvoiceItemId(), itemDto.getCount());
              }).collect(Collectors.toList());

          if (!newOrderItemInfos.isEmpty()) {
            Dialog.confirm(dialogSupport, I18n.UA.getString(I18nKeys.WARNING), I18n.UA.getString(I18nKeys.ALL_CHECKED_ITEMS_WILL_ADD_TO_ORDER_WITH_FULL_COUNT), new DialogCallback() {
              @Override
              public void onCancel() {
                dialogSupport.hidePopupDialog();
              }

              @Override
              public void onYes() {
                addToOrderItemService.setChoiceCountResult(ikeaProcessOrderId, newOrderItemInfos);
                addToOrderItemService.restart();
              }
            });
          }
        });

        button.getItems().add(menuItem);

        overBoughToolBar.getItems().add(button);
      }
    }

    {
      artNumberField = new org.menesty.ikea.ui.controls.form.TextField();
      artNumberField.setDelay(1);
      artNumberField.setOnDelayAction(actionEvent -> filter());
      artNumberField.setPromptText("Product ID #");

      overBoughToolBar.getItems().add(artNumberField);
    }

    setTop(overBoughToolBar);
    setCenter(overBoughTableView);
    setBottom(overBoughTotalStatusPanel = new TotalStatusPanel());
  }

  private void filter() {
    String value = artNumberField.getText();
    List<EntityCheckBoxHolder<StockItemDto>> items;
    if (StringUtils.isBlank(value)) {
      items = this.items;
    } else {

      items = this.items.parallelStream()
          .filter(stockItemDtoEntityCheckBoxHolder -> stockItemDtoEntityCheckBoxHolder.getItem().getArtNumber().startsWith(value))
          .collect(Collectors.toList());
    }

    overBoughTableView.getItems().setAll(items);
  }

  private OrderViewComboDialog getOrderViewComboDialog(Stage stage) {
    if (orderViewComboDialog == null) {
      orderViewComboDialog = new OrderViewComboDialog(stage);
    }

    return orderViewComboDialog;
  }

  public abstract List<Profile> getProfiles();

  public List<EntityCheckBoxHolder<StockItemDto>> getItems() {
    return overBoughTableView.getItems();
  }

  public void setItems(List<EntityCheckBoxHolder<StockItemDto>> items) {
    overBoughTableView.getItems().setAll(this.items = items);

    BigDecimal overBoughTotal = items.stream()
        .map(stockItemDtoEntityCheckBoxHolder -> stockItemDtoEntityCheckBoxHolder.getItem().getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    overBoughTotalStatusPanel.setTotal(overBoughTotal);
  }

  public ReturnItemDialog getReturnItemDialog(DialogSupport dialogSupport) {
    if (returnItemDialog == null) {
      returnItemDialog = new ReturnItemDialog(dialogSupport.getStage());
    }

    return returnItemDialog;
  }

  public Long getIkeaProcessOrderId() {
    return ikeaProcessOrderId;
  }

  public void setIkeaProcessOrderId(Long ikeaProcessOrderId) {
    this.ikeaProcessOrderId = ikeaProcessOrderId;
  }
}
