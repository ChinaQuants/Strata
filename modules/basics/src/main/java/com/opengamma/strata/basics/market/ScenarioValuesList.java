/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.basics.market;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.ArgChecker;

/**
 * A simple {@link ScenarioMarketDataValue} implementation containing a list of single market data values, one
 * for each scenario.
 *
 * @param <T> the type of the market data value used in each scenario
 */
@BeanDefinition
public final class ScenarioValuesList<T> implements ScenarioMarketDataValue<T>, ImmutableBean {

  /** The market data values, one for each scenario. */
  @PropertyDefinition(validate = "notEmpty")
  private final ImmutableList<T> values;

  @Override
  public T getValue(int scenarioIndex) {
    ArgChecker.inRange(scenarioIndex, 0, values.size(), "scenarioIndex");
    return values.get(scenarioIndex);
  }

  @Override
  public int getScenarioCount() {
    return values.size();
  }

  /**
   * Returns a scenario values list containing the values.
   *
   * @param values  market data values, one for each scenario
   * @return a scenario values list containing the values
   */
  public static <T> ScenarioValuesList<T> of(List<T> values) {
    return new ScenarioValuesList<>(values);
  }

  /**
   * Returns a scenario values list containing the values.
   *
   * @param values  market data values, one for each scenario
   * @return a scenario values list containing the values
   */
  @SafeVarargs
  public static <T> ScenarioValuesList<T> of(T... values) {
    return new ScenarioValuesList<>(ImmutableList.copyOf(values));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ScenarioValuesList}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static ScenarioValuesList.Meta meta() {
    return ScenarioValuesList.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code ScenarioValuesList}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> ScenarioValuesList.Meta<R> metaScenarioValuesList(Class<R> cls) {
    return ScenarioValuesList.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ScenarioValuesList.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @param <T>  the type
   * @return the builder, not null
   */
  public static <T> ScenarioValuesList.Builder<T> builder() {
    return new ScenarioValuesList.Builder<T>();
  }

  private ScenarioValuesList(
      List<T> values) {
    JodaBeanUtils.notEmpty(values, "values");
    this.values = ImmutableList.copyOf(values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ScenarioValuesList.Meta<T> metaBean() {
    return ScenarioValuesList.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data values, one for each scenario.
   * @return the value of the property, not empty
   */
  public ImmutableList<T> getValues() {
    return values;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder<T> toBuilder() {
    return new Builder<T>(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ScenarioValuesList<?> other = (ScenarioValuesList<?>) obj;
      return JodaBeanUtils.equal(values, other.values);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(values);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ScenarioValuesList{");
    buf.append("values").append('=').append(JodaBeanUtils.toString(values));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ScenarioValuesList}.
   * @param <T>  the type
   */
  public static final class Meta<T> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code values} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableList<T>> values = DirectMetaProperty.ofImmutable(
        this, "values", ScenarioValuesList.class, (Class) ImmutableList.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "values");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return values;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ScenarioValuesList.Builder<T> builder() {
      return new ScenarioValuesList.Builder<T>();
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends ScenarioValuesList<T>> beanType() {
      return (Class) ScenarioValuesList.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableList<T>> values() {
      return values;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return ((ScenarioValuesList<?>) bean).getValues();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ScenarioValuesList}.
   * @param <T>  the type
   */
  public static final class Builder<T> extends DirectFieldsBeanBuilder<ScenarioValuesList<T>> {

    private List<T> values = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ScenarioValuesList<T> beanToCopy) {
      this.values = beanToCopy.getValues();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return values;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder<T> set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          this.values = (List<T>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder<T> set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder<T> setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder<T> setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder<T> setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ScenarioValuesList<T> build() {
      return new ScenarioValuesList<T>(
          values);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the market data values, one for each scenario.
     * @param values  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder<T> values(List<T> values) {
      JodaBeanUtils.notEmpty(values, "values");
      this.values = values;
      return this;
    }

    /**
     * Sets the {@code values} property in the builder
     * from an array of objects.
     * @param values  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder<T> values(T... values) {
      return values(ImmutableList.copyOf(values));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ScenarioValuesList.Builder{");
      buf.append("values").append('=').append(JodaBeanUtils.toString(values));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
