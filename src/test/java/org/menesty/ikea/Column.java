package org.menesty.ikea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 8/7/14.
 * 8:21.
 */
class Column {
    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private String name;
    private float width;
    private Margin margin;
    private Alignment alignment = Alignment.CENTER;
    private Alignment contentAlignment = Alignment.RIGHT;

    private List<Column> columns = new ArrayList<>();

    public Column(String name, float width) {
        this.name = name;
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public Margin getMargin() {
        return margin;
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Column setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Column addColumn(Column column) {
        columns.add(column);
        return this;
    }
}
