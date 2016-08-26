package org.menesty.ikea.ui.pages.ikea.vk.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.menesty.ikea.domain.vk.VKLoadListener;
import org.menesty.ikea.service.AbstractAsyncService;

import java.util.concurrent.Callable;

/**
 * Created by Menesty on
 * 7/14/16.
 * 23:02.
 */
public class VKLoadService  extends AbstractAsyncService<Object> {
  private ObjectProperty<Callable<?>> objectProperty = new SimpleObjectProperty<>();

  @Override
  protected Task<Object> createTask() {
    Callable<?> _callable = objectProperty.get();
    return new Task<Object>() {
      @Override
      protected Object call() throws Exception {
        return _callable.call();
      }
    };
  }

  public <T> void vkRun(Callable<T> callable, VKLoadListener<T> succeededListener) {
    objectProperty.setValue(callable);
    restart(succeededListener);
  }
}
