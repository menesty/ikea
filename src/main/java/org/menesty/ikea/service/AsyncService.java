package org.menesty.ikea.service;

/**
 * Created by Menesty on
 * 9/1/15.
 * 22:50.
 */
public interface AsyncService<T> {
    void setOnSucceededListener(final SucceededListener<T> listener);
}
