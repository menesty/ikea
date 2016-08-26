package org.menesty.ikea.domain.vk;

/**
 * Created by Menesty on
 * 7/14/16.
 * 14:13.
 */
public class VKResponse<T> {
  private T response;

  public T getResponse() {
    return response;
  }

  public void setResponse(T response) {
    this.response = response;
  }
}


