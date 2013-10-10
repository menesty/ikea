package org.menesty.ikea.ui.pages;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.menesty.ikea.ui.controls.BreadcrumbBar;

import java.util.HashMap;
import java.util.Map;

public class PageManager {
    private final BreadcrumbBar breadcrumbBar;
    private Map<String, BasePage> pages = new HashMap<>();
    private BasePage currentPage;
    private boolean changingPage;
    private Node currentPageView;
    private String currentPagePath;

    private final Pane pageArea;

    public PageManager(Pane pageArea, BreadcrumbBar breadcrumbBar) {
        this.breadcrumbBar = breadcrumbBar;
        this.pageArea = pageArea;
    }

    public void register(BasePage page) {
        if (page instanceof CategoryPage)
            for (BasePage subPage : ((CategoryPage) page).getSubPages())
                register(subPage);

        if (!pages.containsKey(page.getBreadCrumb()))
            pages.put(page.getBreadCrumb(), page);
    }

    public void goToPage(BasePage page) {
        goToPage(page, false, true);
    }

    public void goToPage(String breadcrumbPath) {
        goToPage(pages.get(breadcrumbPath), false, true);
    }

    /**
     * Take ensemble to the given page object, navigating there.
     *
     * @param page      Page object to show
     * @param force     When true reload page if page is current page
     * @param swapViews If view should be swapped to new page
     */
    private void goToPage(BasePage page, boolean force, boolean swapViews) {
        if (page == null) return;
        if (!force && page == currentPage) {
            return;
        }
        changingPage = true;
        if (swapViews) {
            Node view = page.createView();
            if (view == null) view = new Region(); // todo temp workaround
            // replace view in pageArea if new
            if (force || view != currentPageView) {
                /*for (Node child:pageArea.getChildren()){
                    if (child instanceof SamplePage.SamplePageView) {
                        ((SamplePage.SamplePageView)child).stop();
                    }
                }*/
                pageArea.getChildren().setAll(view);
                currentPageView = view;
            }
        }
        currentPage = page;
        currentPagePath = page.getBreadCrumb();
        // when in applet update location bar
        // update breadcrumb bar
        breadcrumbBar.setPath(currentPagePath);
        // done
        changingPage = false;
    }
}