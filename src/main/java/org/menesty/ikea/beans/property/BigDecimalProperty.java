package org.menesty.ikea.beans.property;

import com.sun.javafx.binding.Logging;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import org.menesty.ikea.beans.binding.BidirectionalBinding;
import org.menesty.ikea.beans.value.WritableBigDecimalValue;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 16:05.
 */
public abstract class BigDecimalProperty extends ReadOnlyBigDecimalProperty implements
    Property<Number>, WritableBigDecimalValue {

  /**
   * {@inheritDoc}
   */
  @Override
  public void setValue(Number v) {
    if (v == null) {
      set(BigDecimal.ZERO);
    } else {
      set(new BigDecimal(v.toString()));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void bindBidirectional(Property<Number> other) {
    Bindings.bindBidirectional(this, other);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unbindBidirectional(Property<Number> other) {
    Bindings.unbindBidirectional(this, other);
  }

  /**
   * Returns a string representation of this {@code BigDecimalProperty} object.
   *
   * @return a string representation of this {@code BigDecimalProperty} object.
   */
  @Override
  public String toString() {
    final Object bean = getBean();
    final String name = getName();
    final StringBuilder result = new StringBuilder(
        "BigDecimalProperty [");
    if (bean != null) {
      result.append("bean: ").append(bean).append(", ");
    }
    if ((name != null) && (!name.equals(""))) {
      result.append("name: ").append(name).append(", ");
    }
    result.append("value: ").append(get()).append("]");
    return result.toString();
  }

  /**
   * Returns a {@code BigDecimalProperty} that wraps a
   * {@link javafx.beans.property.Property} and is
   * bidirectionally bound to it.
   * Changing this property will result in a change of the original property.
   * <p>
   * <p>
   * This is very useful when bidirectionally binding an ObjectProperty<BigDecimal> and
   * a BigDecimalProperty.
   * <p>
   * <blockquote><pre>
   *   BigDecimalProperty bigDecimalProperty = new SimpleBigDecimalProperty(1);
   *   ObjectProperty&lt;BigDecimal&gt; objectProperty = new SimpleObjectProperty&lt;&gt;(2);
   * <p>
   *   // Need to keep the reference as bidirectional binding uses weak references
   *   BigDecimalProperty objectAsBigDecimal = BigDecimalProperty.bigDecimalProperty(objectProperty);
   * <p>
   *   bigDecimalProperty.bindBidirectional(objectAsBigDecimal);
   * <p>
   * </pre></blockquote>
   * <p>
   * Another approach is to convert the BigDecimalProperty to ObjectProperty using
   * {@link #asObject()} method.
   * <p>
   * <p>
   * Note: null values in the source property will be interpreted as 0
   *
   * @param property The source {@code Property}
   * @return A {@code BigDecimalProperty} that wraps the
   * {@code Property}
   * @throws NullPointerException if {@code property} is {@code null}
   * @see #asObject()
   * @since JavaFX 8.0
   */
  public static BigDecimalProperty bigDecimalProperty(final Property<BigDecimal> property) {
    if (property == null) {
      throw new NullPointerException("Property cannot be null");
    }
    return new BigDecimalPropertyBase() {
      {
        BidirectionalBinding.bindNumber(this, property);
      }

      @Override
      public Object getBean() {
        return null; // Virtual property, no bean
      }

      @Override
      public String getName() {
        return property.getName();
      }

      @Override
      protected void finalize() throws Throwable {
        try {
          BidirectionalBinding.unbindNumber(property, this);
        } finally {
          super.finalize();
        }
      }
    };
  }

  /**
   * Creates an {@link javafx.beans.property.ObjectProperty}
   * that bidirectionally bound to this {@code BigDecimalProperty}. If the
   * value of this {@code BigDecimalProperty} changes, the value of the
   * {@code ObjectProperty} will be updated automatically and vice-versa.
   * <p>
   * <p>
   * Can be used for binding an ObjectProperty to BigDecimalProperty.
   * <p>
   * <blockquote><pre>
   *   BigDecimalProperty bigDecimalProperty = new SimpleBigDecimalProperty(1);
   *   ObjectProperty&lt;BigDecimal&gt; objectProperty = new SimpleObjectProperty&lt;&gt;(2);
   * <p>
   *   objectProperty.bind(bigDecimalProperty.asObject());
   * </pre></blockquote>
   *
   * @return the new {@code ObjectProperty}
   * @since JavaFX 8.0
   */
  @Override
  public ObjectProperty<BigDecimal> asObject() {
    return new ObjectPropertyBase<BigDecimal>() {

      {
        BidirectionalBinding.bindNumber(this, BigDecimalProperty.this);
      }

      @Override
      public Object getBean() {
        return null; // Virtual property, does not exist on a bean
      }

      @Override
      public String getName() {
        return BigDecimalProperty.this.getName();
      }

      @Override
      protected void finalize() throws Throwable {
        try {
          BidirectionalBinding.unbindNumber(this, BigDecimalProperty.this);
        } finally {
          super.finalize();
        }
      }

    };
  }
}
