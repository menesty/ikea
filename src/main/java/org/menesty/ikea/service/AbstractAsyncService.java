package org.menesty.ikea.service;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * Created by Menesty on 12/21/13.
 */
public abstract class AbstractAsyncService<T> extends Service<T> {
    public static interface SucceededListener<T> {
        void onSucceeded(T value);
    }

    @SuppressWarnings("unchecked")
    public void setOnSucceededListener(final SucceededListener<T> listener) {
        if (listener != null)
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(final WorkerStateEvent workerStateEvent) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener.onSucceeded((T) workerStateEvent.getSource().getValue());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

    }
}