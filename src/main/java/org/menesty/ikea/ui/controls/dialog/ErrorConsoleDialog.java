package org.menesty.ikea.ui.controls.dialog;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.menesty.ikea.dto.ErrorItem;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.service.ServiceFacade;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/6/15.
 * 18:36.
 */
public class ErrorConsoleDialog extends BaseDialog {
    private TableView<ErrorItem> tableView;

    public ErrorConsoleDialog(Stage stage) {
        super(stage);
        setTitle("Errors");
        okBtn.setText("Ok");
        setMinWidth(700);

        addRow(tableView = new TableView<>(), bottomBar);

        {
            TableColumn<ErrorItem, String> column = new TableColumn<>("Error");
            column.setCellValueFactory(new PropertyValueFactory<>("message"));
            column.setMinWidth(200);
            tableView.getColumns().add(column);
        }

        {
            TableColumn<ErrorItem, String> column = new TableColumn<>("Stacktrace");
            column.setCellValueFactory(new PropertyValueFactory<>("details"));
            tableView.getColumns().add(column);
        }

        Button button = new Button(I18n.UA.getString(I18nKeys.CLEAR), ImageFactory.createClear16Icon());
        button.setOnAction(event -> {
            tableView.getItems().clear();
            ServiceFacade.getErrorConsole().clear();
        });

        bottomBar.getChildren().add(0, button);
    }

    @Override
    public void onShow() {
        List<Throwable> items = ServiceFacade.getErrorConsole().getItems();
        List<ErrorItem> transformItems = items.stream().map(ErrorItem::new).collect(Collectors.toList());

        tableView.setItems(FXCollections.observableArrayList(transformItems));
    }
}
