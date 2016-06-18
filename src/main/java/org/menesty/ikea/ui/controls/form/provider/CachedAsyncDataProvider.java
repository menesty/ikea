package org.menesty.ikea.ui.controls.form.provider;

import org.menesty.ikea.service.AbstractAsyncService;

import java.util.List;

/**
 * Created by Menesty on
 * 9/15/15.
 * 09:08.
 */
public class CachedAsyncDataProvider<T> extends DataProvider<T> {
    private boolean loaded;
    private final AbstractAsyncService<List<T>> asyncService;

    public CachedAsyncDataProvider(AbstractAsyncService<List<T>> asyncService) {
        this.asyncService = asyncService;
    }

    @Override
    public void getData(CallBack<T> callBack) {
        if (loaded) {
            super.getData(callBack);
        } else {
            loaded = true;
            asyncService.setOnSucceededListener(callBack::onData);
            asyncService.addOnErrorListener(callBack::onError);
            asyncService.restart();
        }
    }

    public AbstractAsyncService<List<T>> getService(){
        return asyncService;
    }
}
