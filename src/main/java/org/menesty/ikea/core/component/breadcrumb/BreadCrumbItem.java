package org.menesty.ikea.core.component.breadcrumb;

import org.menesty.ikea.core.component.PageDescription;

/**
 * Created by Menesty on
 * 10/10/14.
 * 18:14.
 */
public class BreadCrumbItem {
    private final String name;

    private final PageDescription pageDescription;

    public BreadCrumbItem(String name) {
        this(name, null);
    }

    public BreadCrumbItem(String name, PageDescription pageDescription) {
        this.name = name;
        this.pageDescription = pageDescription;
    }


    public PageDescription getPageDescription() {
        return pageDescription;
    }

    public String getName() {
        return name;
    }

}
