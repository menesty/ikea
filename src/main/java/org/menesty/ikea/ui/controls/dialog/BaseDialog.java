package org.menesty.ikea.ui.controls.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.menesty.ikea.IkeaApplication;
import org.menesty.ikea.ui.controls.pane.LoadingPane;

/**
 * User: Menesty
 * Date: 10/14/13
 * Time: 1:11 PM
 */
public class BaseDialog extends StackPane {

    protected final HBox bottomBar;

    protected Button okBtn;

    protected Button cancelBtn;

    private boolean allowAutoHide = true;

    protected LoadingPane loadingPane;

    private VBox content;

    public BaseDialog() {
        super();
        setId("ProxyDialog");
        setMaxSize(430, USE_PREF_SIZE);
        // block mouse clicks
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent t) {
                t.consume();
            }
        });

        content = new VBox();
        content.setSpacing(10);
        loadingPane = new LoadingPane();

        getChildren().addAll(content, loadingPane);

        cancelBtn = new Button("Cancel");
        cancelBtn.setId("cancelButton");
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onCancel();
            }
        });
        cancelBtn.setMinWidth(74);
        cancelBtn.setPrefWidth(74);
        HBox.setMargin(cancelBtn, new Insets(0, 8, 0, 0));
        okBtn = new Button("Ok");
        okBtn.setId("saveButton");
        okBtn.setDefaultButton(true);

        okBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                onOk();

            }
        });
        okBtn.setMinWidth(74);
        okBtn.setPrefWidth(74);

        bottomBar = new HBox(0);
        bottomBar.setAlignment(Pos.BASELINE_RIGHT);
        bottomBar.getChildren().addAll(cancelBtn, okBtn);
        VBox.setMargin(bottomBar, new Insets(20, 5, 5, 5));
    }

    public Stage getStage(){
        return IkeaApplication.get().getStage();
    }

    public void addRow(Node... rows) {
        content.getChildren().addAll(rows);
    }

    public void addRow(int index, Node row) {
        content.getChildren().add(index, row);
    }

    protected boolean removeRow(Node row) {
        return content.getChildren().remove(row);
    }

    protected int rowCount() {
        return content.getChildren().size();
    }

    protected boolean containRaw(Node row) {
        return content.getChildren().contains(row);
    }

    public void onCancel() {

    }

    public void onOk() {

    }

    public void onShow() {

    }

    public boolean isAllowAutoHide() {
        return allowAutoHide;
    }

    public void setAllowAutoHide(boolean allowAutoHide) {
        this.allowAutoHide = allowAutoHide;
    }

    protected Label createTitle(String text) {
        Label title = new Label(text);
        title.setId("title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        return title;
    }
}

