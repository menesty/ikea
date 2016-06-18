package org.menesty.ikea.beans.property;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 15:46.
 */
public class SimpleBigDecimalProperty extends BigDecimalPropertyBase {
  private static final Object DEFAULT_BEAN = null;
  private static final String DEFAULT_NAME = "";

  private final Object bean;
  private final String name;

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getBean() {
    return bean;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * The constructor of {@code BigDecimalProperty}
   */
  public SimpleBigDecimalProperty() {
    this(DEFAULT_BEAN, DEFAULT_NAME);
  }

  /**
   * The constructor of {@code BigDecimalProperty}
   *
   * @param initialValue the initial value of the wrapped value
   */
  public SimpleBigDecimalProperty(BigDecimal initialValue) {
    this(DEFAULT_BEAN, DEFAULT_NAME, initialValue);
  }

  /**
   * The constructor of {@code BigDecimalProperty}
   *
   * @param bean the bean of this {@code BigDecimalProperty}
   * @param name the name of this {@code BigDecimalProperty}
   */
  public SimpleBigDecimalProperty(Object bean, String name) {
    this.bean = bean;
    this.name = (name == null) ? DEFAULT_NAME : name;
  }

  /**
   * The constructor of {@code IntegerProperty}
   *
   * @param bean         the bean of this {@code BigDecimalProperty}
   * @param name         the name of this {@code BigDecimalProperty}
   * @param initialValue the initial value of the wrapped value
   */
  public SimpleBigDecimalProperty(Object bean, String name, BigDecimal initialValue) {
    super(initialValue);
    this.bean = bean;
    this.name = (name == null) ? DEFAULT_NAME : name;
  }
}
