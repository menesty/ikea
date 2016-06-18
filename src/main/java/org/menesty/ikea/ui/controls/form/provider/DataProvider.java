package org.menesty.ikea.ui.controls.form.provider;

import org.menesty.ikea.ui.controls.form.ItemLabel;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Menesty on
 * 9/2/15.
 * 17:48.
 */
public class DataProvider<T> {
    private ItemLabel<T> itemLabel;

    public interface CallBack<T> {
        void onData(List<T> data);
        void onError();
    }

    private List<T> data;
    private Predicate<T> filterPredicate;

    public void getData(CallBack<T> callBack) {
        callBack.onData(data);
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void filter(CallBack<T> callBack, String query) {
        if (query == null || data == null) {
            callBack.onData(data);
            return;
        }

        if (filterPredicate == null) {
            callBack.onData(data.stream()
                    .filter(t -> itemLabel.label(t).startsWith(query))
                    .collect(Collectors.toList()));
            return;
        }

        callBack.onData(data.stream().filter(filterPredicate).collect(Collectors.toList()));
    }

    public void setFilterPredicate(Predicate<T> filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    public void setItemLabel(ItemLabel<T> itemLabel) {
        this.itemLabel = itemLabel;
    }
}
