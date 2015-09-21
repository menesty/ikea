package org.menesty.ikea.util;

import javafx.scene.control.Tooltip;

/**
 * Created by Menesty on
 * 9/11/15.
 * 20:07.
 */
public class ToolTipUtil {
    public static Tooltip create(String tooltip) {
        return new Tooltip(tooltip);
    }
}
