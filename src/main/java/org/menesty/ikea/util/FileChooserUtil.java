package org.menesty.ikea.util;

import javafx.stage.FileChooser;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.service.ServiceFacade;

import java.io.File;

public class FileChooserUtil {

    public static FileChooser getEpp() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Epp location");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Epp file (*.epp)", "*.epp"));

        String def = ServiceFacade.getApplicationPreference().getFileChooseDefaultDir();

        if (StringUtils.isNotBlank(def) && new File(def).exists())
            fileChooser.setInitialDirectory(new File(def));

        return fileChooser;
    }

    public static void setDefaultDir(File file) {
        ServiceFacade.getApplicationPreference().setFileChooseDefaultDir(file.getParentFile().getAbsolutePath());
    }
}

