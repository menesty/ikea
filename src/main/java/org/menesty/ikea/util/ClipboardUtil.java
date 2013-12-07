package org.menesty.ikea.util;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Created by Menesty on 12/7/13.
 */
public class ClipboardUtil {
    public static void copy(String string) {
        final ClipboardContent content = new ClipboardContent();
        content.putString(string);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
