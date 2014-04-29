package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.menesty.ikea.domain.WarehouseItemDto;
import org.menesty.ikea.util.ColumnUtil;

import java.util.List;

/**
 * Created by Menesty on 4/29/14.
 */
public class ParagonManageDialog extends BaseDialog {
    private TableView<WarehouseItemDto> tableView;

    public ParagonManageDialog() {
        setMinWidth(530);

        tableView = new TableView<>();

        {
            TableColumn<WarehouseItemDto, String> column = new TableColumn<>("Product Number");
            column.setMinWidth(150);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, String>column("productNumber"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDto, String> column = new TableColumn<>("Short Number");
            column.setMinWidth(250);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, String>column("shortName"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDto, Double> column = new TableColumn<>("Price");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, Double>column("price"));
            tableView.getColumns().add(column);
        }

        {
            TableColumn<WarehouseItemDto, Double> column = new TableColumn<>("Count");
            column.setMinWidth(80);
            column.setCellValueFactory(ColumnUtil.<WarehouseItemDto, Double>column("count"));
            tableView.getColumns().add(column);
        }

        addRow(tableView, bottomBar);
    }

    public void show(List<WarehouseItemDto> items) {
        tableView.getItems().clear();
        tableView.getItems().addAll(items);
    }
}
