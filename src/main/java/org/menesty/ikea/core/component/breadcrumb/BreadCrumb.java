package org.menesty.ikea.core.component.breadcrumb;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Created by Menesty on
 * 10/10/14.
 * 18:14.
 */
public class BreadCrumb {
    interface OnBreadCrumbChangeListener {

        void onChange(Deque<BreadCrumbItem> crumbItems);
    }

    private Deque<BreadCrumbItem> breadCrumbItems = new ArrayDeque<>();

    private OnBreadCrumbChangeListener changeListener;

    public void add(BreadCrumbItem item) {
        breadCrumbItems.addLast(item);

        triggerChange();
    }

    private void triggerChange() {
        if (changeListener != null) {
            changeListener.onChange(new ArrayDeque<>(breadCrumbItems));
        }
    }

    public void setItems(List<BreadCrumbItem> items) {
        this.breadCrumbItems.clear();

        if (!items.isEmpty()) {
            this.breadCrumbItems.addFirst(new BreadCrumbItem("Home"));
            this.breadCrumbItems.addAll(items);
        }

        triggerChange();
    }

    public void setChangeListener(OnBreadCrumbChangeListener changeListener) {
        this.changeListener = changeListener;
    }
}


