package org.menesty.ikea.ui.controls.form.provider;

import java.util.List;

/**
 * Created by Menesty on
 * 9/2/15.
 * 18:06.
 */
public class AsyncFilterDataProvider<T> extends DataProvider<T> {
    private final FilterAsyncService<List<T>> asyncService;

    public AsyncFilterDataProvider(FilterAsyncService<List<T>> asyncService) {
        this.asyncService = asyncService;
    }

    @Override
    public void filter(CallBack<T> callBack, String query) {
        asyncService.setFilterQuery(query);
        asyncService.setOnSucceededListener(callBack::onData);
        asyncService.addOnErrorListener(callBack::onError);
        asyncService.restart();
    }
}


