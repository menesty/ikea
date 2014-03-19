package org.menesty.ikea.ui.pages;

public enum Pages {
    ORDERS("Orders");

    private final String title;

    Pages(String title) {
        this.title = title;

    }
    public String getTitle(){
        return title;
    }
}
