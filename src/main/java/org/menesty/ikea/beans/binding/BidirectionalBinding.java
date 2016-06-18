package org.menesty.ikea.beans.binding;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.menesty.ikea.beans.property.BigDecimalProperty;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

/**
 * Created by Menesty on
 * 3/10/16.
 * 16:22.
 */
public abstract class BidirectionalBinding<T> implements ChangeListener<T> {
  private final int cachedHashCode;

  private BidirectionalBinding(Object property1, Object property2) {
    cachedHashCode = property1.hashCode() * property2.hashCode();
  }

  private static void checkParameters(Object property1, Object property2) {
    if ((property1 == null) || (property2 == null)) {
      throw new NullPointerException("Both properties must be specified.");
    }
    if (property1 == property2) {
      throw new IllegalArgumentException("Cannot bind property to itself");
    }
  }

  protected abstract Object getProperty1();

  protected abstract Object getProperty2();


  public static BidirectionalBinding bindNumber(Property<BigDecimal> property1, BigDecimalProperty property2) {
    return bindNumber(property1, (Property<Number>) property2);
  }

  public static BidirectionalBinding bindNumber(BigDecimalProperty property1, Property<BigDecimal> property2) {
    return bindNumberObject(property1, property2);
  }

  private static <T extends Number> BidirectionalBinding bindNumberObject(Property<Number> property1, Property<T> property2) {
    checkParameters(property1, property2);

    final BidirectionalBinding<Number> binding = new TypedNumberBidirectionalBinding<T>(property2, property1);

    property1.setValue(property2.getValue());
    property1.addListener(binding);
    property2.addListener(binding);
    return binding;
  }

  private static <T extends Number> BidirectionalBinding bindNumber(Property<T> property1, Property<Number> property2) {
    checkParameters(property1, property2);

    final BidirectionalBinding<Number> binding = new TypedNumberBidirectionalBinding<T>(property1, property2);

    property1.setValue((T) property2.getValue());
    property1.addListener(binding);
    property2.addListener(binding);
    return binding;
  }

  public static void unbind(Object property1, Object property2) {
    checkParameters(property1, property2);
    final BidirectionalBinding binding = new UntypedGenericBidirectionalBinding(property1, property2);
    if (property1 instanceof ObservableValue) {
      ((ObservableValue) property1).removeListener(binding);
    }
    if (property2 instanceof Observable) {
      ((ObservableValue) property2).removeListener(binding);
    }
  }

  public static <T extends Number> void unbindNumber(Property<T> property1, Property<Number> property2) {
    checkParameters(property1, property2);
    final BidirectionalBinding binding = new UntypedGenericBidirectionalBinding(property1, property2);
    if (property1 instanceof ObservableValue) {
      ((ObservableValue) property1).removeListener(binding);
    }
    if (property2 instanceof Observable) {
      ((ObservableValue) property2).removeListener(binding);
    }
  }

  private static class TypedNumberBidirectionalBinding<T extends Number> extends BidirectionalBinding<Number> {
    private final WeakReference<Property<T>> propertyRef1;
    private final WeakReference<Property<Number>> propertyRef2;
    private boolean updating = false;

    private TypedNumberBidirectionalBinding(Property<T> property1, Property<Number> property2) {
      super(property1, property2);
      propertyRef1 = new WeakReference<Property<T>>(property1);
      propertyRef2 = new WeakReference<Property<Number>>(property2);
    }

    @Override
    protected Property<T> getProperty1() {
      return propertyRef1.get();
    }

    @Override
    protected Property<Number> getProperty2() {
      return propertyRef2.get();
    }

    @Override
    public void changed(ObservableValue<? extends Number> sourceProperty, Number oldValue, Number newValue) {
      if (!updating) {
        final Property<T> property1 = propertyRef1.get();
        final Property<Number> property2 = propertyRef2.get();
        if ((property1 == null) || (property2 == null)) {
          if (property1 != null) {
            property1.removeListener(this);
          }
          if (property2 != null) {
            property2.removeListener(this);
          }
        } else {
          try {
            updating = true;
            if (property1 == sourceProperty) {
              property2.setValue(newValue);
            } else {
              property1.setValue((T) newValue);
            }
          } catch (RuntimeException e) {
            try {
              if (property1 == sourceProperty) {
                property1.setValue((T) oldValue);
              } else {
                property2.setValue(oldValue);
              }
            } catch (Exception e2) {
              e2.addSuppressed(e);
              unbind(property1, property2);
              throw new RuntimeException(
                  "Bidirectional binding failed together with an attempt"
                      + " to restore the source property to the previous value."
                      + " Removing the bidirectional binding from properties " +
                      property1 + " and " + property2, e2);
            }
            throw new RuntimeException(
                "Bidirectional binding failed, setting to the previous value", e);
          } finally {
            updating = false;
          }
        }
      }
    }
  }

  private static class UntypedGenericBidirectionalBinding extends BidirectionalBinding<Object> {

    private final Object property1;
    private final Object property2;

    public UntypedGenericBidirectionalBinding(Object property1, Object property2) {
      super(property1, property2);
      this.property1 = property1;
      this.property2 = property2;
    }

    @Override
    protected Object getProperty1() {
      return property1;
    }

    @Override
    protected Object getProperty2() {
      return property2;
    }

    @Override
    public void changed(ObservableValue<? extends Object> sourceProperty, Object oldValue, Object newValue) {
      throw new RuntimeException("Should not reach here");
    }
  }
}
