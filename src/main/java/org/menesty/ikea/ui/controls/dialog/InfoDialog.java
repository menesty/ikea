package org.menesty.ikea.ui.controls.dialog;

import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Created by Menesty on
 * 10/23/14.
 * 9:19.
 */
public class InfoDialog extends BaseDialog {
  private Label buildVersion;
  private Label buildTime;
  private Label defaultFileEncoding;
  private Label defaultCharsetEncoding;

  public InfoDialog(Stage stage) {
    super(stage);
    setTitle("Info");

    addRow(buildVersion = new Label(), buildTime = new Label(), defaultFileEncoding = new Label(), defaultCharsetEncoding = new Label(), bottomBar);
  }


  @Override
  public void onShow() {
    Properties properties = new Properties();

    try (InputStream version = getClass().getResourceAsStream("/version.properties")) {
      properties.load(version);

      buildTime.setText("Build date : " + properties.getProperty("build.date"));
      buildVersion.setText("Version : " + properties.getProperty("version"));

      properties.clear();
    } catch (IOException e) {
      e.printStackTrace();
    }

    defaultFileEncoding.setText("File encoding : " + System.getProperty("file.encoding"));
    defaultCharsetEncoding.setText("Charset encoding : " + Charset.defaultCharset().toString());
  }
}
