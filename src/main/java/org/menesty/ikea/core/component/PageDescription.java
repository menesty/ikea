package org.menesty.ikea.core.component;

import javafx.scene.Node;
import org.menesty.ikea.ui.pages.BasePage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Menesty on
 * 10/11/14.
 * 11:31.
 */
public class PageDescription {
    private final String name;

    private final Node icon;

    private final Class<? extends BasePage> page;

    private List<PageDescription> subPage = new ArrayList<>();

    private boolean allowRefresh = true;

    public PageDescription(String name, Class<? extends BasePage> page, boolean allowRefresh) {
        this(name, null, page);
        this.allowRefresh = allowRefresh;
    }

    public PageDescription(String name, Node icon, Class<? extends BasePage> page) {
        this.name = name;
        this.icon = icon;
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public Node getIcon() {
        return icon;
    }

    public Class<? extends BasePage> getPageClass() {
        return page;
    }

    public List<PageDescription> getSubPage() {
        return subPage;
    }

    public void addPage(PageDescription pageDescription) {
        subPage.add(pageDescription);
    }

    public boolean isAllowRefresh() {
        return allowRefresh;
    }
}
