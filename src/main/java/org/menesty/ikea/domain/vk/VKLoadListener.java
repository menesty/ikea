package org.menesty.ikea.domain.vk;

import org.menesty.ikea.service.ServiceFacade;
import org.menesty.ikea.service.SucceededListener;

/**
 * Created by Menesty on
 * 7/14/16.
 * 15:19.
 */
public interface VKLoadListener<T> extends SucceededListener<Object> {
  @SuppressWarnings("unchecked")
  default void onSucceeded(Object value) {
    try {
      onLoad((T) value);
    } catch (Exception e) {
      ServiceFacade.getErrorConsole().add(e);
    }
  }

  void onLoad(T value) throws Exception;
}
