package org.menesty.ikea.util;

import javafx.stage.FileChooser;
import org.apache.commons.lang.StringUtils;
import org.menesty.ikea.lib.domain.FileSourceType;
import org.menesty.ikea.service.ServiceFacade;

import javax.validation.constraints.NotNull;
import java.io.File;

public class FileChooserUtil {

    public static FileChooser getEpp() {
        return createFileChooser("Epp location", "Epp file (*.epp)", "*.epp");
    }

    private static FileChooser createFileChooser(String title, String filterName, String... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (filterName != null && filters != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, filters));
        }

        String def = ServiceFacade.getApplicationPreference().getFileChooseDefaultDir();

        if (StringUtils.isNotBlank(def) && new File(def).exists()) {
            fileChooser.setInitialDirectory(new File(def));
        }

        return fileChooser;
    }

    public static FileChooser getXls() {
        return createFileChooser("Xls location", "Xls file (*.xls)", "*.xls", "*.xlsx");
    }

    public static void setDefaultDir(File file) {
        ServiceFacade.getApplicationPreference().setFileChooseDefaultDir(file.getParentFile().getAbsolutePath());
    }

    public static FileChooser getPdf() {
        return createFileChooser("Invoice PDF location", "Pdf files (*.pdf)", "*.pdf");
    }

    public static FileChooser getByType(FileSourceType fileType) {
        if (FileSourceType.XLS == fileType) {
            return getXls();
        } else if (FileSourceType.PDF == fileType) {
            return getPdf();
        } else {
            return createFileChooser("File location", null);
        }

    }
}

