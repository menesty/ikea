package org.menesty.ikea.core.component;

import org.menesty.ikea.core.component.breadcrumb.BreadCrumbItem;
import org.menesty.ikea.ui.pages.BasePage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Menesty on
 * 10/11/14.
 * 11:08.
 */
public class CategoryGroup {
    private final String name;

    private List<PageDescription> items = new ArrayList<>();

    public CategoryGroup(String name) {
        this.name = name;
    }

    public void add(PageDescription description) {
        items.add(description);
    }

    public String getName() {
        return name;
    }

    public List<PageDescription> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<BreadCrumbItem> getBreadCrumbChain(PageDescription pageDescription) {
        List<BreadCrumbItem> crumbItems = new ArrayList<>();
        recursive(crumbItems, items, pageDescription);

        if (!crumbItems.isEmpty()) {
            crumbItems.add(new BreadCrumbItem(name));
            Collections.reverse(crumbItems);

            return crumbItems;
        }

        return null;
    }

    private boolean recursive(List<BreadCrumbItem> breadCrumbItems, List<PageDescription> items, final PageDescription pageDescription) {
        for (PageDescription item : items) {

            if (item.equals(pageDescription)) {
                breadCrumbItems.add(new BreadCrumbItem(pageDescription.getName(), pageDescription));
                return true;
            }

            if (recursive(breadCrumbItems, item.getSubPage(), pageDescription)) {
                breadCrumbItems.add(new BreadCrumbItem(item.getName(), item));
                return true;
            }

        }

        return false;
    }

    public PageDescription getPageDescription(PageDescription parent, Class<? extends BasePage> subPage) {
        if (parent != null) {
            return recursiveSearch(items, parent, subPage);
        } else {
            return recursiveSearchPage(items, subPage);
        }
    }

    private PageDescription recursiveSearchPage(List<PageDescription> items, Class<? extends BasePage> subPage) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        for (PageDescription item : items) {
            if (item.getPageClass().equals(subPage)) {
                return item;
            } else {
                PageDescription result = recursiveSearchPage(item.getSubPage(), subPage);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private PageDescription recursiveSearch(List<PageDescription> items, PageDescription parent,
                                            Class<? extends BasePage> subPageClass) {
        for (PageDescription item : items) {
            if (item.equals(parent)) {
                for (PageDescription subPage : item.getSubPage()) {
                    if (subPage.getPageClass().equals(subPageClass)) {
                        return subPage;
                    }
                }
                throw new RuntimeException(String.format("Page with class %s not registered under parent page %s",
                        subPageClass.getName(), parent.getName()));
            }

            PageDescription result = recursiveSearch(item.getSubPage(), parent, subPageClass);

            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
