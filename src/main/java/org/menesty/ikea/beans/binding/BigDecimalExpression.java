package org.menesty.ikea.beans.binding;

import javafx.beans.binding.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.menesty.ikea.beans.value.ObservableBigDecimalValue;

import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 15:52.
 */
public abstract class BigDecimalExpression extends NumberExpressionBase implements
    ObservableBigDecimalValue {

  @Override
  public int intValue() {
    return get().intValue();
  }

  @Override
  public long longValue() {
    return get().longValue();
  }

  @Override
  public float floatValue() {
    return get().floatValue();
  }

  @Override
  public double doubleValue() {
    return get().doubleValue();
  }

  @Override
  public BigDecimal getValue() {
    return get();
  }

  /**
   * Returns a {@code IntegerExpression} that wraps a
   * {@link javafx.beans.value.ObservableIntegerValue}. If the
   * {@code ObservableIntegerValue} is already a {@code IntegerExpression}, it
   * will be returned. Otherwise a new
   * {@link javafx.beans.binding.IntegerBinding} is created that is bound to
   * the {@code ObservableIntegerValue}.
   *
   * @param value The source {@code ObservableIntegerValue}
   * @return A {@code IntegerExpression} that wraps the
   * {@code ObservableIntegerValue} if necessary
   * @throws NullPointerException if {@code value} is {@code null}
   */
  public static BigDecimalExpression bigDecimalExpression(
      final ObservableBigDecimalValue value) {
    if (value == null) {
      throw new NullPointerException("Value must be specified.");
    }
    return (value instanceof BigDecimalExpression) ? (BigDecimalExpression) value
        : new BigDecimalBinding() {
      {
        super.bind(value);
      }

      @Override
      public void dispose() {
        super.unbind(value);
      }

      @Override
      protected BigDecimal computeValue() {
        return value.get();
      }

      @Override
      public ObservableList<ObservableBigDecimalValue> getDependencies() {
        return FXCollections.singletonObservableList(value);
      }
    };
  }

  /**
   * Returns an {@code BigDecimalExpression} that wraps an
   * {@link javafx.beans.value.ObservableValue}. If the
   * {@code ObservableValue} is already a {@code BigDecimalExpression}, it
   * will be returned. Otherwise a new
   * {@link BigDecimalBinding} is created that is bound to
   * the {@code ObservableValue}.
   * <p>
   * <p>
   * Note: this method can be used to convert an {@link ObjectExpression} or
   * {@link javafx.beans.property.ObjectProperty} of specific number type to BigDecimalExpression, which
   * is essentially an {@code ObservableValue<Number>}. See sample below.
   * <p>
   * <blockquote><pre>
   *   BigDecimalProperty bigDecimalProperty = new SimpleBigDecimalProperty(1);
   *   ObjectProperty&lt;BigDecimal&gt; objectProperty = new SimpleObjectProperty&lt;&gt;(2);
   *   BooleanBinding binding = bigDecimalProperty.greaterThan(BigDecimalExpression.bigDecimalExpression(objectProperty));
   * </pre></blockquote>
   * <p>
   * Note: null values will be interpreted as 0
   *
   * @param value The source {@code ObservableValue}
   * @return A {@code BigDecimalExpression} that wraps the
   * {@code ObservableValue} if necessary
   * @throws NullPointerException if {@code value} is {@code null}
   * @since JavaFX 8.0
   */
  public static <T extends Number> BigDecimalExpression bigDecimalExpression(final ObservableValue<T> value) {
    if (value == null) {
      throw new NullPointerException("Value must be specified.");
    }
    return (value instanceof BigDecimalExpression) ? (BigDecimalExpression) value
        : new BigDecimalBinding() {
      {
        super.bind(value);
      }

      @Override
      public void dispose() {
        super.unbind(value);
      }

      @Override
      protected BigDecimal computeValue() {
        final T val = value.getValue();
        return val == null ? BigDecimal.ZERO : new BigDecimal(val.toString());
      }

      @Override
      public ObservableList<ObservableValue<T>> getDependencies() {
        return FXCollections.singletonObservableList(value);
      }
    };
  }


  @Override
  public IntegerBinding negate() {
    return (IntegerBinding) Bindings.negate(this);
  }

  @Override
  public DoubleBinding add(final double other) {
    return Bindings.add(this, other);
  }

  @Override
  public FloatBinding add(final float other) {
    return (FloatBinding) Bindings.add(this, other);
  }

  @Override
  public LongBinding add(final long other) {
    return (LongBinding) Bindings.add(this, other);
  }

  @Override
  public IntegerBinding add(final int other) {
    return (IntegerBinding) Bindings.add(this, other);
  }

  @Override
  public DoubleBinding subtract(final double other) {
    return Bindings.subtract(this, other);
  }

  @Override
  public FloatBinding subtract(final float other) {
    return (FloatBinding) Bindings.subtract(this, other);
  }

  @Override
  public LongBinding subtract(final long other) {
    return (LongBinding) Bindings.subtract(this, other);
  }

  @Override
  public IntegerBinding subtract(final int other) {
    return (IntegerBinding) Bindings.subtract(this, other);
  }

  @Override
  public DoubleBinding multiply(final double other) {
    return Bindings.multiply(this, other);
  }

  @Override
  public FloatBinding multiply(final float other) {
    return (FloatBinding) Bindings.multiply(this, other);
  }

  @Override
  public LongBinding multiply(final long other) {
    return (LongBinding) Bindings.multiply(this, other);
  }

  @Override
  public IntegerBinding multiply(final int other) {
    return (IntegerBinding) Bindings.multiply(this, other);
  }

  @Override
  public DoubleBinding divide(final double other) {
    return Bindings.divide(this, other);
  }

  @Override
  public FloatBinding divide(final float other) {
    return (FloatBinding) Bindings.divide(this, other);
  }

  @Override
  public LongBinding divide(final long other) {
    return (LongBinding) Bindings.divide(this, other);
  }

  @Override
  public IntegerBinding divide(final int other) {
    return (IntegerBinding) Bindings.divide(this, other);
  }

  /**
   * Creates an {@link javafx.beans.binding.ObjectExpression} that holds the value
   * of this {@code IntegerExpression}. If the
   * value of this {@code IntegerExpression} changes, the value of the
   * {@code ObjectExpression} will be updated automatically.
   *
   * @return the new {@code ObjectExpression}
   * @since JavaFX 8.0
   */
  public ObjectExpression<BigDecimal> asObject() {
    return new ObjectBinding<BigDecimal>() {
      {
        bind(BigDecimalExpression.this);
      }

      @Override
      public void dispose() {
        unbind(BigDecimalExpression.this);
      }

      @Override
      protected BigDecimal computeValue() {
        return BigDecimalExpression.this.getValue();
      }
    };
  }
}
