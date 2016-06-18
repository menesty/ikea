package org.menesty.ikea.ui.controls.form;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.factory.ImageFactory;
import org.menesty.ikea.lib.domain.FileSourceType;
import org.menesty.ikea.util.FileChooserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/1/15.
 * 18:53.
 */
public class FileListField extends VBox implements Field {
    private String label;
    private ListView<File> listView;
    private FileSourceType fileType;
    private boolean allowBlank = true;

    public FileListField(String label, Stage stage) {
        this.label = label;

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 0, 5, 0));
        hBox.setSpacing(5);

        {
            Button button = new Button(null, ImageFactory.createAdd16Icon());
            button.setOnAction(actionEvent -> {

                FileChooser fileChooser = FileChooserUtil.getByType(fileType);

                List<File> selectedFile = fileChooser.showOpenMultipleDialog(stage);

                if (selectedFile != null && selectedFile.size() > 0) {
                    selectedFile = selectedFile.stream()
                            .filter(file -> !listView.getItems().contains(file))
                            .collect(Collectors.toList());
                    listView.getItems().addAll(selectedFile);
                }
            });
            hBox.getChildren().add(button);
        }

        {
            Button button = new Button(null, ImageFactory.createDelete16Icon());
            button.setOnAction(actionEvent -> {
                File item = listView.getSelectionModel().getSelectedItem();

                if (item != null) {
                    listView.getItems().remove(item);
                }
            });
            hBox.getChildren().add(button);
        }

        listView = new ListView<>();
        listView.setCellFactory(param -> new ListCell<File>() {
            @Override
            protected void updateItem(File file, boolean bln) {
                super.updateItem(file, bln);

                if (file != null) {
                    setText(file.getAbsolutePath());
                }
            }

        });

        getChildren().addAll(listView, hBox);
    }

    public boolean isAllowBlank() {
        return allowBlank;
    }

    public void setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
    }

    public FileSourceType getFileType() {
        return fileType;
    }

    public void setFileType(FileSourceType fileType) {
        this.fileType = fileType;
    }

    @Override
    public boolean isValid() {
        boolean result = true;
        getStyleClass().removeAll("validation-succeed", "validation-error");

        if (!allowBlank)
            setValid(result = listView.getItems().size() != 0);

        return result;
    }

    public void setValid(boolean valid) {
        listView.getStyleClass().removeAll("validation-succeed", "validation-error");
        listView.getStyleClass().remove("gray-border");

        if (valid)
            listView.getStyleClass().add("validation-succeed");
        else
            listView.getStyleClass().add("validation-error");

    }

    @Override
    public void reset() {

    }

    @Override
    public String getLabel() {
        return label;
    }

    public List<File> getValues() {
        return new ArrayList<>(listView.getItems());
    }

    public void setValues(List<File> files) {
        listView.getItems().clear();

        if (files == null || files.isEmpty()) {
            return;
        }
        listView.getItems().addAll(files);
    }

}
