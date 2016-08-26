package org.menesty.ikea.ui.pages.ikea.vk.dialog;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.menesty.ikea.domain.vk.VKMarketAlbum;
import org.menesty.ikea.i18n.I18n;
import org.menesty.ikea.i18n.I18nKeys;
import org.menesty.ikea.lib.domain.FileSourceType;
import org.menesty.ikea.service.AbstractAsyncService;
import org.menesty.ikea.ui.controls.dialog.EntityDialog;
import org.menesty.ikea.ui.controls.form.TextField;
import org.menesty.ikea.ui.controls.form.validation.MaxStringLength;
import org.menesty.ikea.ui.pages.ikea.vk.VKPhotoGallery;
import org.menesty.ikea.util.FileChooserUtil;
import org.menesty.ikea.util.HttpServiceUtil;
import org.menesty.ikea.util.vk.VKMarketAPIRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Menesty on
 * 7/14/16.
 * 16:03.
 */
public class VKAlbumCreateDialog extends EntityDialog<VKMarketAlbum> {
  private UploadPhotoService uploadPhotoService;
  private VKPhotoGallery vkPhotoGallery;

  public VKAlbumCreateDialog(Stage stage) {
    super(stage);

    setTitle(I18n.UA.getString(I18nKeys.VK_ALBUM));
    addRow(getEntityForm(), bottomBar);
    setMaxSize(600, USE_PREF_SIZE);

    uploadPhotoService = new UploadPhotoService();


    loadingPane.bindTask(uploadPhotoService);

    vkPhotoGallery = new VKPhotoGallery();
  }

  @Override
  protected void onSave(final VKMarketAlbum currentEntity) {
    AlbumForm form = (AlbumForm) getEntityForm();
    if (form.isChooseFile()) {
      uploadPhotoService.setFile(form.getChooseFile());
      uploadPhotoService.setOnSucceededListener(value -> {
        currentEntity.setPhotoId(value);

        super.onSave(currentEntity);
      });

      uploadPhotoService.restart();
    } else {
      super.onSave(currentEntity);
    }
  }

  @Override
  protected EntityForm<VKMarketAlbum> createForm() {
    return new AlbumForm();
  }

  class AlbumForm extends EntityForm<VKMarketAlbum> {
    private TextField nameField;
    private VkImagePreview vkImagePreview;

    public AlbumForm() {
      add(nameField = new TextField(null, I18n.UA.getString(I18nKeys.NAME)));
      nameField.setAllowBlank(false);
      nameField.addValidationRule(new MaxStringLength(128));

      addRow(vkImagePreview = new VkImagePreview(), 2);
    }

    @Override
    protected VKMarketAlbum collect(VKMarketAlbum entity) {
      entity.setTitle(nameField.getText());

      return entity;
    }

    @Override
    protected void populate(VKMarketAlbum entity) {
      nameField.setText(entity.getTitle());

      if (entity.getPhoto() != null) {
        entity.setPhotoId(entity.getPhoto().getId());
        vkImagePreview.setUrl(entity.getPhoto().getPhoto807());
      }
    }

    @Override
    public void reset() {
      super.reset();
      vkImagePreview.reset();
    }

    public File getChooseFile() {
      return vkImagePreview.getFileName();
    }

    public boolean isChooseFile() {
      return vkImagePreview.isChooseFile();
    }
  }

  class VkImagePreview extends VBox {
    private ImageView imageView;
    private Label fileNameLabel;
    private File file;
    private Label imageSizeLabel;

    public VkImagePreview() {
      setSpacing(2);

      ScrollPane scrollPane = new ScrollPane();
      scrollPane.setMaxHeight(300);
      scrollPane.setPrefHeight(300);

      imageView = new ImageView();

      scrollPane.setContent(imageView);

      HBox fileBox = new HBox();
      fileBox.setSpacing(3);

      Region space = new Region();
      HBox.setHgrow(space, Priority.ALWAYS);

      Button browse = new Button(I18n.UA.getString(I18nKeys.BROWSE_PHOTO));
      browse.setOnAction(event -> {
        FileChooser fileChooser = FileChooserUtil.getByType(FileSourceType.IMG);
        File file = fileChooser.showOpenDialog(getStage());

        if (file != null) {
          try {
            Image image = new Image(new FileInputStream(file));
            imageView.setImage(image);

            if (image.getWidth() < 1280 || image.getHeight() < 720) {
              imageSizeLabel.setText("Min size 1280x720px, current :" + (int) image.getWidth() + "x" + (int) image.getHeight());
              imageSizeLabel.getStyleClass().clear();
              imageSizeLabel.getStyleClass().add("text-red");
            } else {
              imageSizeLabel.getStyleClass().clear();
              imageSizeLabel.setText((int) image.getWidth() + "x" + (int) image.getHeight());
            }


            fileNameLabel.setText(file.getName());

            VkImagePreview.this.file = file;
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
        }

      });

      Button vkGallery = new Button("Gallery");
      vkGallery.setOnAction(event -> {
        vkPhotoGallery.show(data -> {
          System.out.println("Items");
        });
      });

      fileBox.getChildren().addAll(fileNameLabel = new Label(), space, browse, vkGallery);

      getChildren().addAll(imageSizeLabel = new Label(), scrollPane, fileBox);
    }

    public void reset() {
      fileNameLabel.setText(null);
      imageView.setImage(null);
      file = null;
    }

    public boolean isChooseFile() {
      return file != null;
    }

    public File getFileName() {
      return file;
    }

    public void setUrl(String photoUrl) {
      Image image = new Image(photoUrl, true);
      loadingPane.show();
      image.progressProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue.intValue() == 1) {
          loadingPane.hide();
        }
      });
      imageView.setImage(image);

    }

  }
}

class UploadPhotoService extends AbstractAsyncService<Long> {
  private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<Long> createTask() {
    File _file = fileProperty.get();

    return new Task<Long>() {
      @Override
      protected Long call() throws Exception {
        VKMarketAPIRequest request = HttpServiceUtil.getVKMarketAPI();

        assert request != null;

        return request.uploadPhotoAlbum(_file);
      }
    };
  }

  public void setFile(File file) {
    fileProperty.set(file);
  }
}
