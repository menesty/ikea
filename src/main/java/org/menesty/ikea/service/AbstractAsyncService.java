package org.menesty.ikea.service;

import javafx.application.Platform;
import javafx.concurrent.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Menesty on 12/21/13.
 */
public abstract class AbstractAsyncService<T> extends Service<T> implements AsyncService<T> {
  private List<ErrorListener> errorListeners;

  public AbstractAsyncService() {
    errorListeners = new ArrayList<>();

    setOnFailed(event -> {
          ServiceFacade.getErrorConsole().add(event.getSource().getException());
          if (!errorListeners.isEmpty()) {
            errorListeners.stream().forEach(ErrorListener::onError);
          }
        }
    );

  }

  @SuppressWarnings("unchecked")
  public void setOnSucceededListener(final SucceededListener<T> listener) {
    if (listener != null)
      setOnSucceeded(workerStateEvent -> Platform.runLater(() -> {
        try {
          listener.onSucceeded((T) workerStateEvent.getSource().getValue());
        } catch (Exception e) {
          ServiceFacade.getErrorConsole().add(e);
        }
      }));


  }

  public void addOnErrorListener(final ErrorListener listener) {
    if (listener != null) {
      errorListeners.add(listener);
    }
  }

  public void restart(final SucceededListener<T> listener) {
    setOnSucceededListener(listener);
    restart();
  }


}