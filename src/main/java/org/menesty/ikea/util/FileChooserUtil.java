package org.menesty.ikea.util;

import javafx.stage.FileChooser;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.service.ServiceFacade;

import java.io.File;

public class FileChooserUtil {

    public static FileChooser getEpp() {
        return createFileChooser("Epp location", "Epp file (*.epp)", "*.epp");
    }

    private static FileChooser createFileChooser(String title, String filterName, String... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, filters));

        String def = ServiceFacade.getApplicationPreference().getFileChooseDefaultDir();

        if (StringUtils.isNotBlank(def) && new File(def).exists())
            fileChooser.setInitialDirectory(new File(def));

        return fileChooser;
    }

    public static FileChooser getXls() {
        return createFileChooser("Xls location", "Xls file (*.xls)", "*.xls", "*.xlsx");
    }

    public static void setDefaultDir(File file) {
        ServiceFacade.getApplicationPreference().setFileChooseDefaultDir(file.getParentFile().getAbsolutePath());
    }
}

