package org.menesty.ikea.ui.pages.ikea.order.export;

/**
 * Created by Menesty on
 * 10/1/15.
 * 23:27.
 */
public class ExportCategory {
    private final String id;
    private final String group;

    public ExportCategory(String group, String id) {
        this.group = group;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }
}
